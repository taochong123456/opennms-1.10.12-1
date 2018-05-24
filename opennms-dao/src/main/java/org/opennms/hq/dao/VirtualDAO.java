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

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hyperic.hq.appdef.server.session.Platform;
import org.hyperic.hq.authz.server.session.Resource;
import org.hyperic.hq.authz.server.session.Virtual;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
public class VirtualDAO extends AbstractDaoHibernate<Virtual, Integer> implements OnmsDao<Virtual,Integer>{
    private ResourceDAO resourceDAO;

    public VirtualDAO(ResourceDAO resourceDAO) {
        super(Virtual.class);
        this.resourceDAO = resourceDAO;
    }

    public void save(Virtual entity) {
        super.save(entity);
    }

    public void remove(Virtual entity) {
        super.delete(entity);
    }

    public void createVirtual(Resource res, Integer processId) {
        Resource resource =resourceDAO
            .get(res.getId());
        Virtual virt = new Virtual();
        virt.setResource(resource);
        virt.setProcessId(processId);
    }

    public Virtual findByResource(Integer resourceId) {
        String sql = "from Virtual where resourceId = ?";
        return (Virtual)getSession()
            .createQuery(sql)
            .setInteger(0, resourceId.intValue())
            .uniqueResult();

    }

    Collection findVirtualByPysicalId(Integer id, String rtName) {
        String sql = "select v from Virtual v join fetch v.resource r " +
                "where r.resourceType.name = ? and v.physicalId = ?";
        return getSession().createQuery(sql)
                .setString(0, rtName)
                .setInteger(1, id.intValue())
                .setCacheable(true)
                .setCacheRegion("Virtual.findVirtualByPhysicalId")
                .list();
    }

    Collection findVirtualByProcessId(Integer id, String rtName) {
        String sql = "select v from Virtual v join v.resource r " +
                "where r.resourceType.name = ? and v.processId = ?";
        return getSession().createQuery(sql)
                .setString(0, rtName)
                .setInteger(1, id.intValue())
                .list();
    }

    Resource findVirtualByInstanceId(Integer id, String rtName) {
        String sql = "select v from Virtual v join v.resource r " +
                "where r.resourceType.name = ? and r.instanceId = ?";
        return (Resource) getSession().createQuery(sql)
                .setString(0, rtName)
                .setInteger(1, id.intValue())
                .uniqueResult();
    }
}
