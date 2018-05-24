package org.opennms.web.controller.plugin;
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hyperic.hq.appdef.Agent;
import org.hyperic.hq.product.Plugin;
import org.hyperic.hq.product.shared.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>
 * NodeCategoryBoxController class.
 * </p>
 * 
 * @author chong
 * @version $Id: $
 * @since 1.8.1
 */
@Controller
@RequestMapping(value = "/plugin", method = RequestMethod.GET)
public class PluginController {
	@Autowired
	public PluginController(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	private PluginManager pluginManager;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listNodes(HttpServletRequest request,
			HttpServletResponse response) {
		List<Plugin> plugins = pluginManager.getAllPlugins();
		request.setAttribute("plugins", plugins);
		return "plugin/pluginList";
	}

}
