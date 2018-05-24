package org.opennms.web.controller.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.appdef.shared.AppdefEntityTypeID;
import org.hyperic.hq.appdef.shared.AppdefResourceValue;
import org.hyperic.hq.auth.shared.SessionException;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.AuthzBoss;
import org.hyperic.hq.bizapp.shared.MeasurementBoss;
import org.hyperic.hq.bizapp.shared.uibeans.MetricDisplaySummary;
import org.hyperic.hq.bizapp.shared.uibeans.ResourceDisplaySummary;
import org.hyperic.hq.events.AlertPermissionManager;
import org.hyperic.hq.measurement.MeasurementConstants;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.util.RequestUtils;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.emory.mathcs.backport.java.util.Arrays;

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
@RequestMapping(value = "/application", method = { RequestMethod.GET,
		RequestMethod.POST })
public class ApplicationController {

	private final Log log = LogFactory.getLog(ResourceHubPortalAction.class
			.getName());
	private AppdefBoss appdefBoss;
	private AuthzBoss authzBoss;
	private MeasurementBoss measurementBoss;
	private AlertPermissionManager alertPermissionManager;
	private static final int DEFAULT_ENTITY_TYPE = Constants.FILTER_BY_DEFAULT;
	private static final int DEFAULT_RESOURCE_TYPE = -1;
	private static final int DEFAULT_GROUP_TYPE = 1;
	private static final String DEFAULT_RESOURCE_NAME = null;

	@Autowired
	public ApplicationController(AppdefBoss appdefBoss, AuthzBoss authzBoss,
			MeasurementBoss measurementBoss,
			AlertPermissionManager alertPermissionManager) {
		this.appdefBoss = appdefBoss;
		this.authzBoss = authzBoss;
		this.measurementBoss = measurementBoss;
		this.alertPermissionManager = alertPermissionManager;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String applicationList(HttpServletRequest request,
			HttpServletResponse response) {
		int entityType;
		String ft = "";
		entityType = DEFAULT_ENTITY_TYPE;
		String resourceName = null;
		Integer ff = null;
		AppdefEntityTypeID aetid = null;
		String navHierarchy = null;
		Integer gid = null;
		int[] groupSubtype = null;
		int resourceType = DEFAULT_RESOURCE_TYPE;

		// TODO: Pass groupSubType as int[]
		PageList<AppdefResourceValue> resources = null;
		try {
			resources = appdefBoss.search(0, entityType,
					org.hyperic.util.StringUtil.escapeForRegex(resourceName,
							true), aetid, gid, groupSubtype, false, false,
					false, null);
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (SessionException e) {
			e.printStackTrace();
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		}
		request.setAttribute("entityType", entityType);
		request.setAttribute("pageList", resources.toArray());
		return "application/list";
	}

	@RequestMapping(value = "/resource", method = RequestMethod.GET)
	public String getResourceProp(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		AppdefEntityID entityId = null;

		try {
			entityId = RequestUtils.getEntityId(request);
		} catch (Exception e) {
			// not a problem, this can be null
		}
		Properties cprops = appdefBoss.getCPropDescEntries(0, entityId);
		request.setAttribute("cprops", cprops);
		List<ResourceDisplaySummary> servers = measurementBoss
				.findServersCurrentHealth(0, entityId, PageControl.PAGE_ALL);
		request.setAttribute("serverValues", servers);
		long end = System.currentTimeMillis();
		long begin = System.currentTimeMillis()-60*24*60*1000;
		List<MetricDisplaySummary> metrics = getMetrics(request, entityId, MeasurementConstants.FILTER_NONE, null, begin, end, false);

		request.setAttribute("metrics", metrics);
		return "application/resource";
	}
	
	
	 /**
     * Get from the Bizapp the set of metric summaries for the specified entity
     * that will be displayed on the page. Returns a <code>Map</code> keyed by
     * metric category.
     * 
     * @param request the http request
     * @param entityId the entity id of the currently viewed resource
     * @param begin the time (in milliseconds since the epoch) that begins the
     *        timeframe for which the metrics are summarized
     * @param end the time (in milliseconds since the epoch) that ends the
     *        timeframe for which the metrics are summarized
     * @return Map keyed on the category (String), values are List's of
     *         MetricDisplaySummary beans
     */
    protected List<MetricDisplaySummary> getMetrics(HttpServletRequest request, AppdefEntityID entityId,
                                                                long filters, String keyword, Long begin, Long end,
                                                                boolean showAll) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("finding metric summaries for resource [" + entityId + "] for range " + begin + ":" + end +
                      " filters value: " + filters + " and keyword: " + keyword);
        }

        AppdefEntityID[] entIds = new AppdefEntityID[] { entityId };
        Map<String, Set<MetricDisplaySummary>> metrics = measurementBoss.findMetrics(0, entIds, filters,
            keyword, begin.longValue(), end.longValue(), showAll);
        List<MetricDisplaySummary> sumaries = new ArrayList<MetricDisplaySummary>();
        for (String str:metrics.keySet()) {
  	      Set<MetricDisplaySummary> sumary=metrics.get(str);
  	      for (Iterator iterator = sumary.iterator(); iterator.hasNext();) {
				MetricDisplaySummary metricDisplaySummary = (MetricDisplaySummary) iterator.next();
				sumaries.add(metricDisplaySummary);
				System.out.println(metricDisplaySummary.getName()+"-----------"+metricDisplaySummary.getLabel()+"----");
				
			}}
        return sumaries;
    }


}
