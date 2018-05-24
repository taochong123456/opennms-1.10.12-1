package org.opennms.hq.dao;

import org.hyperic.hq.appdef.server.session.ApplicationType;
import org.hyperic.hq.appdef.server.session.ServiceType;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class ApplicationTypeDAO extends AbstractDaoHibernate<ApplicationType, Integer> implements OnmsDao<ApplicationType,Integer> {

    private ServiceTypeDAO serviceTypeDAO;

    public ApplicationTypeDAO(ServiceTypeDAO serviceTypeDAO) {
        super(ApplicationType.class);
        this.serviceTypeDAO = serviceTypeDAO;
    }

    public ApplicationType create(String name, String desc) {
        ApplicationType type = new ApplicationType();
        type.setName(name);
        type.setDescription(desc);
        save(type);
        return type;
    }

    public ApplicationType findByName(String name) {
        String sql = "from ApplicationType where sortName=?";
        return (ApplicationType) getSession().createQuery(sql).setString(0, name.toUpperCase())
            .uniqueResult();
    }

    public boolean supportsServiceType(ApplicationType at, Integer stPK) {
        if (at.getServiceTypes() == null) {
            return false;
        }
        ServiceType serviceType = serviceTypeDAO.get(stPK);
        return at.getServiceTypes().contains(serviceType);
    }
}
