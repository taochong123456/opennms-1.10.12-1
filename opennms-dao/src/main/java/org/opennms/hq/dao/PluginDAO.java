package org.opennms.hq.dao;

import java.util.Collection;
import java.util.Collections;

import org.hyperic.hq.common.SystemException;
import org.hyperic.hq.product.Plugin;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class PluginDAO extends AbstractDaoHibernate<Plugin, Integer> implements OnmsDao<Plugin,Integer> {
    public PluginDAO() {
        super(Plugin.class);
    }

    public Plugin create(String name, String version, String path, String md5) {
        Plugin p = new Plugin();
        p.setName(name);
        p.setVersion(version);
        p.setPath(path);
        p.setMD5(md5);
        p.setModifiedTime(System.currentTimeMillis());
        save(p);
        return p;
    }

    public Plugin findByName(String name) {
        String sql = "from Plugin where name=?";
        return (Plugin) getSession().createQuery(sql).setString(0, name).uniqueResult();
    }

    public Plugin getByFilename(String filename) {
        String hql = "from Plugin where path = :filename";
        return (Plugin) getSession().createQuery(hql).setString("filename", filename).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public Collection<Plugin> getPluginsByFileNames(Collection<String> pluginFileNames) {
        if (pluginFileNames.isEmpty()) {
            return Collections.emptyList();
        }
        String hql = "from Plugin where path in (:filenames)";
        return getSession().createQuery(hql).setParameterList("filenames", pluginFileNames).list();
    }

    public long getMaxModTime() {
        final String hql = "select max(modifiedTime) from Plugin";
        final Number num = (Number) getSession().createQuery(hql).uniqueResult();
        if (num == null) {
            // this is a big problem, throw SystemException
            throw new SystemException("cannot fetch max(modifiedTime) from the Plugin table");
        }
        return num.longValue();
    }
    
    protected boolean cacheFindAll() {
        return true;
    }
}
