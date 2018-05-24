package org.opennms.hq.dao;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.shared.AIServerValue;
import org.hyperic.hq.autoinventory.AIPlatform;
import org.hyperic.hq.autoinventory.AIServer;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class AIServerDAO extends AbstractDaoHibernate<AIServer, Integer> implements OnmsDao<AIServer,Integer> {
    public AIServerDAO() {
        super(AIServer.class);
    }

    public AIServer create(AIServerValue server) {
        AIServer as = new AIServer();

        as.setInstallPath(server.getInstallPath());
        as.setAutoinventoryIdentifier(server.getAutoinventoryIdentifier());
        as.setServicesAutomanaged(server.getServicesAutomanaged());
        as.setName(server.getName());
        as.setQueueStatus(server.getQueueStatus());
        as.setDescription(server.getDescription());
        as.setDiff(server.getDiff());
        as.setIgnored(server.getIgnored());
        as.setServerTypeName(server.getServerTypeName());
        as.setProductConfig(server.getProductConfig());
        as.setMeasurementConfig(server.getMeasurementConfig());
        as.setControlConfig(server.getControlConfig());
        as.setCustomProperties(server.getCustomProperties());
        save(as);
        return as;
    }

    public AIServer findByName(String name) {
        String sql = "from AIServer where name=?";
        return (AIServer) getSession().createQuery(sql).setString(0, name).uniqueResult();
    }

    public Collection findByPlatformId(Integer platformid) {
        String sql = "from AIServer where aIPlatform.id=?";
        return getSession().createQuery(sql).setInteger(0, platformid.intValue()).list();
    }

}
