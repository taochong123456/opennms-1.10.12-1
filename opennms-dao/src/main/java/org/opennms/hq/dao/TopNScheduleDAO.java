package org.opennms.hq.dao;

import org.hyperic.hq.measurement.server.session.TopNSchedule;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class TopNScheduleDAO extends AbstractDaoHibernate<TopNSchedule, Integer> implements OnmsDao<TopNSchedule,Integer> {

    protected TopNScheduleDAO() {
        super(TopNSchedule.class);
    }

    @Override
    public void save(TopNSchedule entity) {
        super.save(entity);
        getSession().flush();
    }

}
