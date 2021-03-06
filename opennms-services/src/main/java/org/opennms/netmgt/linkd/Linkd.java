/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>Linkd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Linkd extends AbstractServiceDaemon {

	/**
	 * The log4j category used to log messages.
	 */
	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	/**
	 * Rescan scheduler thread
	 */
	@Autowired
	private Scheduler m_scheduler;

    /**
     * The DB connection read and write handler
     */
    @Autowired
    private QueryManager m_queryMgr;
    
    /**
    * Linkd Configuration Initialization
    */
    
    @Autowired
    private LinkdConfig m_linkdConfig;
	
	/**
	 * List that contains Linkable Nodes.
	 */
	private List<LinkableNode> m_nodes;

	/**
	 * HashMap that contains SnmpCollections by package.
	 */
	private List<String> m_activepackages;
	
	/**
	 * the list of {@link java.net.InetAddress} for which new suspect event is sent
	 */
	private Set<InetAddress> m_newSuspectEventsIpAddr = null;

	/**
	 * Event handler
	 */
    private volatile EventForwarder m_eventForwarder;

    /**
	 * <p>Constructor for Linkd.</p>
	 */
	public Linkd() {
		super(LOG4J_CATEGORY);
	}

	/**
	 * <p>onInit</p>
	 */
	protected void onInit() {

        Assert.state(m_queryMgr != null, "must set the queryManager property");
        Assert.state(m_linkdConfig != null, "must set the linkdConfig property");
        Assert.state(m_scheduler != null, "must set the scheduler property");
        Assert.state(m_eventForwarder != null, "must set the eventForwarder property");

        // FIXME: circular dependency
        m_queryMgr.setLinkd(this);

		m_activepackages = new ArrayList<String>();
		
		// initialize the ipaddrsentevents
		m_newSuspectEventsIpAddr = new TreeSet<InetAddress>(new InetAddressComparator());
		m_newSuspectEventsIpAddr.add(addr("127.0.0.1"));
		m_newSuspectEventsIpAddr.add(addr("0.0.0.0"));

		try {
			m_nodes = m_queryMgr.getSnmpNodeList();
			m_queryMgr.updateDeletedNodes();
		} catch (SQLException e) {
		    LogUtils.errorf(this, e, "SQL exception executing on database");
	        throw new UndeclaredThrowableException(e);
		}

		Assert.notNull(m_nodes);
		scheduleCollection();
		
		LogUtils.infof(this, "init: LINKD CONFIGURATION INITIALIZED");
	}

	private void scheduleCollection() {
	    synchronized (m_nodes) {
	        for (final LinkableNode node : m_nodes) {
	            scheduleCollectionForNode(node);
	        }
        }
	}

	/**
	 * This method schedules a {@link SnmpCollection} for node
	 * for each package.
	 * Also schedule discovery link on package 
	 * when not still activated.
	 * @param node
	 */
	private void scheduleCollectionForNode(final LinkableNode node) {

		for (final SnmpCollection snmpcoll : getSnmpCollections(node.getNodeId(), node.getSnmpPrimaryIpAddr(), node.getSysoid())) {
			if (m_activepackages.contains(snmpcoll.getPackageName())) {
			    LogUtils.debugf(this, "ScheduleCollectionForNode: package active: %s", snmpcoll.getPackageName());
			} else {
				// schedule discovery link
			    LogUtils.debugf(this, "ScheduleCollectionForNode: Scheduling Discovery Link for Active Package: %s", snmpcoll.getPackageName());
			    final DiscoveryLink discovery = this.getDiscoveryLink(snmpcoll.getPackageName());
	   			if (discovery.getScheduler() == null) {
	   				discovery.setScheduler(m_scheduler);
	    		}
	   			discovery.schedule();
	    		m_activepackages.add(snmpcoll.getPackageName());

			}
			if (snmpcoll.getScheduler() == null) {
					snmpcoll.setScheduler(m_scheduler);
			}
			LogUtils.debugf(this, "ScheduleCollectionForNode: Scheduling SNMP Collection for Package/NodeId: %s/%d/%s", snmpcoll.getPackageName(), node.getNodeId(), snmpcoll.getInfo());
			snmpcoll.schedule();
		}
	}

    /** {@inheritDoc} */
    public DiscoveryLink getDiscoveryLink(final String pkgName) {
        final Package pkg = m_linkdConfig.getPackage(pkgName);

        if (pkg == null) return null;

        final DiscoveryLink discoveryLink = new DiscoveryLink();
        discoveryLink.setLinkd(this);
        discoveryLink.setPackageName(pkg.getName());
        discoveryLink.setInitialSleepTime(m_linkdConfig.getInitialSleepTime());

        discoveryLink.setSnmpPollInterval(pkg.hasSnmp_poll_interval()? pkg.getSnmp_poll_interval() : m_linkdConfig.getSnmpPollInterval());
        discoveryLink.setDiscoveryInterval(pkg.hasDiscovery_link_interval()? pkg.getDiscovery_link_interval() : m_linkdConfig.getDiscoveryLinkInterval());
        discoveryLink.setDiscoveryUsingBridge(pkg.hasUseBridgeDiscovery()? pkg.getUseBridgeDiscovery() : m_linkdConfig.useBridgeDiscovery());
        discoveryLink.setDiscoveryUsingCdp(pkg.hasUseCdpDiscovery()? pkg.getUseCdpDiscovery() : m_linkdConfig.useCdpDiscovery());
        discoveryLink.setDiscoveryUsingRoutes(pkg.hasUseIpRouteDiscovery()? pkg.getUseIpRouteDiscovery() : m_linkdConfig.useIpRouteDiscovery());
        discoveryLink.setEnableDownloadDiscovery(pkg.hasEnableDiscoveryDownload()? pkg.getEnableDiscoveryDownload() : m_linkdConfig.enableDiscoveryDownload());
        discoveryLink.setForceIpRouteDiscoveryOnEtherNet(pkg.hasForceIpRouteDiscoveryOnEthernet()? pkg.getForceIpRouteDiscoveryOnEthernet() : m_linkdConfig.forceIpRouteDiscoveryOnEthernet());

        return discoveryLink;
    }

    /**
     * {@inheritDoc}
     * @param nodeid
     */
    public SnmpCollection getSnmpCollection(final int nodeid, final InetAddress ipaddr, final String sysoid, final String pkgName) {
        final Package pkg = m_linkdConfig.getPackage(pkgName);
        if (pkg != null) {
            final SnmpCollection collection = createCollection(nodeid, ipaddr);
            populateSnmpCollection(collection, pkg, sysoid);
            return collection;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @param nodeid
     */
    public List<SnmpCollection> getSnmpCollections(int nodeid, final InetAddress ipaddr, final String sysoid) {
        List<SnmpCollection> snmpcolls = new ArrayList<SnmpCollection>();

        for (final String pkgName : m_linkdConfig.getAllPackageMatches(ipaddr)) {
            snmpcolls.add(getSnmpCollection(nodeid, ipaddr, sysoid, pkgName));
        }

        return snmpcolls;
    }

    public SnmpCollection createCollection(int nodeid, final InetAddress ipaddr) {
        SnmpCollection coll = null;
        try {
            coll = new SnmpCollection(this, nodeid, SnmpPeerFactory.getInstance().getAgentConfig(ipaddr));
        } catch (final Throwable t) {
            LogUtils.errorf(this, t, "getSnmpCollection: Failed to load snmpcollection parameter from SNMP configuration file");
        }

        return coll;
    }

    private void populateSnmpCollection(final SnmpCollection coll, final Package pkg, final String sysoid) {
        coll.setPackageName(pkg.getName());
        coll.setInitialSleepTime(m_linkdConfig.getInitialSleepTime());
        coll.setPollInterval(pkg.hasSnmp_poll_interval()? pkg.getSnmp_poll_interval() : m_linkdConfig.getSnmpPollInterval());
// TODO: Put this logic inside LinkdConfigManager
        if (m_linkdConfig.hasIpRouteClassName(sysoid)) {
            coll.setIpRouteClass(m_linkdConfig.getIpRouteClassName(sysoid));
            LogUtils.debugf(this, "populateSnmpCollection: found class to get ipRoute: %s", coll.getIpRouteClass());
        } else {
            coll.setIpRouteClass(m_linkdConfig.getDefaultIpRouteClassName());
            LogUtils.debugf(this, "populateSnmpCollection: Using default class to get ipRoute: %s", coll.getIpRouteClass());
        }
        
        if (pkg.hasEnableVlanDiscovery() && pkg.getEnableVlanDiscovery() && m_linkdConfig.hasClassName(sysoid)) {
            coll.setVlanClass(m_linkdConfig.getVlanClassName(sysoid));
            LogUtils.debugf(this, "populateSnmpCollection: found class to get Vlans: %s", coll.getVlanClass());
        } else if (!pkg.hasEnableVlanDiscovery() && m_linkdConfig.isVlanDiscoveryEnabled() && m_linkdConfig.hasClassName(sysoid)) {
            coll.setVlanClass(m_linkdConfig.getVlanClassName(sysoid));
            LogUtils.debugf(this, "populateSnmpCollection: found class to get Vlans: %s", coll.getVlanClass());
        } else {
            LogUtils.debugf(this, "populateSnmpCollection: no class found to get Vlans or VlanDiscoveryDisabled for Package: %s", pkg.getName());
        }

        coll.collectCdpTable(pkg.hasUseCdpDiscovery()? pkg.getUseCdpDiscovery() : m_linkdConfig.useCdpDiscovery());

        final boolean useIpRouteDiscovery = (pkg.hasUseIpRouteDiscovery()? pkg.getUseIpRouteDiscovery() : m_linkdConfig.useIpRouteDiscovery());
        final boolean saveRouteTable = (pkg.hasSaveRouteTable()? pkg.getSaveRouteTable() : m_linkdConfig.saveRouteTable());

        coll.SaveIpRouteTable(saveRouteTable);
        coll.collectIpRouteTable(useIpRouteDiscovery || saveRouteTable);

        final boolean useBridgeDiscovery = (pkg.hasUseBridgeDiscovery()? pkg.getUseBridgeDiscovery() : m_linkdConfig.useBridgeDiscovery());
        coll.collectBridgeForwardingTable(useBridgeDiscovery);

        final boolean saveStpNodeTable = (pkg.hasSaveStpNodeTable()? pkg.getSaveStpNodeTable() : m_linkdConfig.saveStpNodeTable());

        coll.saveStpNodeTable(saveStpNodeTable);
        coll.collectStpNode(useBridgeDiscovery || saveStpNodeTable);

        final boolean saveStpInterfaceTable = (pkg.hasSaveStpInterfaceTable()? pkg.getSaveStpInterfaceTable() : m_linkdConfig.saveStpInterfaceTable());
        
        coll.saveStpInterfaceTable(saveStpInterfaceTable);
        coll.collectStpTable(useBridgeDiscovery || saveStpInterfaceTable);
    }

	/**
	 * <p>onStart</p>
	 */
	protected synchronized void onStart() {

		// start the scheduler
		//
	    LogUtils.debugf(this, "start: Starting linkd scheduler");
		m_scheduler.start();

		// Set the status of the service as running.
		//

	}

	/**
	 * <p>onStop</p>
	 */
	protected synchronized void onStop() {

		// Stop the scheduler
		m_scheduler.stop();

		m_scheduler = null;

	}

	/**
	 * <p>onPause</p>
	 */
	protected synchronized void onPause() {
		m_scheduler.pause();
	}

	/**
	 * <p>onResume</p>
	 */
	protected synchronized void onResume() {
		m_scheduler.resume();
	}

	/**
	 * <p>getLinkableNodes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<LinkableNode> getLinkableNodes() {
		synchronized (m_nodes) {
			return m_nodes;
		}
	}

	/**
	 * <p>getLinkableNodesOnPackage</p>
	 *
	 * @param pkg a {@link java.lang.String} object.
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<LinkableNode> getLinkableNodesOnPackage(String pkg) {
		Collection<LinkableNode> nodesOnPkg = new ArrayList<LinkableNode>();
		synchronized (m_nodes) {
		    for (final LinkableNode node : m_nodes) {
				if (isInterfaceInPackage(node.getSnmpPrimaryIpAddr(), pkg))
					nodesOnPkg.add(node);
			}
			return nodesOnPkg;
		}
	}
	
	/**
	 * <p>isInterfaceInPackage</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 * @param pkg a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isInterfaceInPackage(InetAddress ipaddr, String pkg) {
		return m_linkdConfig.isInterfaceInPackage(ipaddr, m_linkdConfig.getPackage(pkg));
	}

	/**
	 * <p>isInterfaceInPackageRange</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 * @param pkg a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isInterfaceInPackageRange(InetAddress ipaddr, String pkg) {
		return m_linkdConfig.isInterfaceInPackageRange(ipaddr, m_linkdConfig.getPackage(pkg));
	}

	public boolean scheduleNodeCollection(int nodeid) {

		LinkableNode node = null;
		// database changed need reload packageiplist
		m_linkdConfig.updatePackageIpListMap();
		

		// First of all get Linkable Node
		LogUtils.debugf(this, "scheduleNodeCollection: Loading node %d from database", nodeid);
		try {
			node = m_queryMgr.getSnmpNode(nodeid);
			if (node == null) {
			    LogUtils.warnf(this, "scheduleNodeCollection: Failed to get linkable node from database with ID %d. Exiting", nodeid);
				return false;
			}
		} catch (final SQLException sqlE) {
		    LogUtils.errorf(this, sqlE, "scheduleNodeCollection: SQL Exception while syncing node object with ID %d with database information.", nodeid);
			return false;
		}
		synchronized (m_nodes) {
		    LogUtils.debugf(this, "adding node %s to the collection", node);
	        m_nodes.add(node);
        }
		
		scheduleCollectionForNode(node);
		return true;
	}

	public boolean runSingleCollection(final int nodeId) {
	    try {
            final LinkableNode node = m_queryMgr.getSnmpNode(nodeId);

            for (final SnmpCollection snmpColl : getSnmpCollections(nodeId, node.getSnmpPrimaryIpAddr(), node.getSysoid())) {
                snmpColl.setScheduler(m_scheduler);
                snmpColl.run();
                
                final DiscoveryLink link = getDiscoveryLink(snmpColl.getPackageName());
                link.setScheduler(m_scheduler);
                link.run();
            }

            return true;
        } catch (final SQLException e) {
            LogUtils.debugf(this, "runSingleCollection: unable to get linkable node from database with ID %d", nodeId);
        }
        return false;
	}
	
	void wakeUpNodeCollection(int nodeid) {

		LinkableNode node = getNode(nodeid);

		
		if (node == null) {
		    LogUtils.warnf(this, "wakeUpNodeCollection: node not found during scheduling with ID %d", nodeid);
			scheduleNodeCollection(nodeid);
		} else {
			// get collections
			// get readyRunnuble
			// wakeup RR
			Collection<SnmpCollection> collections = getSnmpCollections(nodeid, node.getSnmpPrimaryIpAddr(), node.getSysoid());
			LogUtils.debugf(this, "wakeUpNodeCollection: fetched SnmpCollections from scratch, iterating over %d objects to wake them up", collections.size());
			for (SnmpCollection collection : collections) {
				ReadyRunnable rr = getReadyRunnable(collection);
				if (rr == null) {
				    LogUtils.warnf(this, "wakeUpNodeCollection: found null ReadyRunnable");
					return;
				} else {
					rr.wakeUp();
				}	
			}
		}

	}

	void deleteNode(int nodeid) {
	    LogUtils.debugf(this, "deleteNode: deleting LinkableNode for node %s", nodeid);

		try {
			m_queryMgr.update(nodeid, QueryManager.ACTION_DELETE);
		} catch (SQLException sqlE) {
		    LogUtils.errorf(this, sqlE, "deleteNode: SQL Exception while syncing node object with database information.");
		} 
		

		LinkableNode node = removeNode(nodeid);

		if (node == null) {
		    LogUtils.warnf(this, "deleteNode: node not found: %d", nodeid);
		} else {
			Collection<SnmpCollection> collections = getSnmpCollections(nodeid, node.getSnmpPrimaryIpAddr(), node.getSysoid());
            LogUtils.debugf(this, "deleteNode: fetched SnmpCollections from scratch, iterating over %d objects to wake them up", collections.size());
            for (SnmpCollection collection : collections) {
				ReadyRunnable rr = getReadyRunnable(collection);
				
				if (rr == null) {
				    LogUtils.warnf(this, "deleteNode: found null ReadyRunnable");
					return;
				} else {
					rr.unschedule();
				}	

			}
			
		}

		// database changed need reload packageiplist
		m_linkdConfig.updatePackageIpListMap();

	}
	
	/**
	 * Update database when an interface is deleted
	 * 
	 * @param nodeid
	 *            the nodeid for the node
	 * @param ipAddr
	 *            the ip address of the interface
	 * @param ifIndex
	 *            the ifIndex of the interface
	 */
	void deleteInterface(int nodeid, String ipAddr, int ifIndex) {

	    LogUtils.debugf(this, "deleteInterface: marking table entries as deleted for node %d with IP address %s and ifIndex %s", nodeid, ipAddr, (ifIndex > -1 ? "" + ifIndex : "N/A"));

		try {
			m_queryMgr.updateForInterface(nodeid, ipAddr, ifIndex, QueryManager.ACTION_DELETE);
		} catch (SQLException sqlE) {
		    LogUtils.errorf(this, sqlE, "deleteInterface: SQL Exception while updating database.");
		} 

		// database changed need reload packageiplist
		m_linkdConfig.updatePackageIpListMap();

	}

	void suspendNodeCollection(int nodeid) {
		LogUtils.debugf(this, "suspendNodeCollection: suspend collection LinkableNode for node %d", nodeid);

		try {
			m_queryMgr.update(nodeid, QueryManager.ACTION_UPTODATE);
		} catch (SQLException sqlE) {
		    LogUtils.errorf(this, sqlE, "suspendNodeCollection: SQL Exception while syncing node object with database information.");
		} 

		LinkableNode node = getNode(nodeid);

		if (node == null) {
		    LogUtils.warnf(this, "suspendNodeCollection: found null ReadyRunnable");
		} else {
			// get collections
			// get readyRunnuble
			// suspend RR
            Collection<SnmpCollection> collections = getSnmpCollections(nodeid, node.getSnmpPrimaryIpAddr(), node.getSysoid());
            LogUtils.debugf(this, "suspendNodeCollection: fetched SnmpCollections from scratch, iterating over %d objects to wake them up", collections.size());
            for (SnmpCollection collection : collections) {
				ReadyRunnable rr = getReadyRunnable(collection);
				if (rr == null) {
				    LogUtils.warnf(this, "suspendNodeCollection: suspend: node not found: %d", nodeid);
					return;
				} else {
					rr.suspend();
				}	
			}
		}

	}

	private ReadyRunnable getReadyRunnable(ReadyRunnable runnable) {
	    LogUtils.debugf(this, "getReadyRunnable: get ReadyRunnable from scheduler: %s", runnable.getInfo());
		
		return m_scheduler.getReadyRunnable(runnable);
		
	}


	/**
	 * Method that updates info in List nodes and also save info
	 * into database. This method is called by SnmpCollection after all stuff is
	 * done
	 * 
	 * @param snmpcoll
	 */
	@Transactional
	public void updateNodeSnmpCollection(final SnmpCollection snmpcoll) {
	    LogUtils.debugf(this, "Updating SNMP collection for %s", InetAddressUtils.str(snmpcoll.getTarget()));
		LinkableNode node = removeNode(snmpcoll.getTarget());
		if (node == null) {
		    LogUtils.errorf(this, "No node found for SNMP collection: %s unscheduling!", snmpcoll.getInfo());
			m_scheduler.unschedule(snmpcoll);
			return;
		}
		
		try {
			node = m_queryMgr.storeSnmpCollection(node, snmpcoll);
		} catch (SQLException e) {
		    LogUtils.errorf(this, e, "Failed to save on db snmpcollection/package: %s/%s", snmpcoll.getPackageName(), snmpcoll.getInfo());
			return;
		}
		if (node != null) {
    		synchronized (m_nodes) {
    	        m_nodes.add(node);
            }
		}
	}
	
	/**
	 * Method that uses info in hash snmpprimaryip2nodes and also save info
	 * into database. This method is called by DiscoveryLink after all stuff is
	 * done
	 * 
	 * @param discover
	 */
	void updateDiscoveryLinkCollection(final DiscoveryLink discover) {

		try {
			m_queryMgr.storeDiscoveryLink(discover);
		} catch (SQLException e) {
		    LogUtils.errorf(this, e, "Failed to save discoverylink on database for package: %s", discover.getPackageName());
		}
	}
	
	/**
	 * Send a newSuspect event for the interface
	 * construct event with 'linkd' as source
	 * 
	 * @param ipInterface
	 *            The interface for which the newSuspect event is to be
	 *            generated
	 * @param ipowner
	 * 			  The host that hold this ipInterface information           
	 * @pkgName
	 * 		      The package Name of the ready runnable involved
	 */
	void sendNewSuspectEvent(InetAddress ipaddress, InetAddress ipowner, String pkgName) {

		if (m_newSuspectEventsIpAddr.contains(ipaddress) ) {
		    LogUtils.infof(this, "sendNewSuspectEvent: nothing to send, suspect event previously sent for IP address: %s", str(ipaddress));
			return;
		} else if (!isInterfaceInPackageRange(ipaddress, pkgName)) {
		    LogUtils.infof(this, "sendNewSuspectEvent: nothing to send for IP address: %s, not in package: %s", str(ipaddress), pkgName);
			return;
		}

		org.opennms.netmgt.config.linkd.Package pkg = m_linkdConfig.getPackage(pkgName);

		boolean autodiscovery = false;
		if (pkg.hasAutoDiscovery()) autodiscovery = pkg.getAutoDiscovery(); 
		else autodiscovery = m_linkdConfig.isAutoDiscoveryEnabled();
		
		if ( autodiscovery ) {
		    
		    EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "linkd");
				
		    bldr.setHost(str(ipowner));
		    bldr.setInterface(ipaddress);

			m_eventForwarder.sendNow(bldr.getEvent());
			
			m_newSuspectEventsIpAddr.add(ipaddress);
			
		}
	}

	LinkableNode getNode(int nodeid) {
	    synchronized (m_nodes) {
    		for (LinkableNode node : m_nodes) {
    			if (node.getNodeId() == nodeid) return node;
    		}
            return null;
	    }
	}

	private LinkableNode removeNode(int nodeid) {
		synchronized (m_nodes) {
			Iterator<LinkableNode> ite = m_nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				if (curNode.getNodeId() == nodeid) {
					ite.remove();
					return curNode;
				}
			}
	        return null;
		}
	}

	private LinkableNode removeNode(InetAddress ipaddr) {
		synchronized (m_nodes) {
			Iterator<LinkableNode> ite = m_nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				if (curNode.getSnmpPrimaryIpAddr().equals(ipaddr)) {
					ite.remove();
					return curNode;
				}
			}
		}
		return null;
	}
	
	public QueryManager getQueryManager() {
	    return m_queryMgr;
	}
	
    /**
     * <p>setQueryManager</p>
     *
     * @param queryMgr a {@link org.opennms.netmgt.linkd.QueryManager} object.
     */
    public void setQueryManager(QueryManager queryMgr) {
        m_queryMgr = queryMgr;
        // TODO: Circular; refactor so this can be set in spring
        queryMgr.setLinkd(this);
    }

	/**
	 * <p>getScheduler</p>
	 *
	 * @return a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
	 */
	public Scheduler getScheduler() {
		return m_scheduler;
	}

	/**
	 * <p>setScheduler</p>
	 *
	 * @param scheduler a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
	 */
	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}
	
	/**
	 * <p>getLinkdConfig</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.LinkdConfig} object.
	 */
	public LinkdConfig getLinkdConfig() {
		return m_linkdConfig;
	}

	/**
	 * <p>setLinkdConfig</p>
	 *
	 * @param config a {@link org.opennms.netmgt.config.LinkdConfig} object.
	 */
	public void setLinkdConfig(final LinkdConfig config) {
		m_linkdConfig = config;
	}

    /**
     * @return the eventForwarder
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * @param eventForwarder the eventForwarder to set
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        this.m_eventForwarder = eventForwarder;
    }
}
