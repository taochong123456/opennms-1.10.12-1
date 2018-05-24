/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2008], Hyperic, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hyperic.hq.measurement.server.session.MeasurementDataId;
import org.hyperic.hq.measurement.server.session.MetricProblem;
import org.hyperic.util.jdbc.DBUtil;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class MetricProblemDAO extends AbstractDaoHibernate<MetricProblem, Integer> implements OnmsDao<MetricProblem,Integer> {
    private static Log _log = LogFactory.getLog(MetricProblemDAO.class);

    public MetricProblemDAO() {
        super(MetricProblem.class);
    }

    public MetricProblem get(MeasurementDataId id) {
     /*   return (MetricProblem) super.get(id);*/
    return null;
    }

    public void remove(MetricProblem entity) {
        super.delete(entity);
    }

    public MetricProblem create(Integer mid, long time,
                                int type, Integer additional) {

        MeasurementDataId id = new MeasurementDataId(mid, time, additional);

        MetricProblem p = new MetricProblem();
        p.setId(id);
        p.setType(new Integer(type));

        save(p);
        return p;
    }

    public int deleteByMetricIds(Collection<Integer> ids) {
        String hql = "delete MetricProblem where measurement_id in (:ids)";

        Session session = getSession();
        int count = 0;
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            ArrayList<Integer> subIds = new ArrayList<Integer>();

            for (int i = 0; i < DBUtil.IN_CHUNK_SIZE && it.hasNext(); i++) {
                subIds.add(it.next());
            }

            count += session.createQuery(hql).setParameterList("ids", subIds)
                            .executeUpdate();

            if (_log.isDebugEnabled()) {
                _log.debug("deleteByMetricIds() " + subIds.size() + " of " +
                           ids.size() + " metric IDs");
            }
        }

        return count;
    }
}
