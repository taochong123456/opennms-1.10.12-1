package org.opennms.hq.dao;

import java.util.Collection;

import org.hyperic.hq.common.ConfigProperty;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class ConfigPropertyDAO extends AbstractDaoHibernate<ConfigProperty, Integer> implements OnmsDao<ConfigProperty,Integer> {
    public ConfigPropertyDAO() {
        super(ConfigProperty.class);
    }

    protected boolean cacheFindAll() {
        return true;
    }

    public ConfigProperty create(String prefix, String key, String val, String def) {
        ConfigProperty c = new ConfigProperty();
        c.setPrefix(prefix);
        c.setKey(key);
        c.setValue(val);
        c.setDefaultValue(def);
        save(c);
        return c;
    }

    public Collection<ConfigProperty> findByPrefix(String s) {
        String sql = "from ConfigProperty where prefix=?";
        return getSession().createQuery(sql).setString(0, s).list();
    }
    
    /**
     * Find Config property of a specific key
     * 
     * @param key
     * @return ConfigProperty object of a server config property
     */
    public ConfigProperty findByKey(String key) {
        String sql = "from ConfigProperty where key=?";
        return (ConfigProperty) getSession().createQuery(sql).setString(0, key).uniqueResult();
    }
}
