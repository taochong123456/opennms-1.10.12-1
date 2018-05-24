package org.opennms.web.controller.node;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Util;
import org.opennms.web.asset.Asset;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Interface;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Service;
import org.opennms.web.pathOutage.PathOutageFactory;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.NodeListService;
import org.opennms.web.svclayer.ResourceService;
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
@RequestMapping(value = "/asset", method = {RequestMethod.GET,RequestMethod.POST})
public class NodeController {
	private NodeListService nodeListService;
	private ResourceService m_resourceService;
	private int m_telnetServiceId;
	private int m_sshServiceId;
	private int m_httpServiceId;
	private int m_dellServiceId;
	private int m_snmpServiceId;
	private AssetModel m_model = new AssetModel();
    final static String EVENT_SOURCE_VALUE = "Web UI";

	@Autowired
	public NodeController(NodeListService nodeListService,
			ResourceService resourceService) {
		this.nodeListService = nodeListService;
		this.m_resourceService = resourceService;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listNodes(HttpServletRequest request,
			HttpServletResponse response) {
		List<Asset> nodeListModel = nodeListService.getNodeList();
		request.setAttribute("assets", nodeListModel);
		return "asset/nodelist";

	}
	
	@RequestMapping(value = "/addAsset", method = RequestMethod.POST)
	public String addAsset(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		OnmsAssetRecord record = new OnmsAssetRecord();
		String name = request.getParameter("m_label");
		String ip = request.getParameter("m_address");
		String mac = request.getParameter("mac");
		int assetId = nodeListService.createAssetRecord(record);
        createAndSendNewSuspectInterfaceEvent(ip,assetId);
		List<Asset> nodeListModel = nodeListService.getNodeList();
		request.setAttribute("assets", nodeListModel);
		return "asset/nodelist";

	}
	

    private void createAndSendNewSuspectInterfaceEvent(String ipaddr,int assetId) throws ServletException {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, EVENT_SOURCE_VALUE);
        bldr.setInterface(addr(ipaddr));
        bldr.setHost(InetAddressUtils.getLocalHostName());
        try {
            Util.createEventProxy().send(bldr.getEvent());
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + bldr.getEvent().getUei(), e);
        }
    }
	@RequestMapping(value = "/element", method = RequestMethod.GET)
	public String getElement(HttpServletRequest request,
			HttpServletResponse response) throws UnknownHostException,
			SQLException {
		int nodeId = Integer.parseInt(request.getParameter("node"));
		OnmsNode node_db = NetworkElementFactory.getInstance()
		.getNode(nodeId);
		Map<String, Object> nodeModel = new TreeMap<String, Object>();
		nodeModel.put("id", Integer.toString(nodeId));
		nodeModel.put("label", node_db.getLabel());
		nodeModel.put("foreignId", node_db.getForeignId());
		nodeModel.put("foreignSource", node_db.getForeignSource());

		List<Map<String, String>> links = new ArrayList<Map<String, String>>();
		links.addAll(createLinkForService(nodeId, m_telnetServiceId, "Telnet",
				"telnet://", ""));
		links.addAll(createLinkForService(nodeId, m_sshServiceId, "SSH",
				"ssh://", ""));
		links.addAll(createLinkForService(nodeId, m_httpServiceId, "HTTP",
				"http://", "/"));
		links.addAll(createLinkForService(nodeId, m_dellServiceId,
				"OpenManage", "https://", ":1311"));
		nodeModel.put("links", links);

		Asset asset = m_model.getAsset(nodeId);
		nodeModel.put("asset", asset);
		if (asset != null && asset.getBuilding() != null
				&& asset.getBuilding().length() > 0) {
			nodeModel.put("statusSite", asset.getBuilding());
		}

		nodeModel.put("resources",
				m_resourceService.findNodeChildResources(nodeId));
		nodeModel.put("vlans", NetworkElementFactory.getInstance()
				.getVlansOnNode(nodeId));
		nodeModel
				.put("criticalPath", PathOutageFactory.getCriticalPath(nodeId));
		nodeModel.put("noCriticalPath", PathOutageFactory.NO_CRITICAL_PATH);
		nodeModel.put("admin", request.isUserInRole(Authentication.ROLE_ADMIN));

		// get the child interfaces
		Interface[] intfs = NetworkElementFactory.getInstance()
				.getActiveInterfacesOnNode(nodeId);
		if (intfs != null) {
			nodeModel.put("intfs", intfs);
		} else {
			nodeModel.put("intfs", new Interface[0]);
		}

		Service[] snmpServices = NetworkElementFactory.getInstance()
				.getServicesOnNode(nodeId, m_snmpServiceId);
		if (snmpServices != null && snmpServices.length > 0) {
			for (Interface intf : intfs) {
				if ("P".equals(intf.getIsSnmpPrimary())) {
					nodeModel.put("snmpPrimaryIntf", intf);
					break;
				}
			}
		}

		nodeModel.put("status", getStatusStringWithDefault(node_db));
		nodeModel.put("showIpRoute", NetworkElementFactory.getInstance()
				.isRouteInfoNode(nodeId));
		nodeModel.put("showBridge", NetworkElementFactory.getInstance()
				.isBridgeNode(nodeId));
		nodeModel.put("showRancid", "true".equalsIgnoreCase(Vault
				.getProperty("opennms.rancidIntegrationEnabled")));

		nodeModel.put("node", node_db);

		request.setAttribute("model", nodeModel);

		List<Asset> nodeListModel = nodeListService.getNodeList();
		request.setAttribute("assets", nodeListModel);
		return "asset/node";

	}

	public void init() throws ServletException {
		try {
			m_telnetServiceId = NetworkElementFactory.getInstance()
					.getServiceIdFromName("Telnet");
		} catch (Throwable e) {
			throw new ServletException(
					"Could not determine the Telnet service ID", e);
		}

		try {
			m_sshServiceId = NetworkElementFactory.getInstance()
					.getServiceIdFromName("SSH");
		} catch (Throwable e) {
			throw new ServletException(
					"Could not determine the SSH service ID", e);
		}

		try {
			m_httpServiceId = NetworkElementFactory.getInstance()
					.getServiceIdFromName("HTTP");
		} catch (Throwable e) {
			throw new ServletException(
					"Could not determine the HTTP service ID", e);
		}

		try {
			m_dellServiceId = NetworkElementFactory.getInstance()
					.getServiceIdFromName("Dell-OpenManage");
		} catch (Throwable e) {
			throw new ServletException(
					"Could not determine the Dell-OpenManage service ID", e);
		}

		try {
			m_snmpServiceId = NetworkElementFactory.getInstance()
					.getServiceIdFromName("SNMP");
		} catch (Throwable e) {
			throw new ServletException(
					"Could not determine the SNMP service ID", e);
		}

	}

	public static String getStatusStringWithDefault(OnmsNode node_db) {
		String status = ElementUtil.getNodeStatusString(node_db);
		if (status != null) {
			return status;
		} else {
			return "Unknown";
		}
	}

	public static String findServiceAddress(int nodeId, int serviceId)
			throws SQLException, UnknownHostException {
		Service[] services = NetworkElementFactory.getInstance()
				.getServicesOnNode(nodeId, serviceId);
		if (services == null || services.length == 0) {
			return null;
		}

		List<InetAddress> ips = new ArrayList<InetAddress>();
		for (Service service : services) {
			ips.add(InetAddressUtils.addr(service.getIpAddress()));
		}

		InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

		if (lowest != null) {
			return lowest.getHostAddress();
		} else {
			return null;
		}
	}

	public static Collection<Map<String, String>> createLinkForService(
			int nodeId, int serviceId, String linkText, String linkPrefix,
			String linkSuffix) throws SQLException, UnknownHostException {
		String ip = findServiceAddress(nodeId, serviceId);
		if (ip == null) {
			return new ArrayList<Map<String, String>>();
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("text", linkText);
		map.put("url", linkPrefix + ip + linkSuffix);
		return Collections.singleton(map);
	}

}
