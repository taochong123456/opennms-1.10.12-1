package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

public class HadoopV2RestDataCollector {
	ArrayList nodeList = new ArrayList();
	private Properties property;
	private Hashtable<String, Object> config = new Hashtable();
	private HashMap<String, Object> hm = new HashMap();
	private static String hostportURL = "";
	URLDataCollector urlDC = new URLDataCollector();
	private String webContent = new String();

	public HashMap startDatacollection(Properties prop) {
		try {
			boolean isConnection = getConnection(prop);
			if (isConnection) {
				getPerformanceData(prop);
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return hm;
	}

	private boolean getConnection(Properties props) {
		this.property = ((Properties) props.clone());
		hostportURL = hostURL(props);
		setProperties(hostportURL, "/jmx");
		long currTime = System.currentTimeMillis();
		this.urlDC = new URLDataCollector();
		this.hm = this.urlDC.startDatacollection(this.config);
		int responseCode = 0;
		try {
			responseCode = Integer.parseInt((String) this.hm
					.get("responsecode"));
		} catch (Exception ex) {

		}
		if (responseCode == 200) {
			return true;
		} else {
			return false;
		}
	}

	public void getPerformanceData(Properties prop) {
		// getHDFSData();
		getYARNData();
		// getApplicationsData();
	}

	private void getHDFSData() {
		JSONObject json = null;
		try {
			String response = (String) hm.get("htmlresponse");
			json = new JSONObject(response);

			if (json != null) {
				JSONArray jsonary = json.getJSONArray("beans");
				for (int i = 0; i < jsonary.length(); i++) {
					JSONObject MBean = jsonary.getJSONObject(i);
					try {
						if (MBean.getString("name").equalsIgnoreCase(
								HadoopConstants.mbean_dfs_stat_oname)) {
							HashMap dfsData = getMBeanAttrValue(MBean,
									HadoopConstants.v2_dfs_stat_attrs);
							Iterator it = dfsData.keySet().iterator();
							while (it.hasNext()) {
								String attrName = it.next().toString();
								if (attrName.equals("LiveNodes")) {
									JSONObject LiveNodes = new JSONObject(
											((String) dfsData.get(attrName))
													.toString());
									Iterator keys = LiveNodes.keys();
									while (keys.hasNext()) {
										String key = (String) keys.next();
										ArrayList data = new ArrayList();
										data.add(key);
										this.nodeList.add(key);
										JSONObject DNDetails = LiveNodes
												.getJSONObject(key);
										String state = null;
										if (DNDetails.getString("adminState")
												.equalsIgnoreCase("In Service"))
											state = "Live";
										else if (DNDetails.getString(
												"adminState").equalsIgnoreCase(
												"Decommission In Progress"))
											state = "Decommission In Progress";
										else if (DNDetails.getString(
												"adminState").equalsIgnoreCase(
												"Decommission In Progress")) {
											state = "Live - Decommissioned";
										}

										data.add(state);
										data.add(DNDetails
												.getString("capacity"));
										data.add(DNDetails
												.getString("nonDfsUsedSpace"));
										data.add(DNDetails
												.getString("usedSpace"));
										data.add(DNDetails
												.getString("remaining"));
										for (int k = 0; k < data.size(); k++) {
											System.out.print(data.get(k)
													+ "nnnnnnnn");
										}
										data.clear();
									}
								} else if (attrName.equals("DeadNodes")) {
									JSONObject DeadNodes = new JSONObject(
											((String) dfsData.get(attrName))
													.toString());
									Iterator keys = DeadNodes.keys();
									while (keys.hasNext()) {
										String key = (String) keys.next();
										ArrayList data = new ArrayList();
										data.add(key);
										this.nodeList.add(key);
										JSONObject DNDetails = DeadNodes
												.getJSONObject(key);
										String state = null;
										if (DNDetails.getString(
												"decommissioned")
												.equalsIgnoreCase("true"))
											state = "Dead - Decommissioned";
										else {
											state = "Dead";
										}
										data.add(state);
										data.add("");
										data.add("");
										data.add("");
										data.add("");
										data.clear();
									}
								} else {
								}
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}

					try {
						if (MBean.getString("name").equalsIgnoreCase(
								HadoopConstants.mbean_v2_blocks_stat_oname)) {
							HashMap blockData = getMBeanAttrValue(MBean,
									HadoopConstants.v2_block_stat_attrs);
							Iterator it = blockData.keySet().iterator();
							while (it.hasNext()) {
								String attName = it.next().toString();
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}

					try {
						if (MBean.getString("name").equalsIgnoreCase(
								HadoopConstants.mbean_nnfsstate_oname)) {
							HashMap datanodeData = getMBeanAttrValue(MBean,
									HadoopConstants.v2_dn_stats_attrs);
							Iterator it = datanodeData.keySet().iterator();
							while (it.hasNext()) {
								String attName = it.next().toString();
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getYARNData() {
		JSONObject json = null;
		try {
			String response = (String) hm.get("htmlresponse");
			json = new JSONObject(response);
			if (json != null) {
				JSONArray jsonary = json.getJSONArray("beans");
				for (int i = 0; i < jsonary.length(); i++) {
					JSONObject MBean = jsonary.getJSONObject(i);
					try {
						if (MBean.getString("name").equalsIgnoreCase(
								HadoopConstants.mbean_rm_stat_oname)) {
							JSONArray liveNM = new JSONArray(
									MBean.getString("LiveNodeManagers"));
							for (int j = 0; j < liveNM.length(); j++) {
								JSONObject node = liveNM.getJSONObject(j);
								ArrayList data = new ArrayList();
								data.add(node.getString("HostName"));
								if (this.nodeList.contains(node
										.getString("HostName"))) {
									this.nodeList.remove(node
											.getString("HostName"));
								}
								data.add(node.getString("Rack"));
								data.add(node.getString("State"));
								data.add(node.getString("NodeManagerVersion"));
								if ((node.has("UsedMemoryMB"))
										&& (node.has("AvailableMemoryMB"))) {
									float memoryUsedPercent = (float) (Long
											.parseLong(node
													.getString("UsedMemoryMB"))
											/ (Long.parseLong(node
													.getString("UsedMemoryMB")) + Long
													.parseLong(node
															.getString("AvailableMemoryMB"))) * 100L);
									data.add(String.valueOf(memoryUsedPercent));
								} else {
									data.add("");
								}
								for (int k = 0; k < data.size(); k++) {
									System.out.print(data.get(k) + "===");
								}
								data.clear();
							}
							if (!this.nodeList.isEmpty())
								for (int j = 0; j < this.nodeList.size(); j++) {
									ArrayList data = new ArrayList();
									data.add(this.nodeList.get(j).toString());
									data.add("");
									data.add("Dead");
									data.add("");
									data.add("");
								}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}

					try {
						if (MBean.getString("name").equalsIgnoreCase(
								HadoopConstants.mbean_nm_stat_oname)) {
							HashMap nmData = getMBeanAttrValue(MBean,
									HadoopConstants.nm_stat_attrs);
							Iterator it = nmData.keySet().iterator();
							while (it.hasNext()) {
								String attName = it.next().toString();
								System.out.println(attName + "----"
										+ nmData.get(attName));
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getApplicationsData() {
		JSONObject json = null;
		try {
			String response = (String) hm.get("htmlresponse");
			System.out.println(response);
			json = new JSONObject(response);
			if (!json.getString("clusterMetrics").equalsIgnoreCase("null")) {
				HashMap datanodeData = getMBeanAttrValue(
						new JSONObject(json.getString("clusterMetrics")),
						HadoopConstants.app_stat_attrs);
				Iterator it = datanodeData.keySet().iterator();
				while (it.hasNext()) {
					String attName = it.next().toString();
					System.out.println();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
		if ((props.get("ssl") != null)
				&& (props.get("ssl").toString().equalsIgnoreCase("true"))) {
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

	public HashMap<String, String> getMBeanAttrValue(JSONObject mbean,
			String[] attrList) {
		HashMap data = new HashMap();
		for (int i = 0; i < attrList.length; i++) {
			try {
				data.put(attrList[i], mbean.getString(attrList[i]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static void main(String[] args) {
		/*
		 * HadoopV2RestDataCollector hdc = new HadoopV2RestDataCollector();
		 * Properties prop = new Properties(); prop.put("nameNodeHost",
		 * "app-centos5-64-2"); prop.put("nameNodePort", "50070");
		 * prop.put("isSSLEnabled", "false"); prop.put("jobTrackerHost",
		 * "app-centos5-64-2"); prop.put("jobTrackerPort", "8088");
		 * prop.put("isAuthReqForNameNode", "true"); prop.put("authType",
		 * "simple"); prop.put("nameNodeUserName", "hduser");
		 * prop.put("isMapreduceEnabled", "true");
		 */

		HadoopV2RestDataCollector hadoop = new HadoopV2RestDataCollector();
		Properties props = new Properties();
		props.put("HostName", "192.168.239.113");
		props.put("Port", "8088");
		hadoop.startDatacollection(props);
	}
}