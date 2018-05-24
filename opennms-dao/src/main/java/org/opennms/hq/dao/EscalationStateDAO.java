/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2007], Hyperic, Inc.
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

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hyperic.hq.authz.server.session.AuthzSubject;
import org.hyperic.hq.escalation.server.session.Escalatable;
import org.hyperic.hq.escalation.server.session.Escalation;
import org.hyperic.hq.escalation.server.session.EscalationState;
import org.hyperic.hq.escalation.server.session.PerformsEscalations;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class EscalationStateDAO  extends AbstractDaoHibernate<EscalationState, Integer> implements OnmsDao<EscalationState,Integer>  {
    EscalationStateDAO() {
        super(EscalationState.class);
    }

    /**
     * Find the current escalation state.
     * 
     * @param def The entity performing escalations.
     * @return The current escalation state or <code>null</code> if none exists
     *         for the entity performing escalations.
     */
    EscalationState find(PerformsEscalations def) {
        return (EscalationState) getSession().createCriteria(EscalationState.class).add(
            Expression.eq("alertDefinitionId", def.getId())).add(
            Expression.eq("alertTypeEnum", new Integer(def.getAlertType().getCode())))
            .setCacheable(true).setCacheRegion("EscalationState.findByTypeAndDef").uniqueResult();
  
    }

    EscalationState find(Escalatable esc) {
        Integer alertId = esc.getAlertInfo().getId();
        Integer alertType = new Integer(esc.getDefinition().getAlertType().getCode());

        return (EscalationState) getSession().createCriteria(EscalationState.class).add(Expression.eq("alertTypeEnum", alertType))
            .add(Expression.eq("alertId", alertId)).uniqueResult();
    	
    }

    Collection<EscalationState> findStatesFor(Escalation mesc) {
        return getSession().createCriteria(EscalationState.class).add(Expression.eq("escalation", mesc)).list();
    	
    }

    public List<EscalationState> getActiveEscalations(int maxEscalations) {
        return getSession().createCriteria(EscalationState.class).addOrder(Order.asc("nextActionTime")).setMaxResults(maxEscalations)
            .list();
    	
    }

    public void handleSubjectRemoval(AuthzSubject subject) {
        String sql = "update EscalationState set " + "acknowledgedBy = null "
                     + "where acknowledgedBy = :subject";

        getSession().createQuery(sql).setParameter("subject", subject).executeUpdate();
    }

    /**
     * Delete in batch the given escalation states.
     * 
     * @param stateIds The Ids for the escalation states to delete.
     */
    void removeAllEscalationStates(Integer[] stateIds) {
        if (stateIds.length == 0) {
            return;
        }

        getSession().createQuery("delete from EscalationState s where s.id in (:stateIds)")
            .setParameterList("stateIds", stateIds).executeUpdate();
    }

    @SuppressWarnings("unchecked")
	public
    Collection<EscalationState> getOrphanedEscalationStates(int ClassicEscalationAlertType, int GalertEscalationAlertType) {
        final String hql = new StringBuilder()
            .append("from EscalationState e where (alertTypeEnum = :classicType and not exists (")
                .append("select 1 from Alert a where a = e.alertId")
            .append(")) OR (alertTypeEnum = :galertType and not exists (")
                .append("select 1 from GalertLog g where g.id = e.alertId")
            .append("))")
            .toString();
        return getSession().createQuery(hql)
            .setInteger("classicType", ClassicEscalationAlertType)
            .setInteger("galertType", GalertEscalationAlertType)
            .list();	
    }

}
