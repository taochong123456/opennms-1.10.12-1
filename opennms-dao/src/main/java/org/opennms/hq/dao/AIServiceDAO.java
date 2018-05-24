package org.opennms.hq.dao;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.shared.AIServiceValue;
import org.hyperic.hq.autoinventory.AIServer;
import org.hyperic.hq.autoinventory.AIService;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class AIServiceDAO extends AbstractDaoHibernate<AIService, Integer> implements OnmsDao<AIService,Integer>{
    public AIServiceDAO() {
        super(AIService.class);
    }

    public AIService create(AIServiceValue sv) {
        AIService s = new AIService(sv);
        save(s);
        return s;
    }

    public Collection<AIService> findByType(String stName) {
        String sql = "from AIService where serviceTypeName=?";
        return getSession().createQuery(sql).setString(0, stName).list();
    }

    public AIService findByName(String name) {
        String sql = "from AIService where lower(name)=?";
        return (AIService) getSession().createQuery(sql).setString(0, name.toLowerCase())
            .uniqueResult();
    }

}
