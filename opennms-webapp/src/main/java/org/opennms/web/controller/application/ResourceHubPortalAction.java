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

package org.opennms.web.controller.application;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hyperic.hq.appdef.shared.AppdefEntityConstants;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.appdef.shared.AppdefEntityTypeID;
import org.hyperic.hq.appdef.shared.AppdefInventorySummary;
import org.hyperic.hq.appdef.shared.AppdefResourceTypeValue;
import org.hyperic.hq.appdef.shared.AppdefResourceValue;
import org.hyperic.hq.appdef.shared.InvalidAppdefTypeException;
import org.hyperic.hq.authz.server.session.AuthzSubject;
import org.hyperic.hq.authz.server.session.ResourceGroup;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.AuthzBoss;
import org.hyperic.hq.bizapp.shared.MeasurementBoss;
import org.hyperic.hq.events.AlertPermissionManager;
import org.hyperic.hq.measurement.MeasurementConstants;
import org.hyperic.hq.measurement.UnitsConvert;
import org.hyperic.hq.measurement.server.session.MeasurementTemplate;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.WebUser;
import org.hyperic.hq.ui.util.RequestUtils;
import org.hyperic.util.config.InvalidOptionException;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.hyperic.util.timer.StopWatch;
import org.hyperic.util.units.FormattedNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * An <code>Action</code> that sets up the Resource Hub portal.
 */
@Controller
@RequestMapping(value = "/ResourceHub", method = {RequestMethod.GET,RequestMethod.POST})
public class ResourceHubPortalAction{

    private static final String BLANK_LABEL = "";
    private static final String BLANK_VAL = "";
    private static final String PLATFORM_KEY = "resource.hub.filter.PlatformType";
    private static final String SERVER_KEY = "resource.hub.filter.ServerType";
    private static final String SERVICE_KEY = "resource.hub.filter.ServiceType";
    public static final int SELECTOR_GROUP_COMPAT = 1;
    public static final int SELECTOR_GROUP_ADHOC = 2;
    public static final int SELECTOR_GROUP_DYNAMIC = 3;

    private static final int DEFAULT_ENTITY_TYPE = Constants.FILTER_BY_DEFAULT;
    private static final int DEFAULT_RESOURCE_TYPE = -1;
    private static final int DEFAULT_GROUP_TYPE = 1;
    private static final String DEFAULT_RESOURCE_NAME = null;

    private static final String SEPARATOR = "&nbsp;&rsaquo;&nbsp;";
    private static final String VIEW_ATTRIB = "Resource Hub View";
    private static final String TYPE_ATTRIB = "Resource Hub Apppdef Type";
    private static final String GRP_ATTRIB = "Resource Hub Group Type";

    private final Log log = LogFactory.getLog(ResourceHubPortalAction.class.getName());
    private AppdefBoss appdefBoss;
    private AuthzBoss authzBoss;
    private MeasurementBoss measurementBoss;
    private AlertPermissionManager alertPermissionManager;

    @Autowired
    public ResourceHubPortalAction(AppdefBoss appdefBoss, AuthzBoss authzBoss, MeasurementBoss measurementBoss, AlertPermissionManager alertPermissionManager) {
        super();
        this.appdefBoss = appdefBoss;
        this.authzBoss = authzBoss;
        this.measurementBoss = measurementBoss;
        this.alertPermissionManager = alertPermissionManager;
    }

    /**
     * Set up the Resource Hub portal.
     */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return null;
	}

    private Comparator<AppdefResourceTypeValue> getTypeComparator() {
        return new Comparator<AppdefResourceTypeValue>() {
            public int compare(AppdefResourceTypeValue o1, AppdefResourceTypeValue o2) {
                if (o1 == o2) {
                    return 0;
                }
                int rtn = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                if (rtn != 0) {
                    return rtn;
                } else {
                    return o1.getId().compareTo(o2.getId());
                }
            }
        };
    }

    private String[] getResourceMetrics(HttpServletRequest request, int sessionId, MeasurementBoss mboss,
                                        Collection<MeasurementTemplate> templates, final AppdefEntityID entityId)
        throws RemoteException {
        Map<Integer, MetricValue> vals = mboss.getLastIndicatorValues(sessionId, entityId);

        // Format the values
        String[] metrics = new String[templates.size()];
        if (vals.size() == 0) {
            Arrays.fill(metrics, RequestUtils.message(request, "common.value.notavail"));
        } else {
            int i = 0;
            for (Iterator<MeasurementTemplate> it = templates.iterator(); it.hasNext(); i++) {
                MeasurementTemplate mt = it.next();

                if (vals.containsKey(mt.getId())) {
                    MetricValue mv = vals.get(mt.getId());
                    FormattedNumber fn = UnitsConvert.convert(mv.getValue(), mt.getUnits());
                    metrics[i] = fn.toString();
                } else {
                    metrics[i] = RequestUtils.message(request, "common.value.notavail");
                }
            }
        }
        return metrics;
    }

    private boolean isAdhocGroupSelected(int type) {
        return type == SELECTOR_GROUP_ADHOC;
    }

    private boolean isDynamicGroupSelected(int type) {
        return type == SELECTOR_GROUP_DYNAMIC;
    }

    private boolean isCompatGroupSelected(int type) {
        return type == SELECTOR_GROUP_COMPAT;
    }

    private boolean isGroupSelected(int type) {
        return type == AppdefEntityConstants.APPDEF_TYPE_GROUP;
    }

    private String msg(HttpServletRequest request, String key) {
        return RequestUtils.message(request, key);
    }
}
