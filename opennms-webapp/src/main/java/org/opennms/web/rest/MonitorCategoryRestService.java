/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opennms.netmgt.config.monitor.MonitorCategory;
import org.opennms.netmgt.config.monitor.Monitorgroup;
import org.opennms.netmgt.config.monitor.Server;
import org.opennms.netmgt.dao.MonitorCategoryConfigurationDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.web.rest.support.MonitorListDTOCollection;
import org.opennms.web.rest.support.MonitorMapDTOCollection;
import org.opennms.web.rest.support.MonitorRowDTOCollection;
import org.opennms.web.rest.support.ServerMapDTOItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for OnmsNode entity
 * 
 * @author Chong
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("monitorCategory")
@Transactional
public class MonitorCategoryRestService extends OnmsRestService {
	@Autowired
	private MonitorCategoryConfigurationDao m_monitorCategoryDao;
	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	/**
	 * <p>
	 * getNodes
	 * </p>
	 * 
	 * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("list")
	public MonitorListDTOCollection listCategory() {
		MonitorCategory monitorCategory = m_monitorCategoryDao.getConfig();
		return transferToDTOObject(monitorCategory);
	}

	private MonitorListDTOCollection transferToDTOObject(
			MonitorCategory monitorCategory) {
		Monitorgroup[] monitorGroups = monitorCategory.getMonitorgroup();
		MonitorListDTOCollection monitorList  = new MonitorListDTOCollection();
		int row  =monitorGroups.length % 4;
		if(row == 0){
			row = monitorGroups.length/4;
		}else{
			row = monitorGroups.length/4+1;
		}
		MonitorRowDTOCollection[] monitorArray = new MonitorRowDTOCollection[row];
		for(int i=0;i<row;i++){
			monitorArray[i] = new MonitorRowDTOCollection();
		}
		for(int i=0;i<monitorGroups.length;i++){
			MonitorMapDTOCollection monitorDTO = new MonitorMapDTOCollection();
			Monitorgroup group = monitorGroups[i];
			Server[] servers = group.getServer();
			monitorDTO.setLabel(group.getLabel());
			for(Server server : servers){
				ServerMapDTOItem serverDTO = new ServerMapDTOItem();
				serverDTO.setIcon(server.getIcon());
				serverDTO.setName(server.getName());
				serverDTO.setLabel(server.getLabel());
				serverDTO.setCount(m_monitoredServiceDao.countByType(server.getName()));
				monitorDTO.addServerItem(serverDTO);
			}
			
			monitorArray[i/4].addMonitorItems(monitorDTO);
		}
		for(MonitorRowDTOCollection rowDTO:monitorArray){
			monitorList.addMonitorRow(rowDTO);
		}
		return monitorList;
	}

}
