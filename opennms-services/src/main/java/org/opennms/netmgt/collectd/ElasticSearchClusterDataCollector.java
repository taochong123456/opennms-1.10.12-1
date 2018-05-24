package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.core.utils.ThreadCategory;

public class ElasticSearchClusterDataCollector{
	private Map<String, String> responseMap = new HashMap<String, String>();
	private HashMap<String, String> elasticSearchMap = new HashMap<String, String>();
	private Hashtable<String, Object> config = new Hashtable<String, Object>();
	private HashMap<String, Object> hm = new HashMap<String, Object>();

	private String webContent = new String();
	private String clusterName = "";
	int responseCode;
	int totalPendingTasks = 0;
	private static String hostportURL = "";
	private JSONObject jsonObj = null;
	List<String> esNodes = new ArrayList<String>();
	List<String> esPorts = new ArrayList<String>();
	List<String> esDisplayNames = new ArrayList<String>();

	int activeShards = 0;
	int activePrimaryShards = 0;
	int relocatingShards = 0;
	int initializingShards = 0;
	int unassignedShards = 0;
	int delayedUnassignedShards = 0;
	int totalShards = 1;

	Properties returnProps = new Properties();
	URLDataCollector urlDC = new URLDataCollector();
	private Properties property;

	public Properties discover(Properties props) {
		this.returnProps = ((Properties) props.clone());
		try {
			boolean conn = getConnection(this.returnProps);
			if (!conn) {
				this.returnProps.put("authentication", "failed");
				return this.returnProps;
			}
			this.returnProps.put("authentication", "passed");
		} catch (Exception e) {
			log().error("ElasticSearchClusterDataCollector discover error:"+e);
		}

		return this.returnProps;
	}

	public boolean checkConnection(String hostname, String port, Properties props) {
		int pp = 0;
		try {
			pp = Integer.parseInt(port);
		} catch (NumberFormatException ex) {
			log().error("ElasticSearchClusterDataCollector checkConnection error:"+ex);
			return false;
		}
		String hosturl = "";
		try {
			String protocol = "http";
			if ((props.get("ssl") != null) && (props.get("ssl").toString().equalsIgnoreCase("true"))) {
				protocol = "https";
			}
			hosturl = protocol + "://" + hostname + ":" + pp;
			this.webContent = getRestAPIResult(hosturl, "?filter_path=cluster_name");
			this.jsonObj = new JSONObject(this.webContent);
			if (this.jsonObj.has("cluster_name")) {
				return true;
			}
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector checkConnection error:"+ex);
			return false;
		}
		return false;
	}

	public Properties getNodesToBeAdded(Properties returnProps) {
		Properties props = new Properties();
		try {
			hostportURL = hostURL(returnProps);
			if (returnProps.get("DiscoverAllNodes").toString().equalsIgnoreCase("Yes")) {
				getNodeList();
			} else {
				this.esNodes.add((String) returnProps.get("HostName"));
				this.esPorts.add((String) returnProps.get("Port"));
				this.esDisplayNames.add(returnProps.get("HostName") + "_Node");
			}

			this.webContent = getRestAPIResult(hostportURL, "/_cluster/stats?filter_path=cluster_name");
			this.jsonObj = new JSONObject(this.webContent);
			if (this.jsonObj.has("cluster_name")) {
				returnProps.put("clusterName", this.jsonObj.getString("cluster_name"));
			} else {
				returnProps.put("clustername", "");
			}
		} catch (Exception e) {
			log().error("ElasticSearchClusterDataCollector getNodesToBeAdded error:"+e);
		}

		props.put("esNodes", this.esNodes);
		props.put("esPorts", this.esPorts);
		props.put("esDisplayNames", this.esDisplayNames);
		return props;
	}

	public void getNodeList() {
		try {
			this.esNodes = (this.esPorts = this.esDisplayNames = null);

			this.webContent = getRestAPIResult(hostportURL, "/_cat/nodes?h=ip");
			this.webContent = this.webContent.trim();
			String[] s = this.webContent.split("\n");
			for (int i = 0; i < s.length; i++) {
				s[i] = s[i].trim();
			}
			this.esNodes = Arrays.asList(s);
			this.webContent = getRestAPIResult(hostportURL, "/_cat/nodes?h=name");
			this.webContent = this.webContent.trim();
			s = this.webContent.split("\n");
			for (int i = 0; i < s.length; i++) {
				s[i] = s[i].trim();
			}
			this.esDisplayNames = Arrays.asList(s);

			s = new String[this.esDisplayNames.size()];
			for (int i = 0; i < this.esDisplayNames.size(); i++) {
				this.webContent = ((String) this.esNodes.get(i));
				s[i] = getPort(this.webContent);
			}
			this.esPorts = Arrays.asList(s);
			List nodes = new ArrayList();
			List ports = new ArrayList();
			List names = new ArrayList();
			for (int i = 0; i < this.esDisplayNames.size(); i++) {
				if ((((String) this.esPorts.get(i)).equals("0")) || (((String) this.esPorts.get(i)).equals(null))
						|| (((String) this.esPorts.get(i)).equals("")))
					continue;
				nodes.add(this.esNodes.get(i));
				ports.add(this.esPorts.get(i));
				names.add(this.esDisplayNames.get(i));
			}

			this.esPorts = null;
			this.esNodes = null;
			this.esDisplayNames = null;
			this.esPorts = ports;
			this.esNodes = nodes;
			this.esDisplayNames = names;
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getNodeList error:"+ex);
		}
	}

	public String getPort(String node) {
		String httpAddress = "9200";
		try {
			this.webContent = getRestAPIResult(hostportURL,
					"/_nodes/" + node + "/http?filter_path=nodes.*.http_address");
			this.jsonObj = new JSONObject(this.webContent);

			if (this.jsonObj.has("nodes")) {
				String nodeDetails = this.jsonObj.getString("nodes");
				JSONObject newjsonObj = new JSONObject(nodeDetails);

				if ((newjsonObj.length() == 0) || (newjsonObj.toString() == "{}")) {
					return "0";
				}

				Iterator keys = newjsonObj.keys();
				if (keys.hasNext()) {
					String key = (String) keys.next();

					JSONObject value = newjsonObj.getJSONObject(key);

					if (value.has("http_address")) {
						httpAddress = value.getString("http_address");
					}

					httpAddress = parsePort(httpAddress);
					return httpAddress;
				}

				return "0";
			}

		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getPort error:"+ex);
		}
		return "0";
	}

	public String parsePort(String httpAddress) {
		try {
			httpAddress = httpAddress.substring(httpAddress.lastIndexOf(":") + 1);
			char ch = httpAddress.charAt(httpAddress.length() - 1);
			while (!Character.isDigit(ch)) {
				httpAddress = httpAddress.substring(0, httpAddress.length() - 1);
				ch = httpAddress.charAt(httpAddress.length() - 1);
			}

			return httpAddress;
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector parsePort error:"+ex);
		}
		return "0";
	}

	private boolean getConnection(Properties props) {
		this.property = ((Properties) props.clone());
		hostportURL = hostURL(props);
		setProperties(hostportURL, "/_cluster/health");
		long currTime = System.currentTimeMillis();
		try {
			this.urlDC = new URLDataCollector();
			this.hm = this.urlDC.startDatacollection(this.config);
			try {
				this.responseCode = Integer.parseInt((String) this.hm.get("responsecode"));
			} catch (Exception ex) {
				log().error("ElasticSearchClusterDataCollector getConnection error:"+ex);
			}
			if (this.responseCode == 200) {
				this.webContent = ((String) this.hm.get("htmlresponse"));
				if (!this.webContent.contains("cluster_name")) {
					return false;
				}

				this.jsonObj = new JSONObject(this.webContent);
				this.clusterName = this.jsonObj.getString("cluster_name");
				try {
					this.jsonObj = new JSONObject(this.webContent);
					if (this.jsonObj.has("active_shards")) {
						this.activeShards = this.jsonObj.getInt("active_shards");
					}

					if (this.jsonObj.has("active_primary_shards")) {
						this.activePrimaryShards = this.jsonObj.getInt("active_primary_shards");
					}

					if (this.jsonObj.has("relocating_shards")) {
						this.relocatingShards = this.jsonObj.getInt("relocating_shards");
					}

					if (this.jsonObj.has("initializing_shards")) {
						this.initializingShards = this.jsonObj.getInt("initializing_shards");
					}

					if (this.jsonObj.has("unassigned_shards")) {
						this.unassignedShards = this.jsonObj.getInt("unassigned_shards");
					}

					if (this.jsonObj.has("delayed_unassigned_shards")) {
						this.delayedUnassignedShards = this.jsonObj.getInt("delayed_unassigned_shards");
					}

					if (this.jsonObj.has("number_of_pending_tasks")) {
						this.totalPendingTasks = this.jsonObj.getInt("number_of_pending_tasks");
					}
				} catch (Exception ex) {
					log().error("ElasticSearchClusterDataCollector getConnection error:"+ex);
				}
			}else{
				return false;
			}
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getConnection error:"+ex);
		}
		return true;
	}

	public HashMap<String,String> startDatacollection(Hashtable props) {
		boolean cc = false;
		try {
			cc = getConnection((Properties) props);
			if (cc) {
				getPerformanceData();
			}
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector startDatacollection error:"+ex);
		}

		return this.elasticSearchMap;
	}

	public void getPerformanceData() {
		try {
			getConfInfo();
			getNodeCount();
			getShardCount();
			getPendingTasks();
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getPerformanceData error:"+ex);
		}
	}

	public void getConfInfo() {
		String publishPort;
		String masterNodePort;
		String masterNodeName;
		String masterNodeIP;
		String clusterStatus = masterNodeIP = masterNodeName = masterNodePort = publishPort = "";
		int totalDocs;
		int totalIndices = totalDocs = 0;
		try {
			this.webContent = getRestAPIResult("/_cluster/stats?filter_path=status,indices.count,indices.docs.count");
			this.jsonObj = new JSONObject(this.webContent);

			if (this.jsonObj.has("status")) {
				clusterStatus = this.jsonObj.getString("status");
			}

			if (this.jsonObj.has("indices")) {
				String indexDetails = this.jsonObj.getString("indices");
				JSONObject newjsonObj = new JSONObject(indexDetails);
				if (newjsonObj.has("count")) {
					totalIndices = newjsonObj.getInt("count");
				}

				if (newjsonObj.has("docs")) {
					indexDetails = newjsonObj.getString("docs");
					JSONObject newjsonObj1 = new JSONObject(indexDetails);
					if (newjsonObj1.has("count")) {
						totalDocs = newjsonObj1.getInt("count");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			this.webContent = getRestAPIResult("/_cat/master?h=ip");
			masterNodeIP = this.webContent.trim() + "";
			this.webContent = getRestAPIResult("/_cat/master?h=node");
			masterNodeName = this.webContent.trim() + "";
		} catch (Exception ex) {
		}

		try {
			this.webContent = getRestAPIResult("/_nodes/" + masterNodeIP
					+ "/attributes?filter_path=nodes.*.transport_address,nodes.*.http_address");
			this.jsonObj = new JSONObject(this.webContent);
			if (this.jsonObj.has("nodes")) {
				String nodeDetails = this.jsonObj.getString("nodes");
				JSONObject newjsonObj = new JSONObject(nodeDetails);
				Iterator keys = newjsonObj.keys();
				if (keys.hasNext()) {
					String key = (String) keys.next();

					JSONObject value = newjsonObj.getJSONObject(key);
					String transportAddress = "";
					String httpAddress = "";
					if (value.has("transport_address")) {
						transportAddress = value.getString("transport_address");
					}

					if (value.has("http_address")) {
						httpAddress = value.getString("http_address");
					}

					publishPort = parsePort(transportAddress);
					masterNodePort = parsePort(httpAddress);
				}
			}
		} catch (Exception ex) {
		}
		this.responseMap.put("totalIndices", totalIndices + "");
		this.responseMap.put("totalDocs", totalDocs + "");
		this.responseMap.put("clusterName", this.clusterName);
		this.responseMap.put("clusterStatus", clusterStatus);
		this.responseMap.put("masterNodeName", masterNodeName);
		this.responseMap.put("masterNodeIP", masterNodeIP);
		this.responseMap.put("masterNodePort", masterNodePort);
		this.responseMap.put("publishPort", publishPort);
	}

	public void getShardCount() {
		try {
			this.webContent = getRestAPIResult("/_stats?filter_path=_shards.total");
			this.jsonObj = new JSONObject(this.webContent);
			if (this.jsonObj.has("_shards")) {
				this.webContent = this.jsonObj.getString("_shards");
				this.jsonObj = new JSONObject(this.webContent);
				if (this.jsonObj.has("total")) {
					this.totalShards = this.jsonObj.getInt("total");
					responseMap.put("totalShards", totalShards+"");
				}
			}
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getShardCount error:"+ex);
		}

	}

	public void getNodeCount() {
		String totalMasterData;
		String totalClientOnly;
		String totalDataOnly;
		String totalMasterOnly;
		String totalNodes = totalMasterOnly = totalDataOnly = totalClientOnly = totalMasterData = "";
		try {
			this.webContent = getRestAPIResult("/_cluster/stats?filter_path=nodes.count");
			this.jsonObj = new JSONObject(this.webContent);

			if (this.jsonObj.has("nodes")) {
				String nodeDetails = this.jsonObj.getString("nodes");
				this.jsonObj = new JSONObject(nodeDetails);
				if (this.jsonObj.has("count")) {
					nodeDetails = this.jsonObj.getString("count");
					this.jsonObj = new JSONObject(nodeDetails);
					if (this.jsonObj.has("total")) {
						totalNodes = this.jsonObj.getString("total");
					}
					if (this.jsonObj.has("master_only")) {
						totalMasterOnly = this.jsonObj.getString("master_only");
					}
					if (this.jsonObj.has("data_only")) {
						totalDataOnly = this.jsonObj.getString("data_only");
					}
					if (this.jsonObj.has("master_data")) {
						totalMasterData = this.jsonObj.getString("master_data");
					}
					if (this.jsonObj.has("client")) {
						totalClientOnly = this.jsonObj.getString("client");
					}
				}
			}

		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getNodeCount error:"+ex);
		}
		this.responseMap.put("totalNodes", totalNodes + "");
	}

	public void getPendingTasks() {
		String source;
		String priority;
		String timeInQueue;
		String insertOrder = timeInQueue = priority = source = "";
		try {
			if (this.totalPendingTasks != 0) {
				this.webContent = getRestAPIResult("/_cluster/pending_tasks");
				this.jsonObj = new JSONObject(this.webContent);
				JSONArray jsonArray = (JSONArray) this.jsonObj.get("tasks");
				for (int i = 0; i < this.totalPendingTasks; i++) {
					this.jsonObj = jsonArray.getJSONObject(i);
					if (this.jsonObj.has("insert_order")) {
						insertOrder = this.jsonObj.getString("insert_order");
					}

					if (this.jsonObj.has("time_in_queue_millis")) {
						timeInQueue = this.jsonObj.getString("time_in_queue_millis");
					}

					if (this.jsonObj.has("priority")) {
						priority = this.jsonObj.getString("priority");
					}

					if (this.jsonObj.has("source")) {
						source = this.jsonObj.getString("source");
					}
				}
			}
		} catch (Exception ex) {
			log().error("ElasticSearchClusterDataCollector getPendingTasks error:"+ex);
		}
	}

	private String getRestAPIResult(String restAPI) {
		setProperties(hostportURL, restAPI);
		this.hm = this.urlDC.startDatacollection(this.config);
		if (!this.hm.get("responsecode").toString().equals("200")) {
			return "";
		}
		this.webContent = ((String) this.hm.get("htmlresponse"));
		return this.webContent;
	}

	private String getRestAPIResult(String hostportURL, String restAPI) {
		setProperties(hostportURL, restAPI);
		this.hm = this.urlDC.startDatacollection(this.config);
		if (!this.hm.get("responsecode").toString().equals("200")) {
			return "";
		}
		this.webContent = ((String) this.hm.get("htmlresponse"));
		return this.webContent;
	}

	public String hostURL(Hashtable props) {
		String hostName = (String) props.get("HostName");
		int port = Integer.parseInt((String) props.get("Port"));
		String protocol = "http";
		if ((props.get("ssl") != null) && (props.get("ssl").toString().equalsIgnoreCase("true"))) {
			protocol = "https";
		}
		String hosturl = protocol + "://" + hostName + ":" + port;
		return hosturl;
	}

	private void setProperties(String hosturl, String restApiURL) {
		String esURI = hosturl + restApiURL;
		this.config.put("mt", "URL");
		this.config.put("mn", "es");
		this.config.put("a", esURI);
		this.config.put("hn", "esHeader");
		this.config.put("hv", "esValue");
		this.config.put("webContent", "yes");
		this.config.put("returnRespStr", "true");

		if ((String) this.property.get("UserName") == null) {
			this.property.setProperty("UserName", "");
			this.property.setProperty("Password", "");
		}
		this.config.put("un", (String) this.property.get("UserName"));
		this.config.put("ps", (String) this.property.get("Password"));
	}

	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(HttpCollector.class);
	}
	
	public Map<String,String> getResponseMap(){
		return responseMap;
	}


}