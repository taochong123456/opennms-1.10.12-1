package org.opennms.hq.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.hyperic.hq.common.server.session.CrispoOption;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class CrispoOptionDAO extends AbstractDaoHibernate<CrispoOption, Integer> implements OnmsDao<CrispoOption,Integer> {

    public CrispoOptionDAO() {
        super(CrispoOption.class);
    }

    /**
     * Return a list of CrispoOption's that have a key that contains the given
     * String key
     * @param key The key to search for
     * @return A List of CrispoOptions that have a key that contains the given
     *         search key.
     */
    @SuppressWarnings("unchecked")
	public
    List<CrispoOption> findOptionsByKey(String key) {/*
        return createCriteria().add(Restrictions.like("key", "%" + key + "%")).list();
    */
    return null;	
    }

    @SuppressWarnings("unchecked")
	public
    List<CrispoOption> findOptionsByValue(String val) {/*
        String hql = "from CrispoOption o join o.array a where "
                     + "o.optionValue = :val or a = :val";
        return createQuery(hql).setString("val", val).list();
    */
    return null;	
    }
}
