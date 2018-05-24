package org.opennms.hq.dao;

import java.io.IOException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.control.server.session.ControlSchedule;
import org.hyperic.hq.scheduler.ScheduleValue;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class ControlScheduleDAO extends AbstractDaoHibernate<ControlSchedule, Integer> implements OnmsDao<ControlSchedule,Integer> {

    public ControlScheduleDAO() {
        super(ControlSchedule.class);
    }

    ControlSchedule create(AppdefEntityID entityId, String subject, String action,
                           ScheduleValue schedule, long nextFire, String triggerName,
                           String jobName, String jobOrderData) {
        ControlSchedule s = new ControlSchedule();

        try {
            s.setEntityId(entityId.getId());
            s.setEntityType(new Integer(entityId.getType()));
            s.setSubject(subject);
            s.setScheduleValue(schedule);
            s.setNextFireTime(nextFire);
            s.setTriggerName(triggerName);
            s.setJobName(jobName);
            s.setJobOrderData(jobOrderData);
            s.setAction(action);
            save(s);
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<ControlSchedule> findByFireTime(boolean asc) {/*
        return createCriteria().addOrder(
            asc ? Order.asc("nextFireTime") : Order.desc("nextFireTime")).list();
    */
    return null;	
    }

    @SuppressWarnings("unchecked")
    public Collection<ControlSchedule> findByEntity(int type, int id) {
        return createFindByEntity(type, id).list();
    }

    @SuppressWarnings("unchecked")
    public Collection<ControlSchedule> findByEntityAction(int type, int id, boolean asc) {
        return createFindByEntity(type, id).addOrder(
            asc ? Order.asc("action") : Order.desc("action")).list();
    }

    @SuppressWarnings("unchecked")
    public Collection<ControlSchedule> findByEntityFireTime(int type, int id, boolean asc) {
        return createFindByEntity(type, id).addOrder(
            asc ? Order.asc("nextFireTime") : Order.desc("nextFireTime")).list();
    }

    private Criteria createFindByEntity(int type, int id) {/*
        return createCriteria().add(Expression.eq("entityId", new Integer(id))).add(
            Expression.eq("entityType", new Integer(type)));
    */
    return null;	
    }

   
}
