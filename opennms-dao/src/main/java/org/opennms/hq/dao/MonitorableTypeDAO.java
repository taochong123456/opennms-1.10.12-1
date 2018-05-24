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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hyperic.hq.measurement.server.session.MonitorableType;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class MonitorableTypeDAO extends AbstractDaoHibernate<MonitorableType, Integer> implements OnmsDao<MonitorableType,Integer> {

    public MonitorableTypeDAO() {
        super(MonitorableType.class);
    }

    public MonitorableType create(String name, int appdefType, String plugin) {
        MonitorableType mt = new MonitorableType(name, appdefType, plugin);

        save(mt);
        return mt;
    }

    public MonitorableType findByName(String name) {
        String sql = "from MonitorableType where name=?";
        return (MonitorableType) getSession().createQuery(sql).setString(0, name).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public Map<String, MonitorableType> findByPluginName(String pluginName) {
        String sql = "from MonitorableType where plugin=?";
        List<MonitorableType> list = getSession().createQuery(sql).setString(0, pluginName).list();
        Map<String, MonitorableType> rtn = new HashMap<String, MonitorableType>(list.size());
        for (MonitorableType type : list) {
            rtn.put(type.getName(), type);
        }
        return rtn;
    }
}
