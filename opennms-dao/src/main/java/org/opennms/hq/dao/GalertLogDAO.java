/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2009], Hyperic, Inc.
 * This file is part of HQ.
 *
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.opennms.hq.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hyperic.hibernate.PageInfo;
import org.hyperic.hq.authz.server.session.ResourceGroup;
import org.hyperic.hq.authz.shared.AuthzConstants;
import org.hyperic.hq.events.AlertSeverity;
import org.hyperic.hq.galerts.server.session.GalertAuxLog;
import org.hyperic.hq.galerts.server.session.GalertDef;
import org.hyperic.hq.galerts.server.session.GalertLog;
import org.hyperic.hq.galerts.server.session.GalertLogSortField;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class GalertLogDAO extends AbstractDaoHibernate<GalertLog, Integer> implements OnmsDao<GalertLog,Integer> {
    GalertLogDAO() {
        super(GalertLog.class);
    }

    void save(GalertAuxLog log) {
        getSession().saveOrUpdate(log);
    }

    @SuppressWarnings("unchecked")
	public
    List<GalertLog> findAll(ResourceGroup g) {
        String sql = "from GalertLog l where l.alertDef.group = :group " + "order by l.timestamp";

        return (List<GalertLog>) getSession().createQuery(sql).setParameter("group", g).list();
    }

    public GalertLog findLastByDefinition(GalertDef def, boolean fixed) {/*
        return (GalertLog) createCriteria().add(Expression.eq("alertDef", def)).add(
            Expression.eq("fixed", new Boolean(fixed))).addOrder(Order.desc("timestamp"))
            .setMaxResults(1).uniqueResult();
    */
    return null	;
    }

    public PageList<GalertLog> findByTimeWindow(ResourceGroup g, long begin, long end, PageControl pc) {/*
        final String tsProp = "timestamp";
        Integer count = (Integer) createCriteria().createAlias("alertDef", "d").add(
            Restrictions.eq("d.group", g)).add(Restrictions.ge(tsProp, new Long(begin))).add(
            Restrictions.le(tsProp, new Long(end))).setProjection(Projections.rowCount())
            .uniqueResult();

        if (count.intValue() > 0) {
            Criteria crit = createCriteria().createAlias("alertDef", "d").add(
                Restrictions.eq("d.group", g)).add(Restrictions.ge(tsProp, new Long(begin))).add(
                Restrictions.le(tsProp, new Long(end))).addOrder(
                pc.isDescending() ? Order.desc(tsProp) : Order.asc(tsProp));

            return getPagedResult(crit, count, pc);
        }

        return new PageList<GalertLog>();
    */
    return null;	
    }

    @SuppressWarnings("unchecked")
	public
    List<GalertLog> findUnfixedByTimeWindow(ResourceGroup g, long begin, long end) {/*
        final String tsProp = "timestamp";
        return (List<GalertLog>) createCriteria().createAlias("alertDef", "d").add(
            Restrictions.eq("fixed", Boolean.FALSE)).add(Restrictions.eq("d.group", g)).add(
            Restrictions.ge(tsProp, new Long(begin))).add(Restrictions.le(tsProp, new Long(end)))
            .list();
    */
    return null;	
    }

    @SuppressWarnings("unchecked")
    List<GalertLog> findByCreateTime(long startTime, long endTime, int count) {/*
        Criteria criteria = createCriteria().add(
            Expression.between("timestamp", new Long(startTime), new Long(endTime))).addOrder(
            Order.desc("timestamp"));
        if (count >= 0) {
            criteria.setMaxResults(count);
        }
        return (List<GalertLog>) criteria.list();
    */
    return null;	
    }

    @SuppressWarnings("unchecked")
	public
    List<GalertLog> findByCreateTimeAndPriority(Integer subjectId, long begin, long end,
                                                AlertSeverity severity, boolean inEsc,
                                                boolean notFixed, Integer groupId,
                                                Integer galertDefId, PageInfo pageInfo) {/*
        GalertLogSortField sort = (GalertLogSortField) pageInfo.getSort();
        String op = AuthzConstants.groupOpViewResourceGroup;
        String sql = PermissionManagerFactory.getInstance().getGroupAlertsHQL(inEsc, notFixed,
            groupId, galertDefId) +
                     " order by " +
                     sort.getSortString("a", "d", "g") +
                     (pageInfo.isAscending() ? "" : " DESC");

        if (!sort.equals(GalertLogSortField.DATE)) {
            sql += ", " + GalertLogSortField.DATE.getSortString("a", "d", "g") + " DESC";
        }

        Query q = getSession().createQuery(sql).setLong("begin", begin).setLong("end", end)
            .setInteger("priority", severity.getCode());

        if (sql.indexOf("subj") > 0) {
            q.setInteger("subj", subjectId.intValue()).setParameter("op", op);
        }

        return (List<GalertLog>) pageInfo.pageResults(q).list();
    */
    return null;	
    }

    public void removeAll(ResourceGroup g) {
        String sql = "delete from GalertLog l where l.alertDef.group = :group";

        getSession().createQuery(sql).setParameter("group", g).executeUpdate();
    }

    void removeAll(GalertDef d) {
        String sql = "delete from GalertLog l where l.alertDef = :def";

        getSession().createQuery(sql).setParameter("def", d).executeUpdate();
    }

    public Integer countAlerts(ResourceGroup g) {/*
        return (Integer) createCriteria().createAlias("alertDef", "d").add(
            Restrictions.eq("d.group", g)).setProjection(Projections.rowCount()).uniqueResult();
    */
    return null;	
    }
}
