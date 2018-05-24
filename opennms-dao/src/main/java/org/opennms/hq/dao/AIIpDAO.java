package org.opennms.hq.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.shared.AIIpValue;
import org.hyperic.hq.autoinventory.AIHistory;
import org.hyperic.hq.autoinventory.AIIp;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class AIIpDAO extends AbstractDaoHibernate<AIIp, Integer> implements OnmsDao<AIIp,Integer> {
    public AIIpDAO() {
        super(AIIp.class);
    }

    public AIIp create(AIIpValue ipv) {
        AIIp ip = new AIIp();
        ip.setAddress(ipv.getAddress());
        ip.setNetmask(ipv.getNetmask());
        ip.setMacAddress(ipv.getMACAddress());
        ip.setQueueStatus(new Integer(ipv.getQueueStatus()));
        ip.setDiff(ipv.getDiff());
        ip.setIgnored(ipv.getIgnored());
        save(ip);
        return ip;
    }

    public List<AIIp> findByAddress(String addr) {
        String sql = "from AIIp where address=?";
        return getSession().createQuery(sql).setString(0, addr).list();
    }

    public List<AIIp> findByMACAddress(String addr) {
        String sql = "from AIIp where macAddress=?";
        return getSession().createQuery(sql).setString(0, addr).list();
    }
}
