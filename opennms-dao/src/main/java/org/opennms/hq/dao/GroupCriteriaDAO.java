package org.opennms.hq.dao;

import org.hyperic.hq.management.shared.GroupCriteria;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class GroupCriteriaDAO extends AbstractDaoHibernate<GroupCriteria, Integer> implements OnmsDao<GroupCriteria,Integer> {

    public GroupCriteriaDAO() {
        super(GroupCriteria.class);
    }
    
}
