package org.opennms.web.controller.autoDisc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AIQueueConstants;
import org.hyperic.hq.appdef.shared.AIServerValue;
import org.hyperic.hq.autoinventory.ScanStateCore;
import org.hyperic.hq.bizapp.shared.AIBoss;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.AuthzBoss;
import org.hyperic.hq.bizapp.shared.MeasurementBoss;
import org.hyperic.hq.events.AlertPermissionManager;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.util.BizappUtils;
import org.hyperic.hq.ui.util.RequestUtils;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.opennms.web.controller.application.ResourceHubPortalAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>
 * ViewController class.
 * </p>
 * 
 * @author chong
 * @version $Id: $
 * @since 1.8.1
 */
@Controller
@RequestMapping(value = "/autoDiscResource", method = { RequestMethod.GET,
		RequestMethod.POST })
public class ViewController {
	private final Log log = LogFactory.getLog(ResourceHubPortalAction.class
			.getName());
	private AppdefBoss appdefBoss;
	private AuthzBoss authzBoss;
	private MeasurementBoss measurementBoss;
	private AlertPermissionManager alertPermissionManager;
	private AIBoss aiBoss;
	private static final int DEFAULT_ENTITY_TYPE = Constants.FILTER_BY_DEFAULT;
	private static final int DEFAULT_RESOURCE_TYPE = -1;
	private static final int DEFAULT_GROUP_TYPE = 1;
	private static final String DEFAULT_RESOURCE_NAME = null;

	@Autowired
	public ViewController(AppdefBoss appdefBoss, AuthzBoss authzBoss,
			MeasurementBoss measurementBoss,
			AlertPermissionManager alertPermissionManager, AIBoss aiBoss) {
		this.appdefBoss = appdefBoss;
		this.authzBoss = authzBoss;
		this.measurementBoss = measurementBoss;
		this.alertPermissionManager = alertPermissionManager;
		this.aiBoss = aiBoss;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String autoDiscResourceList(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PageControl page = new PageControl();
		page.setPagesize(100);
		StopWatch watch = new StopWatch();
		if (log.isDebugEnabled()) {
			watch.start("getQueue");
		}
		// always show ignored platforms and already-processed platforms
		PageList<AIPlatformValue> aiQueue = aiBoss.getQueue(0, true,
				false, true, page);

		if (log.isDebugEnabled()) {
			watch.stop();
			log.debug(watch.prettyPrint());
		}
		List<AIPlatformWithStatus> queueWithStatus = getStatuses(0,
				aiQueue);
		request.setAttribute("resources", queueWithStatus);

		// If the queue is empty, check to see if there are ANY agents
		// defined in HQ inventory.
		if (aiQueue.size() == 0) {
			int agentCnt = appdefBoss.getAgentCount(0);
			request.setAttribute("hasNoAgents", new Boolean(agentCnt == 0));
		}

		// check every box for queue
		Integer[] platformsToProcess = new Integer[aiQueue.size()];
		StringBuffer platformIdStr = new StringBuffer();
		StringBuffer serverIdStr = new StringBuffer();
		List<Integer> serversToProcess = new ArrayList<Integer>();
		AIPlatformValue aiPlatform;
		AIServerValue[] aiServers;
		for (int i = 0; i < platformsToProcess.length; i++) {
			aiPlatform = aiQueue.get(i);
			platformsToProcess[i] = aiPlatform.getId();
			platformIdStr.append(platformsToProcess[i]);
			platformIdStr.append(",");
			// Add all non-virtual servers on this platform
			aiServers = aiPlatform.getAIServerValues();
			for (int j = 0; j < aiServers.length; j++) {
				if (!BizappUtils.isAutoApprovedServer(0, appdefBoss,
						aiServers[j])) {
					serversToProcess.add(aiServers[j].getId());
					serverIdStr.append(aiServers[j].getId());
					serverIdStr.append(",");
				}
			}
		}
		if(platformIdStr.length()>0){
			request.setAttribute("platformIdStr", platformIdStr.substring(0,platformIdStr.length()-1));
		}
		if(serverIdStr.length()>0){
			request.setAttribute("serverIdStr", serverIdStr.substring(0,serverIdStr.length()-1));
		}
		return "autoDisc/autoDiscList";

	}

	private List<AIPlatformWithStatus> getStatuses(int sessionId,
			PageList<AIPlatformValue> aiQueue) throws Exception {
		ScanStateCore ssc = null;
		List<AIPlatformWithStatus> results = new ArrayList<AIPlatformWithStatus>();
		for (Iterator<AIPlatformValue> it = aiQueue.iterator(); it.hasNext();) {
			AIPlatformValue aiPlatform = it.next();
			// FIXME: HHQ-4242: This needs to be done at the server-side /
			// manager layer,
			// not at the UI layer. Re-sync the queue, ensuring the status is
			// up-to-date
			aiPlatform = aiBoss.findAIPlatformById(sessionId, aiPlatform
					.getId().intValue());

			if (aiPlatform.getQueueStatus() == AIQueueConstants.Q_STATUS_PLACEHOLDER) {
				it.remove();
			} else {
				results.add(new AIPlatformWithStatus(aiPlatform, ssc));
			}
		}

		return results;
	}
}
