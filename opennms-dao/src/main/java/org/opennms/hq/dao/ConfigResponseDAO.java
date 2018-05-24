/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.ConfigResponseDB;
import org.hyperic.hq.appdef.server.session.Service;
import org.hyperic.hq.authz.server.session.Resource;
import org.hyperic.util.config.ConfigResponse;
import org.hyperic.util.config.EncodingException;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class ConfigResponseDAO extends AbstractDaoHibernate<ConfigResponseDB, Integer> implements OnmsDao<ConfigResponseDB,Integer>  {

    public ConfigResponseDAO() {
        super(ConfigResponseDB.class);

    }

    public ConfigResponseDB findById(Integer id) {
        return super.get(id);
    }

    public ConfigResponseDB get(Integer id) {
        return super.get(id);
    }

    public void save(ConfigResponseDB entity) {
        super.save(entity);
    }

    public void remove(ConfigResponseDB entity) {
        super.delete(entity);
    }

    /**
     * @return newly instantiated config response object
     */
    public ConfigResponseDB create() {
        ConfigResponseDB newConfig = new ConfigResponseDB();
        save(newConfig);
        return newConfig;
    }

    /**
     * Initialize the config response for a new platform
     */
    public ConfigResponseDB createPlatform() {/*
        try {
        	ConfigResponseDB cLocal = new ConfigResponseDB();
            ConfigResponse empty = new ConfigResponse();
            cLocal.setProductResponse(empty.encode());
            cLocal.setMeasurementResponse(empty.encode());
            save(cLocal);
        } catch (EncodingException e) {
            // will never happen, we're setting up an empty response
        }
        return cLocal;
    */
    return null;	
    }

    /**
     * ValidationError setter so that the version isn't incremented. The issue
     * is that when measurements are being scheduled during a resource creation
     * process, it's possible that an error will be registering at the same time
     * that the ConfigResponseDB object is being used somewhere else.
     */
    public void setValidationError(ConfigResponseDB resp, String error) {
        String sql = "update ConfigResponseDB set validationError = ? " + "where id = ?";
        getSession().createQuery(sql).setString(0, error).setParameter(1, resp.getId())
            .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private Map<Resource, ConfigResponseDB> getConfigs(List<Resource> list, String table) {
        String hql = "select t.resource, t.configResponse from :table t where t.resource in (:resources)";
        hql = hql.replace(":table", table);
        final List<Object[]> objs = new ArrayList<Object[]>();
        final int size = list.size();
        for (int ii=0; ii<size; ii+=BATCH_SIZE) {
            final int end = Math.min(size, ii+BATCH_SIZE);
            final List<Resource> sublist = list.subList(ii, end);
            final List<Object[]> result = getSession().createQuery(hql).setParameterList("resources", sublist).list();
            objs.addAll(result);
        }
        final Map<Resource, ConfigResponseDB> rtn = new HashMap<Resource, ConfigResponseDB>();
        for (final Object[] obj : objs) {
            rtn.put((Resource) obj[0], (ConfigResponseDB) obj[1]);
        }
        return rtn;
    }

    public Map<Resource, ConfigResponseDB> getServerConfigs(List<Resource> list) {
        return getConfigs(list, "Server");
    }

    public Map<Resource, ConfigResponseDB> getServiceConfigs(List<Resource> list) {
        return getConfigs(list, "Service");
    }

    public Map<Resource, ConfigResponseDB> getPlatformConfigs(List<Resource> list) {
        return getConfigs(list, "Platform");
    }

}
