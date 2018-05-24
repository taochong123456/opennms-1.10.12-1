/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.api.Util;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>AssetRecordResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Path("assetRecord")
@Transactional
public class AssetRecordResource extends OnmsRestService {
    
    @Autowired
    private NodeDao m_nodeDao;    
    
    @Autowired
    private EventProxy m_eventProxy;
    final static String EVENT_SOURCE_VALUE = "Web UI";

    /**
     * <p>getAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsAssetRecord getAssetRecord(@PathParam("nodeCriteria") String nodeCriteria) {
        readLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getCategories: Can't find node " + nodeCriteria);
            }
            return getAssetRecord(node);
        } finally {
            readUnlock();
        }
    }
    
    /**
     * <p>updateAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateAssetRecord(@PathParam("nodeCriteria") String nodeCriteria,  MultivaluedMapImpl params) {
        writeLock();
		List<String> ips = params.get("ip");
        try {
        	OnmsNode node = null;
        	if("undefined".equals(nodeCriteria)){
        		node = new OnmsNode();
        		List<String> names = params.get("name");
        		List<String> macs = params.get("mac"); 
        		if(names!=null){
            		node.setLabel(names.get(0)+"");
        		}
        		if(ips!=null){
            		node.setIp(ips.get(0)+"");
        		}
        		if(macs!=null){
            		node.setMac(macs.get(0)+"");
        		}
        		node.setType("A");
        		OnmsAssetRecord assetRecord = new OnmsAssetRecord();
        		node.setAssetRecord(assetRecord);
        		assetRecord.setNode(node);
        	}else{
        		 node = m_nodeDao.get(nodeCriteria);
                 if (node == null) {
                     throw getException(Status.BAD_REQUEST, "updateAssetRecord: Can't find node " + nodeCriteria);
                 }
        	}
        	
            OnmsAssetRecord assetRecord = getAssetRecord(node);
            if (assetRecord == null) {
                throw getException(Status.BAD_REQUEST, "updateAssetRecord: Node " + node  + " could not update ");
            }
            log().debug("updateAssetRecord: updating category " + assetRecord);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(assetRecord);
            for(String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
       
            log().debug("updateAssetRecord: assetRecord " + assetRecord + " updated");
            m_nodeDao.saveOrUpdate(node);
            
            try {
            	if("undefined".equals(nodeCriteria)){
            		if(ips!=null){
                		createAndSendNewSuspectInterfaceEvent(ips.get(0),node.getId());
                	}
            	}else{
                    sendEvent(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, node.getId());
            	}
            
            } catch (EventProxyException e) {
                throw getException(Status.BAD_REQUEST, e.getMessage());
            } catch (ServletException e) {
				e.printStackTrace();
			}
            
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    private OnmsAssetRecord getAssetRecord(OnmsNode node) {
        return node.getAssetRecord();
    }
    
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        EventBuilder bldr = new EventBuilder(uei, getClass().getName());
        bldr.setNodeid(nodeId);
        m_eventProxy.send(bldr.getEvent());
    }

    private void createAndSendNewSuspectInterfaceEvent(String ipaddr,int nodeId) throws ServletException {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, EVENT_SOURCE_VALUE);
        bldr.setInterface(addr(ipaddr));
        bldr.setHost(InetAddressUtils.getLocalHostName());
        bldr.setNodeid(Long.parseLong(nodeId+""));
        try {
            Util.createEventProxy().send(bldr.getEvent());
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + bldr.getEvent().getUei(), e);
        }
    }
    
}
