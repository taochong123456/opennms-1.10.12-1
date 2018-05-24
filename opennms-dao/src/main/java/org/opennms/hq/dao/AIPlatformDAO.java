package org.opennms.hq.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.shared.AIIpValue;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AIServerValue;
import org.hyperic.hq.autoinventory.AIIp;
import org.hyperic.hq.autoinventory.AIPlatform;
import org.hyperic.hq.autoinventory.AIServer;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


public class AIPlatformDAO extends AbstractDaoHibernate<AIPlatform, Integer> implements OnmsDao<AIPlatform,Integer> {

    public AIPlatformDAO() {
        super(AIPlatform.class);
    }

    private void fixdata(AIPlatform p) {
        String name = p.getName();
        if (name == null || "".equals(name.trim())) {
            p.setName(p.getFqdn());
        }
    }

    public AIPlatform create(AIPlatformValue apv) {
        AIPlatform p = new AIPlatform(apv);
        fixdata(p);

        // handle IP's in the value object
        // Since this is a new value object
        // I'll assume we need add all ips
        AIIpValue[] newIPs = apv.getAIIpValues();
        Collection ipSet = new HashSet();
        p.setAIIps(ipSet);

        for (int i = 0; i < newIPs.length; i++) {
            AIIpValue ipVal = newIPs[i];
            AIIp ip = new AIIp(ipVal);
            ip.setAIPlatform(p);
            ipSet.add(ip);
        }

        // handle Server's in the value object
        // Since this is a new value object
        // I'll assume we need add all servers
        AIServerValue[] newServers = apv.getAIServerValues();
        Collection serverSet = new HashSet();
        p.setAIServers(serverSet);

        // XXX hack around bug in xcraplet's generated
        // removeAllXXX methods (they actually don't remove anything)
        // The AIQueueManagerImpl.queue method relies on this working.
        HashSet xdocletServerHackSet = new HashSet();
        for (int i = 0; i < newServers.length; i++) {
            AIServerValue serverVal = newServers[i];
            AIServer s = new AIServer(serverVal);
            s.setAIPlatform(p);
            serverSet.add(s);
        }
        save(p);
        return p;
    }

    public Collection<AIPlatform> findAllNotIgnored() {
        String sql = "from AIPlatform where ignored=false and " + "lastApproved < modifiedTime "
                     + "order by name";
        return getSession().createQuery(sql).list();
    }

    public Collection<AIPlatform> findAllNotIgnoredIncludingProcessed() {
        String sql = "from AIPlatform where ignored=false order by name";
        return getSession().createQuery(sql).list();
    }

    public Collection<AIPlatform> findAllIncludingProcessed() {
        String sql = "from AIPlatform order by name";
        return getSession().createQuery(sql).list();
    }

    public AIPlatform findByFQDN(String fqdn) {
        String sql = "from AIPlatform where fqdn=?";
        return (AIPlatform) getSession().createQuery(sql).setString(0, fqdn).uniqueResult();
    }

    public AIPlatform findByCertDN(String dn) {
        String sql = "from AIPlatform where certdn=?";
        return (AIPlatform) getSession().createQuery(sql).setString(0, dn).uniqueResult();
    }

    public AIPlatform findByAgentToken(String token) {
        String sql = "from AIPlatform where agentToken=?";
        return (AIPlatform) getSession().createQuery(sql).setString(0, token).uniqueResult();
    }

    public AIPlatform findByName(String name) {
        String sql = "from AIPlatform where lower(name)=?";
        return (AIPlatform) getSession().createQuery(sql).setString(0, name.toUpperCase())
            .uniqueResult();
    }

    /**
     * legacy method
     * 
     * @param aiplatform The new AI data to update this platform with.
     * @param updateServers If true, servers will be updated too.
     */
    public void updateQueueState(AIPlatform aip, AIPlatformValue aiplatform, boolean updateServers,
                                 boolean isApproval, boolean isReport) {
        // reassociate platform
        aip =get(aip.getId());

        long nowTime = System.currentTimeMillis();
        aip.setFqdn(aiplatform.getFqdn());
        aip.setName(aiplatform.getName());
        aip.setPlatformTypeName(aiplatform.getPlatformTypeName());
        aip.setAgentToken(aiplatform.getAgentToken());
        aip.setQueueStatus(aiplatform.getQueueStatus());
        aip.setDescription(aiplatform.getDescription());
        aip.setDiff(aiplatform.getDiff());
        aip.setCpuCount(aiplatform.getCpuCount());
        aip.setCustomProperties(aiplatform.getCustomProperties());
        aip.setProductConfig(aiplatform.getProductConfig());
        aip.setMeasurementConfig(aiplatform.getMeasurementConfig());
        aip.setControlConfig(aiplatform.getControlConfig());

        if (isReport || isApproval) {
            if (isApproval) {
                aiplatform.setLastApproved(new Long(nowTime + 1));
            } else {
                Long lastApproved = aiplatform.getLastApproved();
                if (lastApproved == null)
                    lastApproved = new Long(0);
                aiplatform.setLastApproved(lastApproved);
            }
            aip.setLastApproved(aiplatform.getLastApproved());
        }

        // Sanitize name
        if (aip.getName() == null) {
            aip.setName(aip.getFqdn());
        }
        updateIpSet(aip, aiplatform);
        if (updateServers) {
            updateServerSet(aip, aiplatform);
        }
        save(aip);
    }

    private void updateIpSet(AIPlatform p, AIPlatformValue aiplatform) {
        List newIPs = new ArrayList(Arrays.asList(aiplatform.getAIIpValues()));
        Collection ipSet = p.getAIIps();
        for (Iterator i = ipSet.iterator(); i.hasNext();) {
            AIIp qip = (AIIp) i.next();
            AIIpValue aiip = findAndRemoveAIIp(newIPs, qip.getAddress());
            if (aiip == null) {
                i.remove();
            } else {
                boolean qIgnored = qip.isIgnored();
                qip.setQueueStatus(new Integer(aiip.getQueueStatus()));
                qip.setDiff(aiip.getDiff());
                qip.setIgnored(aiip.getIgnored());
                qip.setAddress(aiip.getAddress());
                qip.setMacAddress(aiip.getMACAddress());
                qip.setNetmask(aiip.getNetmask());
                qip.setIgnored(qIgnored);
            }
        }

        // Add remaining IPs
        for (Iterator i = newIPs.iterator(); i.hasNext();) {
            AIIpValue aiip = (AIIpValue) i.next();
            AIIp ip = new AIIp();
            ip.setQueueStatus(new Integer(aiip.getQueueStatus()));
            ip.setDiff(aiip.getDiff());
            ip.setIgnored(aiip.getIgnored());
            ip.setAddress(aiip.getAddress());
            ip.setMacAddress(aiip.getMACAddress());
            ip.setNetmask(aiip.getNetmask());
            ip.setAIPlatform(p);
            ipSet.add(ip);
        }
    }

    private AIIpValue findAndRemoveAIIp(List ips, String addr) {
        AIIpValue aiip;
        for (int i = 0; i < ips.size(); i++) {
            aiip = (AIIpValue) ips.get(i);
            if (aiip.getAddress().equals(addr)) {
                return (AIIpValue) ips.remove(i);
            }
        }
        return null;
    }

    private Map getServersMap(List servers) {
        Map rtn = new HashMap();
        for (Iterator it = servers.iterator(); it.hasNext();) {
            AIServerValue server = (AIServerValue) it.next();
            rtn.put(server.getAutoinventoryIdentifier(), server);
        }
        return rtn;
    }

    private void updateServerSet(AIPlatform p, AIPlatformValue aiplatform) {
        Map newServers = getServersMap(Arrays.asList(aiplatform.getAIServerValues()));
        // XXX, scottmf need to get Platform from AIPlatform
        // then find a way to correlate the old aiid with the new aiid for
        // each server
        Collection serverSet = p.getAIServers();
        for (Iterator i = serverSet.iterator(); i.hasNext();) {
            AIServer qserver = (AIServer) i.next();
            String aiid = qserver.getAutoinventoryIdentifier();
            AIServerValue aiserver = (AIServerValue) newServers.remove(aiid);
                        
            if (aiserver != null) {
                // keep the user specified ignored value
                boolean qIgnored = qserver.getIgnored();
                qserver.setAIServerValue(aiserver);
                qserver.setIgnored(qIgnored);
            }
        }

        for (Iterator i = newServers.values().iterator(); i.hasNext();) {
            AIServerValue aiserver = (AIServerValue) i.next();
            AIServer ais = new AIServer();
            ais.setAIServerValue(aiserver);
            p.addAIServer(ais);
        }
    }
}
