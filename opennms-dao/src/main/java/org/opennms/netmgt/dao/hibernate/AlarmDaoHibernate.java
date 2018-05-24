/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>AlarmDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmDaoHibernate extends AbstractDaoHibernate<OnmsAlarm, Integer> implements AlarmDao {
	
	/**
	 * <p>Constructor for AlarmDaoHibernate.</p>
	 */
	public AlarmDaoHibernate() {
		super(OnmsAlarm.class);
	}

    /** {@inheritDoc} */
    public OnmsAlarm findByReductionKey(String reductionKey) {
        String hql = "from OnmsAlarm as alarms where alarms.reductionKey = ?";
        return super.findUnique(hql, reductionKey);
    }

    public List<AlarmSummary> getNodeAlarmSummaries() {
        return findObjects(
            AlarmSummary.class,
            "SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary(node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), count(*)) " +
            "FROM OnmsAlarm AS alarm " +
            "LEFT JOIN alarm.node AS node " +
            "WHERE node.id IS NOT NULL AND alarm.alarmAckTime IS NULL AND alarm.severity > 3 " +
            "GROUP BY node.id, node.label " +
            "ORDER BY min(alarm.lastEventTime) DESC, node.label ASC"
        );
    }

	@Override
	public List<HeatMapElement> getHeatMapItemsForEntity(
			final String entityNameColumn, final String entityIdColumn,
			boolean processAcknowledgedAlarms, final String restrictionColumn,
			final String restrictionValue, String... groupByColumns) {

        String grouping = "";

        if (groupByColumns != null && groupByColumns.length > 0) {
            for (String groupByColumn : groupByColumns) {
                if (!"".equals(grouping)) {
                    grouping += ", ";
                }

                grouping += groupByColumn;
            }
        } else {
            grouping = entityNameColumn + ", " + entityIdColumn;
        }

        final String groupByClause = grouping;

        final String maximumSeverityQuery = (processAcknowledgedAlarms ? "max(distinct greatest(alarms.severity,3)) as maxSeverity " : "max(distinct case when alarms.alarmacktime is not null then 3 else greatest(alarms.severity,3) end) as maxSeverity ");

        return getHibernateTemplate().execute(new HibernateCallback<List<HeatMapElement>>() {
            @Override
            public List<HeatMapElement> doInHibernate(Session session) throws HibernateException, SQLException {
                return (List<HeatMapElement>) session.createSQLQuery(
                        "select coalesce(" + entityNameColumn + ",'Uncategorized'), " + entityIdColumn + ", " +
                                "count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) as servicesTotal, " +
                                "count(distinct node.nodeid) as nodeTotalCount, " +
                                maximumSeverityQuery +
                                "from node " +
                                "left join category_node using (nodeid) " +
                                "left join categories using (categoryid) " +
                                "left outer join ipinterface using (nodeid) " +
                                "left outer join ifservices on (ifservices.ipinterfaceid = ipinterface.id) " +
                                "left outer join service on (ifservices.serviceid = service.serviceid) " +
                                "left outer join alarms on (alarms.nodeid = node.nodeid and alarms.alarmtype in (1,3)) " +
                                "where nodeType <> 'D' " +
                                (restrictionColumn != null ? "and coalesce(" + restrictionColumn + ",'Uncategorized')='" + restrictionValue + "' " : "") +
                                "group by " + groupByClause + " having count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) > 0")
                        .setResultTransformer(new ResultTransformer() {
                            private static final long serialVersionUID = 5152094813503430377L;

                            @Override
                            public Object transformTuple(Object[] tuple, String[] aliases) {
                                return new HeatMapElement((String) tuple[0], (Number) tuple[1], (Number) tuple[2], (Number) tuple[3], (Number) tuple[4]);
                            }

                            @SuppressWarnings("rawtypes")
                            @Override
                            public List transformList(List collection) {
                                return collection;
                            }
                        }).list();
            }
        });
    }

}
