package org.opennms.hq.dao;

import java.util.Collection;

import org.hyperic.hq.bizapp.server.session.UpdateStatus;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
public class UpdateStatusDAO extends AbstractDaoHibernate<UpdateStatus, Integer> implements OnmsDao<UpdateStatus,Integer>
{
    public UpdateStatusDAO() {
        super(UpdateStatus.class);
    }

    UpdateStatus get() {
        Collection vals = findAll();

        if (vals.isEmpty()) {
            return null;
        }

        return (UpdateStatus)vals.iterator().next();
    }

    public void save(UpdateStatus status) {
        super.save(status);
    }
}
