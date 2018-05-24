package org.opennms.core.http;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class ObjectHolder {
	public HashMap serverVsConObj = new HashMap();
	public static ObjectHolder obj = null;
	private static long serverAgentTimeDiff = 0L;
	private static int threadPoolSize = 100;
	public static Properties classes = new Properties();
	public static Properties modules = new Properties();
	public static Hashtable instance = new Hashtable();
	public static URL[] jarsUrl = addJars();
	public static Properties mailServerProps = new Properties();
	public static boolean ppmUpdationCheck = true;
	public static String errorMessage = "";
	public static boolean isThreadPollNeeded = true;
	public static String agentName = "localhost";
	public static String agentIP = "";
	private static boolean isSite24x7 = false;
	private static boolean isNewtorkSanityCheckRequired = false;
	public static Hashtable<String, Logger> nameVsLogger = new Hashtable();
	public static boolean isLoggerInitialized = false;
	public static boolean isFailedReportSenderTaskInitiated = false;
	public static String[] pollernetworkcheckurls = null;
	public static boolean isProbe = false;
	public static boolean isOneMinuteMonitoring = false;
	public static String oNE_MINUTE_MONITORING = "";
	public static String aPP_NAME = "APPSERVER";
	public static Properties pluginModules = new Properties();
	private Queue<Integer> requestIdQueue = new LinkedList();
	private ScheduledFuture<?> schFuture = null;

	public static boolean isNewtorkSanityCheckRequired() {
		return isNewtorkSanityCheckRequired;
	}

	public static String[] getPollernetworkcheckurls() {
		return pollernetworkcheckurls;
	}

	public static void setNewtorkSanityCheckRequired(
			boolean isNewtorkSanityCheckRequired) {
		isNewtorkSanityCheckRequired = isNewtorkSanityCheckRequired;
	}

	public static void setPollernetworkcheckurls(String[] pollernetworkcheckurls) {
		pollernetworkcheckurls = pollernetworkcheckurls;
	}

	public static void setSite24x7(boolean s24x7) {
		isSite24x7 = s24x7;
		if (isSite24x7) {
			setAppName("s24x7+");
		}
	}

	public static void setAppName(String app) {
		aPP_NAME = app;
	}

	public static boolean isSite24x7() {
		return isSite24x7;
	}

	public static String getAgentIP() {
		return agentIP;
	}

	public static void setAgentIP(String agentIP) {
		agentIP = agentIP;
	}

	public static String getAgentName() {
		return agentName;
	}

	public static void setAgentName(String agentName) {
		agentName = agentName;
	}

	public static int getThreadPoolSize() {
		return threadPoolSize;
	}

	public static void setThreadPoolSize(int threadPoolSize) {
		threadPoolSize = threadPoolSize;
	}

	public static boolean isThreadPollNeeded() {
		return isThreadPollNeeded;
	}

	public static void setThreadPollNeeded(boolean isThreadPollNeeded) {
		isThreadPollNeeded = isThreadPollNeeded;
	}

	public static boolean getPpmUpdationCheck() {
		return ppmUpdationCheck;
	}

	public static void setPpmUpdationCheck(boolean ppm) {
		ppmUpdationCheck = ppm;
	}

	public static String getErrorMessage() {
		return errorMessage;
	}

	public static void setErrorMessage(String msg) {
		errorMessage = msg;
	}

	public HashMap getServerVsConObj() {
		return this.serverVsConObj;
	}

	public static ObjectHolder getObj() {
		return obj;
	}

	public static Properties getClasses() {
		return classes;
	}

	public static Properties getModules() {
		return modules;
	}

	public static void setModules(Properties moduleProps) {
		modules = moduleProps;
		Util.updatePropertyFile(System.getProperty("eum.home") + File.separator
				+ "conf" + File.separator + "modules.conf", modules);
	}

	public static Properties getpluginModules() {
		return pluginModules;
	}

	public static URL[] getJarsUrl() {
		return jarsUrl;
	}

	public static ObjectHolder getInstance() {
		if (obj == null) {
			obj = new ObjectHolder();
		}
		return obj;
	}

	public static URLClassLoader getUrlClassLoader() {
		URLClassLoader ucl = null;
		try {
			ucl = new URLClassLoader(jarsUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ucl;
	}

	public static void readClasses() {
		if (System.getProperty("eum.home") == null) {
			return;
		}

		String classpathFile = System.getProperty("eum.home") + File.separator
				+ "conf" + File.separator + "classes.conf";
		FileInputStream fis = null;
		classes = new Properties();
		try {
			fis = new FileInputStream(new File(classpathFile));
			classes.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	public static void readModules() {
		modules = readProperties(System.getProperty("eum.home")
				+ File.separator + "conf" + File.separator + "modules.conf");
	}

	public static void readPluginModles() {
		pluginModules = readProperties(System.getProperty("eum.home")
				+ File.separator + "conf" + File.separator
				+ "PluginModules.conf");
	}

	public static void readEUMServerConfigurations() {
		String poller = System.getProperty("agent.probe");
		if ((poller != null) && (poller.equalsIgnoreCase("true"))) {
			Properties confProp = null;
			String confFile = System.getProperty("eum.home") + File.separator
					+ "conf" + File.separator + "EUMServer.properties";
			try {
				confProp = readProperties(confFile);
				if ((confProp != null) && (confProp.size() > 0)
						&& (confProp.containsKey("agent.oneminute.monitoring"))) {
					oNE_MINUTE_MONITORING = (String) confProp
							.get("agent.oneminute.monitoring");
					if (oNE_MINUTE_MONITORING.contains(",")) {
						oNE_MINUTE_MONITORING = oNE_MINUTE_MONITORING.replace(
								",", "|");
					}
					if (!oNE_MINUTE_MONITORING.equals("")) {
						setOneMinuteMonitoring(true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return;
		}
	}

	public static Properties readProperties(String propFile) {
		FileInputStream fis = null;
		Properties prop = new Properties();
		try {
			if ((propFile != null) && (new File(propFile).exists())) {
				fis = new FileInputStream(new File(propFile));
				prop.load(fis);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}

	public static URL[] addJars() {
		if (System.getProperty("eum.home") == null) {
			return new URL[0];
		}

		URL[] urls = null;
		try {
			String classpathFile = System.getProperty("eum.home")
					+ File.separator + "conf" + File.separator
					+ "classpath.conf";
			FileInputStream fis = null;
			Properties cpProps = new Properties();
			try {
				fis = new FileInputStream(new File(classpathFile));
				cpProps.load(fis);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}

			ArrayList jarsToLoad = new ArrayList();
			Enumeration e = cpProps.propertyNames();
			String excludeJars = "";
			if (cpProps.containsKey("exclude")) {
				excludeJars = "" + cpProps.remove("exclude");
			}
			ArrayList exclude = new ArrayList();
			if (!excludeJars.equals("")) {
				try {
					List list = Arrays.asList(excludeJars.split(","));
					exclude.addAll(list);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			while (e.hasMoreElements()) {
				String dirName = cpProps.getProperty((String) e.nextElement());
				File jarsList = new File(System.getProperty("eum.home")
						+ File.separator + dirName);

				for (File jar : jarsList.listFiles()) {
					if ((exclude.contains(jar.getName()))
							|| ((!jar.getName().endsWith(".jar"))
									&& (!jar.getName().endsWith(".zip")) && (!jar
										.isDirectory())))
						continue;
					jarsToLoad.add(new URL("file", null, jar.getPath()));
				}

			}

			int jarListSize = jarsToLoad.size();
			urls = new URL[jarListSize];
			for (int i = 0; i < jarsToLoad.size(); i++) {
				urls[i] = ((URL) jarsToLoad.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
	}

	public Properties getAgentProps() {
		Properties prop = new Properties();
		try {
			prop = readProperties(System.getProperty("eum.home")
					+ File.separator + "conf" + File.separator + "agent.conf");
		} catch (Exception e) {
		}
		return prop;
	}

	public Properties getMailServerProps() {
		return mailServerProps;
	}

	public void setMailServerProps(Properties props) {
		mailServerProps = props;
	}

	public void setServerAgentTimeDiff(long diff) {
		serverAgentTimeDiff = diff;
	}

	public long getServerAgentTimeDiff() {
		return serverAgentTimeDiff;
	}

	public Logger getLogger(String mod) {
		try {
			if (isLoggerInitialized) {
				if (nameVsLogger.containsKey(mod)) {
					return (Logger) nameVsLogger.get(mod);
				}

				return (Logger) nameVsLogger.get("default");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Logger.getLogger(mod);
	}

	public void setLogger(String name, Logger l) {
		nameVsLogger.put(name, l);
	}

	public boolean isFailedReportSenderTaskInitiated() {
		return isFailedReportSenderTaskInitiated;
	}

	public void setFailedReportSenderTaskInitiated(boolean val) {
		isFailedReportSenderTaskInitiated = val;
	}

	public static boolean isProbe() {
		return isProbe;
	}

	public static void setProbe(boolean isProbe) {
		isProbe = isProbe;
		if (isProbe) {
			setAppName("s24x7+");
		}
	}

	public static void setOneMinuteMonitoring(boolean isOneMinute) {
		isOneMinuteMonitoring = isOneMinute;
	}

	public static boolean isOneMinuteMonitoring() {
		return isOneMinuteMonitoring;
	}

	public boolean isOneMinutePollingEnabled(String type,
			Hashtable configurations) {
		boolean returnValue = Boolean.FALSE.booleanValue();
		if (((isOneMinuteMonitoring()) && (type.matches(oNE_MINUTE_MONITORING)))
				|| ((configurations.containsKey("one_minute_monitoring")) && (configurations
						.get("one_minute_monitoring").equals("1")))) {
			returnValue = Boolean.TRUE.booleanValue();
		}
		return returnValue;
	}

	public Queue<Integer> getRequestIds() {
		return this.requestIdQueue;
	}

	public void addRequestIds(int reqId) {
		if (this.requestIdQueue.size() > 10) {
			this.requestIdQueue.remove();
		}
		this.requestIdQueue.add(Integer.valueOf(reqId));
	}

	public ScheduledFuture<?> getPluginTask() {
		return this.schFuture;
	}

	public void setPluginTask(ScheduledFuture<?> sfuture) {
		this.schFuture = sfuture;
	}

	static {
		readClasses();
		readModules();
		readPluginModles();
		readEUMServerConfigurations();
	}
}