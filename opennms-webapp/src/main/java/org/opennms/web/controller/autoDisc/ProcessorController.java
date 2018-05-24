package org.opennms.web.controller.autoDisc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hyperic.hq.appdef.shared.AIIpValue;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AIQueueConstants;
import org.hyperic.hq.appdef.shared.AIServerValue;
import org.hyperic.hq.auth.shared.SessionNotFoundException;
import org.hyperic.hq.auth.shared.SessionTimeoutException;
import org.hyperic.hq.bizapp.shared.AIBoss;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.AuthzBoss;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.WebUser;
import org.hyperic.hq.ui.server.session.DashboardConfig;
import org.hyperic.hq.ui.util.BizappUtils;
import org.hyperic.hq.ui.util.RequestUtils;
import org.hyperic.util.config.ConfigResponse;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@RequestMapping(value = "/autoDiscProcess", method = { RequestMethod.GET,
		RequestMethod.POST })
public class ProcessorController {
	private AIBoss aiBoss;
	private AppdefBoss appdefBoss;
	private AuthzBoss authzBoss;

	@Autowired
	public ProcessorController(AIBoss aiBoss, AppdefBoss appdefBoss,
			AuthzBoss authzBoss) {
		super();
		this.aiBoss = aiBoss;
		this.appdefBoss = appdefBoss;
		this.authzBoss = authzBoss;
	}

	@RequestMapping(value = "/process", method = { RequestMethod.GET,
			RequestMethod.POST })
	public String process(HttpServletRequest request,
			HttpServletResponse response) throws SessionNotFoundException,
			SessionTimeoutException {
		int sessionId = 0;
		String platformIdStr = request.getParameter("platformIdStr");
		String[] platformIds = platformIdStr.split(",");
		String serverIdStr = request.getParameter("serverIdStr");
		String[] serverIds = serverIdStr.split(",");
		Integer[] aiPlatformIds = new Integer[platformIds.length];
		for(int i = 0;i < platformIds.length; i++){
			aiPlatformIds[i] = Integer.parseInt(platformIds[i]);
		}
		Integer[] aiServerIds = new Integer[serverIds.length];
		for(int i =0;i<serverIds.length;i++){
			aiServerIds[i] = Integer.parseInt(serverIds[i]);
		}
		
		int queueAction = 1;
		boolean isApproval = (queueAction == AIQueueConstants.Q_DECISION_APPROVE);
		boolean isIgnore = (queueAction == AIQueueConstants.Q_DECISION_IGNORE);

		List<Integer> aiPlatformList = new ArrayList<Integer>();
		List<Integer> aiIpList = new ArrayList<Integer>();
		List<Integer> aiServerList = new ArrayList<Integer>();

		// Refresh the queue items this user can see.
		PageControl page = new PageControl();

		page.setPagesize(100);

		PageList<AIPlatformValue> aiQueue = aiBoss.getQueue(sessionId, true,
				false, true, page);

		// Walk the queue. For each platform in the queue:
		//
		// 1. If it's selected for processing, add all of its IPs (and later,
		// all of it's virtual servers) for processing. If it's selected
		// for removal, remove all servers, not just virtual ones.
		//
		// 2. If any of its servers are selected for APPROVAL, then select
		// the platform for approval as well.
		//
		int pidx, sidx;
		for (int i = 0; i < aiQueue.size(); i++) {
			AIPlatformValue aiPlatform = (AIPlatformValue) aiQueue.get(i);
			pidx = isSelectedForProcessing(aiPlatform, aiPlatformIds);
			if (pidx == -1) {
				// platform isnt selected
				continue;
			}

			aiPlatformList.add(aiPlatformIds[pidx]);

			AIIpValue[] ips = aiPlatform.getAIIpValues();
			for (int j = 0; j < ips.length; j++)
				aiIpList.add(ips[j].getId());

			AIServerValue[] aiServers = aiPlatform.getAIServerValues();
			// Now check servers on this platform
			for (int j = 0; j < aiServers.length; j++) {
				sidx = isSelectedForProcessing(aiServers[j], aiServerIds);
				if (sidx != -1) {
					// If we're approving stuff, and this platform's not
					// already in the list, add it
					if (isApproval
							&& !aiPlatformList.contains(aiPlatform.getId())) {
						aiPlatformList.add(aiPlatform.getId());
					}

					// Add the server (XXX: Maybe we shouldn't add it if the
					// server
					// is ignored?)
					aiServerList.add(aiServers[j].getId());

					// Set error flag if the server is modified and the user
					// tries to ignore it.
					if (isIgnore
							&& aiServers[j].getQueueStatus() != AIQueueConstants.Q_STATUS_ADDED) {

					}
				} else if (isApproval
						&& BizappUtils.isAutoApprovedServer(sessionId,
								appdefBoss, aiServers[j])) {
					// All virtual servers are approved when their platform
					// is approved. The HQ agent is also auto-approved.
					aiServerList.add(aiServers[j].getId());
				}
			}
		}

		if (aiServerList.isEmpty() && isIgnore) {
			// Change to purge
			queueAction = AIQueueConstants.Q_DECISION_PURGE;
		}

		try {
			aiBoss.processQueue(sessionId, aiPlatformList, aiServerList,
					aiIpList, queueAction);
		} catch (Exception e) {
		}
		return null;

	}

	private int isSelectedForProcessing(AIPlatformValue aiPlatform,
			Integer[] platformsToProcess) {
		Integer id = aiPlatform.getId();
		for (int i = 0; i < platformsToProcess.length; i++) {
			if (platformsToProcess[i].equals(id))
				return i;
		}
		return -1;
	}

	private int isSelectedForProcessing(AIServerValue aiServer,
			Integer[] serversToProcess) {
		Integer id = aiServer.getId();
		for (int i = 0; i < serversToProcess.length; i++) {
			if (serversToProcess[i].equals(id))
				return i;
		}
		return -1;
	}
}
