package org.opennms.netmgt.collectd;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.APIDatacollectorUtil;

/**
 * spark worker datacollector
 * 
 * **/
public class ApacheSparkWorkerDatacollector {
	private Map<String, String> responseMap = new HashMap<String, String>();
	private Properties property;
	private Map<String, String> sparkMap = new HashMap<String, String>();
	private Hashtable<String, Object> config = new Hashtable<String, Object>();
	private HashMap<String, Object> hm = new HashMap<String, Object>();
	int responseCode;
	private static String hostportURL = "";
	private JSONObject jsonObj = null;
	private String webContent = "";
	URLDataCollector urlDC = new URLDataCollector();

	private boolean getConnection(Properties props) {
		this.property = ((Properties) props.clone());
		hostportURL = APIDatacollectorUtil.getHostPortURL(props);
		setProperties(hostportURL, "/json");
		long currTime = System.currentTimeMillis();
		try {
			this.urlDC = new URLDataCollector();
			this.hm = this.urlDC.startDatacollection(this.config);
			try {
				this.responseCode = Integer.parseInt((String) this.hm
						.get("responsecode"));
			} catch (Exception ex) {
				log().error(
						"ApacheSparkWorkerDatacollector getConnection:" + ex);
			}
			if (this.responseCode == 200) {
				this.webContent = ((String) this.hm.get("htmlresponse"));
				this.jsonObj = new JSONObject(this.webContent);

			} else {
				if (this.responseCode == 400) {
					return false;
				}
				if (this.responseCode == 404) {
					return false;
				}
				if (this.responseCode == 401) {
					return false;
				}

				return false;
			}
		} catch (Exception ex) {
			log().error("ApacheSparkWorkerDatacollector getConnection:" + ex);
			return false;
		}
		return true;
	}

	private void setProperties(String hosturl, String restApiURL) {
		String asURI = hosturl + restApiURL;
		this.config.put("mt", "URL");
		this.config.put("mn", "as");
		this.config.put("a", asURI);
		this.config.put("hn", "asHeader");
		this.config.put("hv", "asValue");
		this.config.put("webContent", "yes");
		this.config.put("returnRespStr", "true");

		if ((String) this.property.get("UserName") == null) {
			this.property.setProperty("UserName", "");
			this.property.setProperty("Password", "");
		}
		this.config.put("un", (String) this.property.get("UserName"));
		this.config.put("ps", (String) this.property.get("Password"));
	}

	public Map startDatacollection(Properties props) {
		boolean cc = false;
		try {
			cc = getConnection(props);
			if (cc) {
				getPerformanceData();
			}
		} catch (Exception ex) {
			log().error(
					"ApacheSparkWorkerDatacollector startDatacollection:" + ex);
		}

		return this.sparkMap;
	}

	private void getPerformanceData() {
		try {
			getExecutorAndOverallDetails(this.webContent);
		} catch (Exception ex) {
			log().error(
					"ApacheSparkWorkerDatacollector getPerformanceData:" + ex);
		}
	}

	private void getExecutorAndOverallDetails(String webContent) {
		String masterURL = "";
		String masterWebUIURL = "";
		String workerId = "";
		int totalCores = 0;
		int coresFree = 0;
		int coresUsed = 0;
		int activeExecutors = 0;
		int finishedExecutors = 0;
		float memTotal = 0.0F;
		float memFree = 0.0F;
		float memUsed = 0.0F;
		float memUsedPercent = 0.0F;
		float MBtoGB = 1024.0F;
		try {
			this.jsonObj = new JSONObject(webContent);

			workerId = APIDatacollectorUtil.getStringfromJSON(this.jsonObj,
					"id");
			masterURL = APIDatacollectorUtil.getStringfromJSON(this.jsonObj,
					"masterurl");
			masterWebUIURL = APIDatacollectorUtil.getStringfromJSON(
					this.jsonObj, "masterwebuiurl");
			totalCores = APIDatacollectorUtil.getIntfromJSON(this.jsonObj,
					"cores");
			coresUsed = APIDatacollectorUtil.getIntfromJSON(this.jsonObj,
					"coresused");
			activeExecutors = APIDatacollectorUtil.getJsonArrayfromJSON(
					this.jsonObj, "executors").length();
			finishedExecutors = APIDatacollectorUtil.getJsonArrayfromJSON(
					this.jsonObj, "finishedexecutors").length();
			memTotal = APIDatacollectorUtil.getFloatfromJSON(this.jsonObj,
					"memory") / MBtoGB;
			memUsed = APIDatacollectorUtil.getFloatfromJSON(this.jsonObj,
					"memoryused") / MBtoGB;

			coresFree = totalCores - coresUsed;
			memFree = memTotal - memUsed;
			if (memTotal > 0.0F) {
				memUsedPercent = memUsed / memTotal;
			}

			getExecutorDetails(APIDatacollectorUtil.getJsonArrayfromJSON(
					this.jsonObj, "executors"));
		} catch (Exception e) {
			log().error(
					"ApacheSparkWorkerDatacollector getPerformanceData:" + e);
		}
		this.responseMap.put("workerId", workerId);
		this.responseMap.put("masterURL", masterURL);
		this.responseMap.put("masterWebUIURL", masterWebUIURL);
		this.responseMap.put("coresFree", coresFree + "");
		this.responseMap.put("coresUsed", coresUsed + "");
		this.responseMap.put("activeExecutors", activeExecutors + "");
		this.responseMap.put("finishedExecutors", finishedExecutors + "");
		this.responseMap.put("memFree", memFree + "");
		this.responseMap.put("memUsed", memUsed + "");
		this.responseMap.put("memUsedPercent", memUsedPercent + "");
		this.responseMap.put("memTotal",
				new DecimalFormat("#.##").format(memTotal));
		this.responseMap.put("cores", totalCores+"");
	}

	private void getExecutorDetails(JSONArray executorArray) {
		float MBtoGB = 1024.0F;
		try {

			for (int i = 0; i < executorArray.length(); i++) {
				JSONObject json = (JSONObject) executorArray.get(i);
			}
		} catch (Exception e) {
			log().error(
					"ApacheSparkWorkerDatacollector getPerformanceData:" + e);
		}
	}

	public static String getHostPortURL(Hashtable props) {
		String hostName = (String) props.get("HostName");
		int port = Integer.parseInt((String) props.get("Port"));

		String protocol = "http";
		if ((props.get("ssl") != null)
				&& (props.get("ssl").toString().equalsIgnoreCase("true"))) {
			protocol = "https";
		}
		String hosturl = protocol + "://" + hostName + ":" + port;
		return hosturl;
	}

	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(HttpCollector.class);
	}

	public Map<String, String> getResponseMap() {
		return responseMap;
	}

}