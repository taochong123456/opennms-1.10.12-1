package org.opennms.hq.dao;

import java.io.IOException;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.autoinventory.AIPlatform;
import org.hyperic.hq.autoinventory.AISchedule;
import org.hyperic.hq.common.ApplicationException;
import org.hyperic.hq.scheduler.ScheduleValue;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

public class AIScheduleDAO extends AbstractDaoHibernate<AISchedule, Integer> implements OnmsDao<AISchedule,Integer>  {
    public AIScheduleDAO() {
        super(AISchedule.class);
    }

    public AISchedule create(AppdefEntityID entityId, String subject, String scanName,
                             String scanDesc, ScheduleValue schedule, long nextFire,
                             String triggerName, String jobName) throws ApplicationException

    {
        try {
            AISchedule s = new AISchedule();
            s.setEntityId(entityId.getId());
            s.setEntityType(new Integer(entityId.getType()));
            s.setSubject(subject);
            s.setScheduleValue(schedule);
            s.setNextFireTime(nextFire);
            s.setTriggerName(triggerName);
            s.setJobName(jobName);
            s.setJobOrderData(null);
            s.setScanName(scanName);
            s.setScanDesc(scanDesc);
            save(s);
            return s;
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage());
        }
    }

    public AISchedule findByScanName(String name) {
        String sql = "from AISchedule where scanName=?";
        return (AISchedule) getSession().createQuery(sql).setString(0, name).uniqueResult();
    }

    /**
     * @deprecated use findByEntityFireTime()
     * @param type
     * @param id
     * @return
     */
    public Collection findByEntityFireTimeDesc(int type, int id) {
        return findByEntityFireTime(type, id, false);
    }

    /**
     * @deprecated use findByEntityFireTime()
     * @param type
     * @param id
     * @return
     */
    public Collection findByEntityFireTimeAsc(int type, int id) {
        return findByEntityFireTime(type, id, true);
    }

    public Collection<AISchedule> findByEntityFireTime(int type, int id, boolean asc) {
        String sql = "from AISchedule where entityId=? and entityType=? " +
                     "order by nextFireTime " + (asc ? "asc" : "desc");
        return getSession().createQuery(sql).setInteger(0, id).setInteger(1, type).list();
    }

    public Collection<AISchedule> findByEntityScanName(int type, int id, boolean asc) {
        String sql = "from AISchedule where entityId=? and entityType=? " + "order by scanName " +
                     (asc ? "asc" : "desc");
        return getSession().createQuery(sql).setInteger(0, id).setInteger(1, type).list();
    }
}
