package org.opennms.protocols.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import net.sf.cglib.asm.Constants;

import org.opennms.core.http.ObjectHolder;
import org.opennms.core.http.Util;

public class ServerAgentConstants {
	public static final int SAMPLING_TIME = 30;
	public static final int SAMPLING_INIT_DELAY = 0;
	public static final String LINUX = "LINUX";
	public static final String MSPCUSTOMERID = "MSPCUSTOMERID";
	public static final String AGENTKEY = "AGENTKEY";
	public static final String DATACOLLECTTIME = "DATACOLLECTTIME";
	public static final String ERRORMSG = "ERRORMSG";
	public static final String CPUUTILIZATION = "CPUUtilization";
	public static final int REGISTER = 1001;
	public static final int METRIC_DATA = 1002;
	public static final String PARAM_SEPARATER = "&";
	public static final String DISCOVER_PROCESS = "DISCOVER_PROCESSES_AND_SERVICES";
	public static final String UPD_SELECTED_PROCESS = "UPDATE_SERVICE_AND_PROCESS_DETAILS";
	public static final String ADD_PROCESS = "ADD_PROCESS";
	public static final String KILL_PROCESS = "KILL_PROCESS";
	public static Long pROBE_SERVER_POLLTIME = new Long(300000L);
	public static final Long PROBE_STATUS_UPDATE_TIME = new Long(60000L);
	public static final String DISCOVER = "DISCOVER";
	public static final String RESET = "RESET";
	public static final String ADD_MONITOR = "ADDMONITOR";
	public static final String POLL_NOW = "POLL_NOW";
	public static final String RESTART = "RESTART";
	public static final String TESTWMS = "TESTWMS";
	public static final String MODIFY_THREAD_POOL_SIZE = "MODIFY_THREADPOOL";
	public static final int THREAD_POOL_SIZE = 50;
	public static final String DISABLE_AGENT_SERVICE = "DISABLE_AGENT_SERVICE";
	public static final String PROBE_CONTEXT = "probe";
	public static String sECONDARY_HOST = "plus2.site24x7.com";
	public static String sECONDARY_PORT = "443";
	public static final int CATEGORY_WINDOWS = 0;
	public static final int CATEGORY_LINUX = 1;
	public static final int CATEGORY_WINDOWS_PLUGIN = 4;
	public static final int CATEGORY_LINUX_PLUGIN = 5;
	public static final String S247PLUS = "s24x7+";
	public static final int TELNET = 0;
	public static final int SSH_PWD = 1;
	public static final int SSH_KEY = 2;
	public static final String ADD_APP = "ADDAPP";
	public static final String REMOVE_APP = "REMOVEAPP";
	public static final String MOD_APP = "MODAPP";
	public static final String MOD_TZ = "MOD_TIMEZONE";
	public static final String ADD_CREDENTIAL = "ADDCREDENTIAL";
	public static final String ADD_DEVICE = "ADDDEVICE";
	public static final String ADD_NETWORK = "ADDNETWORK";
	public static final String UPDATE_ID = "UPDATESITE24X7ID";
	public static final String DELETE_DEVICE = "DELETEDEVICE";
	public static final String UPDATE_DEVICE_STATUS = "UPDATEDEVICESTATUS";
	private static String wmsSERVER = "dms.zoho.com";
	private static String wmsPRODUCTID = "SI";
	public static String pROTOCOL = "https";
	public static String cOMMUNICATIONPROTOCOLS = getProtocols();
	private static Properties agentConfig = loadAgentConfig();
	private static Properties wmsConfig = loadWMSConfig();
	private static String sITE247_HOST = "plus.site24x7.com";
	private static String sITE247_PORT = "443";
	private static String aC_HOST = "plusac.site24x7.com";
	private static String aC_PORT = "443";
	public static final String WMS_REQ_SEP = "<WMSFAILEDREQ>";
	public static final String HOST_DISCOVERY = "HOST_DISCOVERY";
	private static final String AGENT_VERSION = loadAgentVersionInfo();
	public static final String WEBSOCKETURL = "ws://dmsws.csez.zohocorpin.com:8080/wsconnect";
	public static final String DC_ZIP = "DATA_COLLECTION_ZIP";
	public static final String RA_URL = "/plus/RegisterAgent";
	public static final String FC_URL = "/plus/PollerFileCollector";
	public static final String DAH_URL = "/plus/DataAgentHandlerServlet";
	public static final String SU_URL = "/plus/PollerStatusUpdater";
	public static final String PU_URL = "/plus/PollerPluginsUpdater";
	public static final String UPLOAD_URL = "/plus/NetworkConfigReceiver";
	public static final String DOWNLOAD_URL = "/plus/NetworkConfigSender";
	public static final String NET_DATA = "/plus/NetworkDataReceiver";
	public static final String NET_DATA_SENDER = "/plus/NetworkDataSender";
	private static String wmsparams;
	static FilenameFilter fileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.matches(ObjectHolder.getpluginModules().getProperty("OPM.homeDir"));
		}
	};

	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException unknown) {
			unknown.printStackTrace();
		}
		return "localhost";
	}

	public static String getLocalIpAddr() {
		String returnIp = "127.0.0.1";
		try {
			returnIp = InetAddress.getLocalHost().getHostAddress();
			if ((returnIp != null) && (returnIp.equals("127.0.1.1"))) {
				returnIp = getRealIP();
			}
		} catch (UnknownHostException unknown) {
			unknown.printStackTrace();
		}
		return returnIp;
	}

	public static String getRealIP() {
		String ip = "127.0.0.1";
		Socket socket = null;
		try {
			socket = new Socket("dms.zoho.com", 443);
			ip = socket.getLocalAddress().toString();
			if (ip.startsWith("/")) {
				ip = ip.substring(ip.indexOf("/") + 1);
			}
		} catch (ConnectException e) {
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ip;
	}

	public static Object getProxyConfig() {
		return null;
	}

	public static String getWmsServer() {
		return wmsSERVER;
	}

	public static boolean initialize(String confPath) {
		return false;
	}

	private static Properties loadAgentConfig() {
		return agentConfig;
	}

	private static Properties loadWMSConfig() {
		FileInputStream confis = null;
		Properties tmpagentConfig = new Properties();
		try {
			if (new File(new StringBuilder().append("conf").append(File.separator).append("localsetup.properties")
					.toString()).exists()) {
				confis = new FileInputStream(new StringBuilder().append("conf").append(File.separator)
						.append("localsetup.properties").toString());

				tmpagentConfig.load(confis);

				if (tmpagentConfig != null) {
					if (tmpagentConfig.containsKey("wms.prdid")) {
						wmsPRODUCTID = (String) tmpagentConfig.get("wms.prdid");
					} else {
						wmsPRODUCTID = "SI";
					}
					if (tmpagentConfig.containsKey("routingserver")) {
						wmsSERVER = (String) tmpagentConfig.get("routingserver");
					} else {
						wmsSERVER = "dms.zoho.com";
					}
					if (tmpagentConfig.containsKey("protocol")) {
						pROTOCOL = (String) tmpagentConfig.get("protocol");
					} else {
						pROTOCOL = "https";
					}
					if (tmpagentConfig.containsKey("secondaryhost")) {
						sECONDARY_HOST = (String) tmpagentConfig.get("secondaryhost");
					}
					if (tmpagentConfig.containsKey("secondaryport")) {
						sECONDARY_PORT = (String) tmpagentConfig.get("secondaryport");
					}
					if (tmpagentConfig.containsKey("host")) {
						sITE247_HOST = (String) tmpagentConfig.get("host");
						agentConfig.setProperty("HOST", sITE247_HOST);
					}
					if (tmpagentConfig.containsKey("port")) {
						sITE247_PORT = (String) tmpagentConfig.get("port");
						agentConfig.setProperty("PORT", sITE247_PORT);
					}
					if (tmpagentConfig.containsKey("achost")) {
						aC_HOST = (String) tmpagentConfig.get("achost");
						agentConfig.setProperty("ACHOST", aC_HOST);
					}
					if (tmpagentConfig.containsKey("acport")) {
						aC_PORT = (String) tmpagentConfig.get("acport");
						agentConfig.setProperty("ACPORT", aC_PORT);
					}
				}
			} else {
				wmsPRODUCTID = "SI";
				wmsSERVER = "dms.zoho.com";
				tmpagentConfig.put("wms.prdid", wmsPRODUCTID);
				tmpagentConfig.put("wmsSERVER", wmsSERVER);
			}
			Properties localProperties1 = tmpagentConfig;
			return localProperties1;
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				if (confis != null) {
					confis.close();
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	private static String loadAgentVersionInfo() {
		FileInputStream confis = null;
		try {
			confis = new FileInputStream(new StringBuilder().append("conf").append(File.separator)
					.append("EUMServer.properties").toString());
			Properties tmpagentConfig = new Properties();
			tmpagentConfig.load(confis);
			if (tmpagentConfig != null) {
				if (tmpagentConfig.containsKey("agent.pollinterval")) {
					pROBE_SERVER_POLLTIME = new Long(Util
							.parseLong(new StringBuilder().append("")
									.append(tmpagentConfig.containsKey("agent.pollinterval")).toString(), 5L)
							* 60L * 1000L);
				}
				String str = (String) tmpagentConfig.get("agent.version");
				return str;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				confis.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return "1.0.0";
	}

	public static String getWmsparams() {
		return wmsparams;
	}

	public static void setWmsparams(String wmsparams) {
		wmsparams = wmsparams;
	}

	public static String getCustomerID() {
		return agentConfig.getProperty("CUSTOMERID").trim();
	}

	public static String getServerHost() {
		return agentConfig.getProperty("HOST") != null ? agentConfig.getProperty("HOST") : sITE247_HOST;
	}

	public static int getServerPort() {
		String portstr = agentConfig.getProperty("PORT") != null ? agentConfig.getProperty("PORT") : sITE247_PORT;
		return Integer.parseInt(portstr);
	}

	public static String getAcHost() {
		return agentConfig.getProperty("ACHOST") != null ? agentConfig.getProperty("ACHOST") : aC_HOST;
	}

	public static int getAcPort() {
		String portstr = agentConfig.getProperty("ACPORT") != null ? agentConfig.getProperty("ACPORT") : aC_PORT;
		return Integer.parseInt(portstr);
	}

	public static String getAcUrl() {
		String url = new StringBuilder().append("https://").append(getAcHost()).append(":").append(getAcPort())
				.toString();
		return url;
	}

	public static String getMonAgentID() {
		return agentConfig.getProperty("MONAGENTID");
	}

	public static String getAgentUniqueID() {
		return agentConfig.getProperty("AGENTUID");
	}

	public static String getAgentTimeZone() {
		if (agentConfig.containsKey("TIMEZONE")) {
			return agentConfig.getProperty("TIMEZONE");
		}

		return null;
	}

	public static void setAgentTimeZone(String tz, Boolean bool) {
		if (bool.booleanValue()) {
			agentConfig.setProperty("TIMEZONE", tz);
			Util.updatePropertyFile(
					new StringBuilder().append("conf").append(File.separator).append("serveragent.config").toString(),
					agentConfig);
		} else if (!agentConfig.containsKey("TIMEZONE")) {
			agentConfig.setProperty("TIMEZONE", tz);
			Util.updatePropertyFile(
					new StringBuilder().append("conf").append(File.separator).append("serveragent.config").toString(),
					agentConfig);
		}
	}

	public static int getCategory() {
		File[] fileList = getPluginFiles();

		if ((fileList != null) && (fileList.length > 0)) {
			return Util.isWindows() ? 4 : 5;
		}

		return Util.isWindows() ? 0 : 1;
	}

	private static File[] getPluginFiles() {
		File[] fileList = null;
		File f = new File(System.getProperty("eum.home"));
		fileList = f.listFiles(fileFilter);
		return fileList;
	}

	public static String getAgentOSDetails() {
		String details = System.getProperty("os.name");
		if (details.toLowerCase().indexOf("win") > -1) {
			
		} else {
			details = new StringBuilder().append(details).append(" <br>OS Details : ")
					.append(System.getProperty("os.arch")).toString();
			details = new StringBuilder().append(details).append(",").append(System.getProperty("os.version"))
					.toString();
			details = new StringBuilder().append(details).append(" <br>No of Processors : ")
					.append(System.getenv("NUMBER_OF_PROCESSORS") == null ? "-" : System.getenv("NUMBER_OF_PROCESSORS"))
					.toString();
		}
		details = new StringBuilder().append(details).append(" <br>Processor Details : ")
				.append(System.getenv("PROCESSOR_IDENTIFIER") == null ? "-" : System.getenv("PROCESSOR_IDENTIFIER"))
				.toString();
		details = new StringBuilder().append(details).append(", ")
				.append(System.getenv("PROCESSOR_ARCHITECTURE") == null ? "-" : System.getenv("PROCESSOR_ARCHITECTURE"))
				.toString();
		details = new StringBuilder().append(details).append(", System Type : ")
				.append(System.getProperty("sun.arch.data.model")).append("-bit OS").toString();

		return details;
	}

	public static String getAgentPlatform() {
		String details = System.getProperty("os.name");
		if (details.toLowerCase().indexOf("win") > -1) {
			details = null;
		}
		return details;
	}

	public String toString() {
		return agentConfig.toString();
	}

	public static Properties getAgentConfig() {
		return agentConfig;
	}

	public static void setAgentConfig(Properties config) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(
					new StringBuilder().append("conf").append(File.separator).append("serveragent.config").toString());
			config.store(out, "");
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public static String getAgentVersion() {
		return AGENT_VERSION;
	}

	public static String getProductId() {
		return wmsPRODUCTID;
	}

	public static boolean isProxyNeeded() {
		return (agentConfig.getProperty("PROXY") != null)
				&& (agentConfig.getProperty("PROXY").equalsIgnoreCase("true"));
	}

	public static String getProxyHost() {
		return agentConfig.getProperty("PROXY_HOST");
	}

	public static String getProxyPort() {
		return Util.getProperty(agentConfig, "PROXY_PORT", "80");
	}

	public static String getProxyUser() {
		return agentConfig.getProperty("PROXY_USERNAME");
	}

	public static String getProxyPwd() {
		return agentConfig.getProperty("PROXY_PASSWORD");
	}

	public static long getProbeServerPollTime() {
		return pROBE_SERVER_POLLTIME.longValue();
	}

	public static String getProtocols() {
		String protocol = "TLSv1";
		String protos = System.getProperty("communication.protocol");
		if ((protos != null) && (!protos.equals(""))) {
			protocol = protos;
		}
		return protocol;
	}
}