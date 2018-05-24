package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.jmx.connectors.Jsr160ConnectionWrapper;

/**
 * zookeeper collection utill
 * 
 * **/
public class ZookeeperDataCollector {
	static ThreadCategory log = ThreadCategory
			.getInstance(ZookeeperDataCollector.class);
	private String mode = null;
	private Map<String, String> zookeeperMap = new HashMap<String, String>();
	private Map<String, List<String>> clusterTableAttributes = new HashMap<String, List<String>>();
	private JMXConnector connector = null;
	private MBeanServerConnection mbeanSC = null;

	static String[] operatingSystem = { "FreePhysicalMemorySize",
			"FreeSwapSpaceSize", "TotalPhysicalMemorySize",
			"TotalSwapSpaceSize", "CommittedVirtualMemorySize" };
	static String[] Threading = { "DaemonThreadCount", "PeakThreadCount",
			"ThreadCount", "TotalStartedThreadCount" };
	static String[] runTime = { "BootClassPath", "ClassPath", "SpecVendor",
			"SpecVersion", "VmName", "VmVendor", "Uptime" };
	static String[] data = { "committed", "init", "max", "used" };
	static String[] memories = { "HeapMemoryUsage", "NonHeapMemoryUsage" };
	static String[] configAttributes = { "InitLimit", "MaxClientCnxnsPerHost",
			"MaxSessionTimeout", "MinSessionTimeout", "Name", "QuorumAddress",
			"StartTime", "State", "SyncLimit", "Tick", "TickTime" };
	static String[] metrices = { "AvgRequestLatency", "ClientPort",
			"CurrentZxid", "MaxRequestLatency", "MinRequestLatency",
			"NumAliveConnections", "OutstandingRequests", "PacketsReceived",
			"PacketsSent", "Version" };
	static String[] inMemoryDataTreeMetrics = { "LastZxid", "NodeCount",
			"WatchCount" };

	public void startDatacollection(Jsr160ConnectionWrapper connection) {
		try {
			setConnection(connection);
			if (this.connector != null) {
//				getZookeeperCommonMetrics();
				ObjectName objName = checkStandalone();
				if (objName != null) {
					getZookeeperConfigurationMetrics(String.valueOf(objName));
					getZookeeperMetrics(String.valueOf(objName),
							String.valueOf(objName) + ",name1=InMemoryDataTree");
				} else {
					getZookeeperPerformanceData();
				}
			}
		} catch (Exception e) {
			log.error("collector zookeeper data error:" + e);
		}
	}

	private ObjectName checkStandalone() {
		ObjectName objectName = null;
		try {
			Set<ObjectInstance> replica = this.mbeanSC
					.queryMBeans(new ObjectName(
							"org.apache.ZooKeeperService:name0=*"), null);
			for (ObjectInstance obj : replica) {
				String type = (String) obj.getObjectName().getKeyPropertyList()
						.get("name0");
				if ((type != null) && (type.contains("StandaloneServer"))) {
					objectName = obj.getObjectName();
					this.mode = "StandaloneServer";
					return objectName;
				}
			}
		} catch (Exception e) {
			log.error("zookeeper checkStandalone error:" + e);
		}
		return objectName;
	}

	public void discover() {
		try {
			ObjectName metric_query = new ObjectName(
					"org.apache.ZooKeeperService:name0=ReplicatedServer_id*,name1=replica.*");
			Set<ObjectInstance> set = this.mbeanSC.queryMBeans(metric_query,
					null);
			List liveNodes = new ArrayList();
			for (ObjectInstance obj : set) {
				String classname = obj.getClassName();
				ObjectName clusterName = obj.getObjectName();
				String QuorumAddress = (String) getValue(this.mbeanSC,
						clusterName, "QuorumAddress");
				if (!classname
						.equalsIgnoreCase("org.apache.zookeeper.server.quorum.LocalPeerBean")) {
					String[] quorumArry = QuorumAddress.split(":");
					liveNodes.add(quorumArry[0]);
				}
			}

		} catch (Exception e) {
			log.error("zookeeper discovery error:" + e);
		}
	}

	private Object getValue(MBeanServerConnection mbeanSC, ObjectName oname,
			String attrName) {
		Object toRet = null;
		try {
			toRet = mbeanSC.getAttribute(oname, attrName);
		} catch (Exception ee) {
			log.error("zookeeper get vaule error:" + ee);
		} catch (Error err) {
			log.error("zookeeper get vaule error:" + err);
		}
		return toRet;
	}

	private void getZookeeperCommonMetrics() {
		try {
			getAttributes("java.lang:type=OperatingSystem", operatingSystem);
			getAttributes("java.lang:type=Threading", Threading);
			getAttributes("java.lang:type=Runtime", runTime);
			collectCompositedata(data, memories, new ObjectName(
					"java.lang:type=Memory"));
		} catch (Exception exe) {
			log.error("zookeeper get vaule error:" + exe);
		}
	}

	private void getZookeeperPerformanceData() {
		try {
			Set replica = this.mbeanSC.queryMBeans(new ObjectName(
					"org.apache.ZooKeeperService:name0=ReplicatedServer_id*"),
					null);
			ObjectName replicaObjectName = null;
			String replicaPID = null;
			String replicaObjNameInString = null;
			Iterator it = replica.iterator();
			if (it.hasNext()) {
				ObjectInstance replicaObject = (ObjectInstance) it.next();
				replicaObjectName = replicaObject.getObjectName();
				replicaObjNameInString = String.valueOf(replicaObjectName);
				replicaPID = replicaObjNameInString
						.substring(replicaObjNameInString.length() - 1);
			}

			Set childReplica = this.mbeanSC.queryMBeans(new ObjectName(
					replicaObjNameInString + ",name1=replica." + replicaPID
							+ ",name2=*"), null);
			ObjectName childReplicaObjectName = null;
			Hashtable attributeList = null;
			Iterator i = childReplica.iterator();
			if (i.hasNext()) {
				ObjectInstance childReplicaObject = (ObjectInstance) i.next();
				childReplicaObjectName = childReplicaObject.getObjectName();
				attributeList = childReplicaObjectName.getKeyPropertyList();
				if ((attributeList != null)
						&& (attributeList.get("name2") != null)) {
					this.mode = ((String) attributeList.get("name2"));
				}
			}

			String commonAttributeObjectName = replicaObjNameInString
					+ ",name1=replica." + replicaPID;
			getZookeeperConfigurationMetrics(commonAttributeObjectName);

			String metricObjectName = null;
			String inMemoryDataTreeObjectName = null;
			if ((this.mode != null) && (this.mode.equalsIgnoreCase("Leader"))) {
				metricObjectName = replicaObjNameInString + ",name1=replica."
						+ replicaPID + ",name2=" + this.mode;
				inMemoryDataTreeObjectName = metricObjectName
						+ ",name3=InMemoryDataTree";
				getZookeeperMetrics(metricObjectName,
						inMemoryDataTreeObjectName);
			}
			if ((this.mode != null) && (this.mode.equalsIgnoreCase("Follower"))) {
				metricObjectName = replicaObjNameInString + ",name1=replica."
						+ replicaPID + ",name2=" + this.mode;
				inMemoryDataTreeObjectName = metricObjectName
						+ ",name3=InMemoryDataTree";
				getZookeeperMetrics(metricObjectName,
						inMemoryDataTreeObjectName);
			}
			if ((this.mode != null)
					&& (this.mode.equalsIgnoreCase("LeaderElection"))) {
				metricObjectName = replicaObjNameInString + ",name1=replica."
						+ replicaPID + ",name2=" + this.mode;
				getZookeeperMetrics(metricObjectName,
						inMemoryDataTreeObjectName);
			}
		} catch (Exception e) {
			log.error("zookeeper getZookeeperPerformanceData error:" + e);
		}
	}

	private void getZookeeperConfigurationMetrics(
			String commonAttributeObjectName) {
		try {
			getAttributes(commonAttributeObjectName, configAttributes);
		} catch (Exception e) {
			log.error("zookeeper getZookeeperConfigurationMetrics error:" + e);
		}
	}

	private void getZookeeperMetrics(String metricObjectName,
			String inMemoryDataTreeObjectName) {
		try {
			if (this.mode.equalsIgnoreCase("StandaloneServer")) {
				getAttributes(metricObjectName, metrices);
				getAttributes(inMemoryDataTreeObjectName,
						inMemoryDataTreeMetrics);
				getOperationData(inMemoryDataTreeObjectName);
			} else if (this.mode.equalsIgnoreCase("LeaderElection")) {
				String electionStartTime = (String) getValue(this.mbeanSC,
						new ObjectName(metricObjectName), "StartTime");
				if (electionStartTime != null) {
					this.zookeeperMap.put("ElectionStartTime",
							electionStartTime);
				}
			} else {
				getAttributes(metricObjectName, metrices);
				getAttributes(inMemoryDataTreeObjectName,
						inMemoryDataTreeMetrics);
				getOperationData(inMemoryDataTreeObjectName);
				getClusterTableAttributes();
			}
		} catch (Exception e) {
			log.error("zookeeper getZookeeperMetrics error:" + e);
		}
	}

	private void getOperationData(String inMemoryDataTreeObjectName) {
		try {
			Integer count = Integer.valueOf(-1);
			Long approximateDataSize = Long.valueOf(0L);
			count = (Integer) this.mbeanSC.invoke(new ObjectName(
					inMemoryDataTreeObjectName), "countEphemerals",
					new Object[0], new String[0]);
			approximateDataSize = (Long) this.mbeanSC.invoke(new ObjectName(
					inMemoryDataTreeObjectName), "approximateDataSize",
					new Object[0], new String[0]);
			this.zookeeperMap.put("approximateDataSize",
					String.valueOf(approximateDataSize));
			this.zookeeperMap.put("countEphemerals", count + "");
		} catch (Exception e) {
			log.error("zookeeper getOperationData error:" + e);
		}
	}

	private void getClusterTableAttributes() {
		ObjectName metric_query = null;
		try {
			metric_query = new ObjectName(
					"org.apache.ZooKeeperService:name0=ReplicatedServer_id*,name1=replica.*");
			Set<ObjectInstance> set = this.mbeanSC.queryMBeans(metric_query,
					null);
			for (ObjectInstance obj : set) {
				String classname = obj.getClassName();
				ObjectName clusterName = obj.getObjectName();
				if (!classname
						.equalsIgnoreCase("org.apache.zookeeper.server.quorum.LocalPeerBean"))
					try {
						String ReplicaName = (String) getValue(this.mbeanSC,
								clusterName, "Name");
						String QuorumAddress = (String) getValue(this.mbeanSC,
								clusterName, "QuorumAddress");
						String[] quorumArry = QuorumAddress.split(":");
						ArrayList<String> arrList = new ArrayList<String>();
						arrList.add(quorumArry[0]);//node3/192.168.239.113:2888
						arrList.add(ReplicaName);
						arrList.add(QuorumAddress);
						clusterTableAttributes.put(ReplicaName, arrList);
					} catch (Exception exe) {
						log.error("zookeeper getClusterTableAttributes error:"
								+ exe);
					}
			}
		} catch (Exception exe) {
			log.error("zookeeper get vaule error:" + exe);
		}

	}

	private AttributeList getAttributes(String objectName, String[] attrs) {
		AttributeList list = null;
		try {
			ObjectName obj = new ObjectName(objectName);
			list = this.mbeanSC.getAttributes(obj, attrs);
			for (int i = 0; i < list.size(); i++) {
				Attribute att = (Attribute) list.get(i);
				if (att.getName().toString().equalsIgnoreCase("State")) {
					this.zookeeperMap.put(att.getName().toString(), this.mode);
				} else {
					this.zookeeperMap.put(att.getName().toString(), att
							.getValue().toString());
				}
			}
		} catch (Exception e) {
			log.error("zookeeper get attributes error:" + e);
		}
		return list;
	}

	private void collectCompositedata(String[] data, String[] memories,
			ObjectName obj) {
		try {
			for (int i = 0; i < memories.length; i++) {
				CompositeData composite = (CompositeData) this.mbeanSC
						.getAttribute(obj, memories[i]);
				for (int j = 0; j < data.length; j++) {
					this.zookeeperMap.put(memories[i] + "_" + data[j],
							composite.get(data[j]).toString());
				}
			}
		} catch (Exception e) {
			log.error("zookeeper collect collectCompositedata error:" + e);
		}
	}

	public void setConnection(Jsr160ConnectionWrapper connection) {
		connector = connection.getConnector();
		mbeanSC = connection.getMBeanServer();
	}

	public Map<String, List<String>> getClusterTableAttributeValues() {
		return clusterTableAttributes;
	}

	public Map<String, String> getBasicInfo() {
		return zookeeperMap;
	}

}