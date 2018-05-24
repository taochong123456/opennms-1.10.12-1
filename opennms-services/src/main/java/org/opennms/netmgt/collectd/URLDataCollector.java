package org.opennms.netmgt.collectd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opennms.core.http.NVPair;
import org.opennms.core.http.ObjectHolder;
import org.opennms.core.http.Util;
import org.opennms.protocols.http.HttpClientHolder;
import org.opennms.protocols.http.HttpConnection;
import org.opennms.protocols.http.Response;
import org.opennms.protocols.http.ServerAgentConstants;
import org.w3c.dom.Document;
public class URLDataCollector {
	public static boolean isTempFolderLoaded = false;
	private static String url_response_temp_folder = "";
	public static final String COOKIE_PARAM_DELIMITTER = ":-:";
	public static final String COOKIE_PARAM_START = "COOKIE:-:";
	public static final String TEXT_PARAM_DELIMITTER = ":-:";
	public static final String TEXT_PARAM_START = "TEXT:-:";
	public static final String URL_PARAM_DELIMITTER = ":-:";
	public static final String URL_PARAM_START = "URL:-:";
	public static int urlReqCount = 0;
	private static Logger logger = Logger.getLogger("URL");
	public static final int AVAILABLE = 1;
	public static final int UNAVAILABLE = 0;
	public static final int TROUBLE = 2;
	public static final int CASESENSITIVE = -1;
	private String headernames = "";
	private String headervalues = "";
	private String useragent = "";
	private String resolvedip = "";
	private String responseHeaders = "";
	private String requestHeaders = "";
	private boolean isProxyRequired = Boolean.FALSE.booleanValue();
	private boolean isProbeProxyRequired = Boolean.FALSE.booleanValue();
	private String proxyHost = "";
	private int proxyPort = 80;
	private String proxyUsername = "";
	private String proxyPassword = "";
	private String userroles = "FREEUSER";
	private String basicAuthUsername = "";
	private String basicAuthPassword = "";
	private String formSubmissionMethod = "G";
	private String requestBodyContent = "";
	private int timeout = 30;
	private boolean printlogs = Boolean.FALSE.booleanValue();
	private boolean sendHeaders = false;
	private String monitorid = "-1";
	private String queryString = "";
	private int currentstatus = 1;
	private String url = "";
	private String clientToUse = "4";

	private String ntlmDomainName = null;

	private String headersDelimitter = "_sep_";
	private HashMap<String, String> dynamicparams = null;

	private String context = "default";
	private int soTimeOut = -1;

	private String availableMatchString = "";
	private ArrayList<String> availableMatchStrings = new ArrayList();

	private String unAvailableMatchString = "";
	private ArrayList<String> unAvailableMatchStrings = new ArrayList();
	private ArrayList<String> matchStrings = new ArrayList();
	private int caseSensitive = -1;
	private String regExString = "";
	private String xpathString = "";
	private String locationId = "";
	private boolean isRegExConfigured = Boolean.FALSE.booleanValue();
	private boolean isXPathConfigured = Boolean.FALSE.booleanValue();
	private boolean ismd5ContentConfigured = Boolean.FALSE.booleanValue();
	private boolean isAvailabilityStringConfigured = Boolean.FALSE
			.booleanValue();
	private boolean isUnAvailabilityStringConfigured = Boolean.FALSE
			.booleanValue();

	private boolean stackOverFlowError = Boolean.FALSE.booleanValue();
	private String htmlresponse = "";
	private int responsecode = 0;
	private int availability = 1;
	private String refererUrl = "";
	private boolean isTraceRouteRequired = Boolean.FALSE.booleanValue();

	private boolean retry = Boolean.FALSE.booleanValue();
	boolean remoteAgent = Boolean.FALSE.booleanValue();

	private String idcLocUrl = "https://www.site24x7.com/receivemultipartdata";

	private int retryDelay = 5;
	private int errorCode = 0;
	private String exceptionErrorMsg = "";
	private long responsetime = -1L;
	private long dnstime = -1L;
	private long connectiontime = -1L;
	private long firstbytetime = -1L;
	private long lastbytetime = -1L;
	private long downloadtime = -1L;
	private long headcontentlength = 0L;
	private String md5String = "";
	private boolean regExStatus = Boolean.FALSE.booleanValue();
	private String availableFailedString = "";
	private String unAvailableFailedString = "";
	private String xmlresponse = "";
	private String urlXmlresponse = "";
	private HashMap<String, Object> responsemap = new HashMap();

	private String monitortype = "URL";
	private String collectionTime = "";
	private String userid = "";

	private String dfsBlockId = null;
	private String dfsFilePath = null;

	private int availabilityContentStatusAlertType = 2;
	private int unAvailabilityContentStatusAlertType = 2;
	private int regExContentStatusAlertType = 2;

	private boolean storeHtmlResp = Boolean.FALSE.booleanValue();
	private String postUrl = "site24x7.com";

	private String customDomainResolver = "";
	private boolean dnsEnabled = Boolean.FALSE.booleanValue();
	private boolean isDiscovery = false;

	private boolean isHtmlResponseRequired = false;
	private boolean reCheckHtmlResponse = Boolean.TRUE.booleanValue();
	private String xmd5String = "";
	private long xcontentLength = -1L;
	private boolean iscontentChange = Boolean.FALSE.booleanValue();

	private String propName = "";
	private String propValue = "";
	private String responseType = "";
	private String jsonFailedString = "";
	private String xmlFailedString = "";

	private String ssl_protocols = "";

	private StringBuilder logs = new StringBuilder();
	private boolean returnRespStr = false;
	private Hashtable clonedConfig;

	private HashMap<String, Object> getResponseProps() {
		this.responseHeaders = Util.trim(this.responseHeaders);
		if (this.responseHeaders.length() > 0) {
			this.responseHeaders = Util.escapeXML(this.responseHeaders);
		}
		this.requestHeaders = Util.trim(this.requestHeaders);
		if (this.requestHeaders.length() > 0) {
			this.requestHeaders = Util.escapeXML(this.requestHeaders);
		}

		this.responsemap.put("availability", String.valueOf(this.availability));
		this.responsemap.put("isTraceRouteRequired",
				String.valueOf(this.isTraceRouteRequired));
		this.responsemap.put("error_code", String.valueOf(this.errorCode));
		this.responsemap.put("exErrMsg", this.exceptionErrorMsg);
		this.responsemap.put("responsetime", String.valueOf(this.responsetime));
		this.responsemap.put("mid", String.valueOf(this.monitorid));
		this.responsemap.put("responsecode", String.valueOf(this.responsecode));
		this.responsemap.put("dnstime", String.valueOf(this.dnstime));
		this.responsemap.put("connectiontime",
				String.valueOf(this.connectiontime));
		this.responsemap.put("firstbytetime",
				String.valueOf(this.firstbytetime));
		this.responsemap.put("lastbytetime", String.valueOf(this.lastbytetime));
		if (this.isHtmlResponseRequired) {
			this.responsemap.put("htmlresponse",
					String.valueOf(this.htmlresponse));
		}
		this.downloadtime = (this.lastbytetime - this.firstbytetime);
		if ((this.downloadtime < 0L) && (this.availability == 1)) {
			this.downloadtime = 0L;
		}
		this.responsemap.put("downloadtime", String.valueOf(this.downloadtime));
		this.responsemap.put("refererUrl", this.refererUrl);
		this.responsemap.put("resolvedip", this.resolvedip);
		if ((this.dfsFilePath != null) && (this.dfsBlockId != null)) {
			this.responsemap.put("dfsfp", this.dfsFilePath);
			this.responsemap.put("dfsbid", this.dfsBlockId);
		}
		this.responsemap.put("responseHeaders", this.responseHeaders);
		this.responsemap.put("requestHeaders", this.requestHeaders);
		this.responsemap.put("stackOverFlowError",
				String.valueOf(this.stackOverFlowError));
		this.responsemap.put("headcontentlength",
				String.valueOf(this.headcontentlength));
		this.availableFailedString = trim(this.availableFailedString);
		this.unAvailableFailedString = trim(this.unAvailableFailedString);
		this.md5String = trim(this.md5String);
		if (this.ismd5ContentConfigured) {
			this.responsemap.put("md5String", this.md5String);
		}
		if (this.isRegExConfigured) {
			this.responsemap.put("regExStatus",
					String.valueOf(this.regExStatus));
		}
		if (this.isAvailabilityStringConfigured) {
			this.responsemap.put("availableFailedString",
					this.availableFailedString);
		}
		if (this.isUnAvailabilityStringConfigured) {
			this.responsemap.put("unAvailableFailedString",
					this.unAvailableFailedString);
		}
		this.responsemap.put("rt", this.responseType);
		if ((this.responseType.equals("J")) || (this.responseType.equals("X"))) {
			this.responsemap.put("propValue", this.propValue);
			this.responsemap.put("propName", this.propName);
		}
		this.responsemap.put("locationId", String.valueOf(this.locationId));

		getResponseInXml();
		this.responsemap.put("xmlResponse", this.xmlresponse);
		this.responsemap.put("UrlXmlresponse", this.urlXmlresponse);
		return this.responsemap;
	}

	public void resetOldValues() {
		this.availability = 1;
	}

	public HashMap<String, String> getDynamicProps() {
		return this.dynamicparams;
	}

	public void getURLResponseInXml() {
		StringBuilder sb = new StringBuilder("\n<Url");
		sb.append(" locid=\"").append(String.valueOf(this.locationId))
				.append("\"");
		sb.append(" mid=\"").append(String.valueOf(this.monitorid))
				.append("\"");
		sb.append(" dcstatus=\"1\"");
		if ((this.ismd5ContentConfigured) && (this.md5String.length() > 0)) {
			sb.append(" md5=\"").append(this.md5String).append("\"");
		} else {
			sb.append(" md5=\"").append("").append("\"");
		}
		if ((this.isAvailabilityStringConfigured)
				&& (this.availableFailedString.length() > 0)) {
			sb.append(" acontentcheck=\"").append(String.valueOf(Boolean.TRUE))
					.append("\"");
			sb.append(" accStr=\"")
					.append(Util.escapeXML(this.availableFailedString))
					.append("\"");
		} else {
			sb.append(" acontentcheck=\"")
					.append(String.valueOf(Boolean.FALSE)).append("\"");
			sb.append(" accStr=\"").append("").append("\"");
		}
		if ((this.isUnAvailabilityStringConfigured)
				&& (this.unAvailableFailedString.length() > 0)) {
			sb.append(" ucontentcheck=\"").append(String.valueOf(Boolean.TRUE))
					.append("\"");
			sb.append(" uccStr=\"")
					.append(Util.escapeXML(this.unAvailableFailedString))
					.append("\"");
		} else {
			sb.append(" ucontentcheck=\"")
					.append(String.valueOf(Boolean.FALSE)).append("\"");
			sb.append(" uccStr=\"").append("").append("\"");
		}
		if (this.responseType.equals("X")) {
			if (this.xmlFailedString.length() > 0) {
				sb.append(" xmlcheck=\"").append(String.valueOf(Boolean.TRUE))
						.append("\"");
				sb.append(" propName=\"").append(Util.escapeXML(this.propName))
						.append("\"");
				sb.append(" propValue=\"")
						.append(Util.escapeXML(this.propValue)).append("\"");
			} else {
				sb.append(" xmlcheck=\"").append(String.valueOf(Boolean.FALSE))
						.append("\"");
				sb.append(" propName=\"").append(Util.escapeXML(this.propName))
						.append("\"");
				sb.append(" propValue=\"")
						.append(Util.escapeXML(this.propValue)).append("\"");
			}
		} else if (this.responseType.equals("J")) {
			if (this.jsonFailedString.length() > 0) {
				sb.append(" jsoncheck=\"").append(String.valueOf(Boolean.TRUE))
						.append("\"");
				sb.append(" propName=\"").append(Util.escapeXML(this.propName))
						.append("\"");
				sb.append(" propValue=\"")
						.append(Util.escapeXML(this.propValue)).append("\"");
			} else {
				sb.append(" jsoncheck=\"")
						.append(String.valueOf(Boolean.FALSE)).append("\"");
				sb.append(" propName=\"").append(Util.escapeXML(this.propName))
						.append("\"");
				sb.append(" propValue=\"")
						.append(Util.escapeXML(this.propValue)).append("\"");
			}
		}
		sb.append(" tp=\"")
				.append(String.valueOf(Util.getThroughputValue(
						Long.valueOf(this.responsetime),
						Long.valueOf(this.headcontentlength)))).append("\"");
		sb.append(" ct=\"").append(String.valueOf(this.collectionTime))
				.append("\"");
		sb.append(" availability=\"").append(String.valueOf(this.availability))
				.append("\"");
		sb.append(" responseCode=\"").append(String.valueOf(this.responsecode))
				.append("\"");
		if ((this.responsecode > 0) && (this.responsecode != 200)) {
			this.errorCode = 1001;
		}
		sb.append(" responseTime=\"").append(String.valueOf(this.responsetime))
				.append("\"");
		sb.append(" dnstime=\"").append(String.valueOf(this.dnstime))
				.append("\"");
		sb.append(" connectiontime=\"")
				.append(String.valueOf(this.connectiontime)).append("\"");
		sb.append(" firstbytetime=\"")
				.append(String.valueOf(this.firstbytetime)).append("\"");
		sb.append(" lastbytetime=\"").append(String.valueOf(this.lastbytetime))
				.append("\"");
		sb.append(" downloadtime=\"").append(String.valueOf(this.downloadtime))
				.append("\"");
		sb.append(" error_code=\"").append(String.valueOf(this.errorCode))
				.append("\"");

		sb.append(" regex_check=\"").append(String.valueOf(this.regExStatus))
				.append("\"");
		if ((this.regExStatus) && (this.regExString.length() > 0))
			sb.append(" regex=\"").append(Util.escapeXML(this.regExString))
					.append("\"");
		else {
			sb.append(" regex=\"").append("").append("\"");
		}
		sb.append(" contentLength=\"")
				.append(String.valueOf(this.headcontentlength)).append("\"");
		sb.append(" isTraceRouteRequired=\"")
				.append(String.valueOf(this.isTraceRouteRequired)).append("\"");
		sb.append(" resolvedip=\"").append(this.resolvedip).append("\"");
		if (this.isDiscovery) {
			sb.append(" discovery=\"true\"");
		}
		if (((this.currentstatus == 1) && ((this.availability == 0)
				|| (!this.availableFailedString.equals("")) || (!this.unAvailableFailedString
					.equals("")))) || (this.sendHeaders)) {
			sb.append(" responseHeaders=\"")
					.append(Util.trim(this.responseHeaders)).append("\"");
			sb.append(" requestHeaders=\"")
					.append(Util.trim(this.requestHeaders)).append("\"");
		} else {
			sb.append(" responseHeaders=\"").append("").append("\"");
			sb.append(" requestHeaders=\"").append("").append("\"");
		}
		if ((this.dfsFilePath != null) && (this.dfsBlockId != null)) {
			sb.append(" dfsfp=\"").append(String.valueOf(this.dfsFilePath))
					.append("\"");
			sb.append(" dfsbid=\"").append(String.valueOf(this.dfsBlockId))
					.append("\"");
		}
		if ((this.refererUrl != null) && (this.refererUrl.trim().length() > 1)) {
			sb.append(" refererUrl=\"").append(Util.escapeXML(this.refererUrl))
					.append("\"");
		} else {
			sb.append(" refererUrl=\"").append(Util.escapeXML(this.url))
					.append("\"");
		}
		sb.append("/>");
		this.urlXmlresponse = sb.toString();
	}

	private void getResponseInXml() {
		StringBuilder sb = new StringBuilder(
				"<?xml version=\"1.0\" standalone=\"no\"?>");
		getURLResponseInXml();
		sb.append(this.urlXmlresponse);
		this.xmlresponse = sb.toString();
	}

	public HashMap discover(Hashtable config) {
		this.isDiscovery = true;

		return startDatacollection(config);
	}

	public HashMap startDatacollection(Hashtable config) {
		try {
			setProperties(config);
			this.url = Util.getProperty(config, "a", this.url);

			this.clonedConfig = ((Hashtable) config.clone());
			doDataCollection();
			if ((this.retry) && (this.availability == 0)) {
			}
			if (this.returnRespStr) {
				HashMap respMap = new HashMap();
				respMap.put("responsecode", String.valueOf(this.responsecode));
				respMap.put("htmlresponse", String.valueOf(this.htmlresponse));
				respMap.put("errorCode", String.valueOf(this.errorCode));
				respMap.put("errMsg", this.exceptionErrorMsg);
				HashMap localHashMap1 = respMap;
				return localHashMap1;
			}
			getResponseProps();
		} catch (Exception ex) {
		} finally {
		}
		return this.responsemap;
	}

	private void setProperties(Hashtable config) {
		this.monitorid = Util.getProperty(config, "mid", new StringBuilder()
				.append("").append(this.monitorid).toString());
		this.monitortype = Util.getProperty(config, "mt", "");
		this.collectionTime = Util.getProperty(
				config,
				"ct",
				new StringBuilder().append(System.currentTimeMillis())
						.append("").toString());
		this.userid = Util.getProperty(config, "uid", null);
		this.headernames = Util.getProperty(config, "hn", this.headernames);
		this.headervalues = Util.getProperty(config, "hv", this.headervalues);
		this.headersDelimitter = Util.getProperty(config, "hd",
				this.headersDelimitter);
		this.useragent = Util.getProperty(config, "ua", this.useragent);
		if ((this.useragent.length() == 0) && (ObjectHolder.isSite24x7())) {
			this.useragent = "Site24x7";
		}
		this.isProxyRequired = Boolean.valueOf(
				Util.getProperty(config, "isProxyRequired",
						String.valueOf(Boolean.FALSE))).booleanValue();
		this.sendHeaders = Boolean.valueOf(
				Util.getProperty(config, "sendHeaders",
						String.valueOf(Boolean.FALSE))).booleanValue();
		this.customDomainResolver = Util.getProperty(config, "ds",
				this.customDomainResolver);
		this.dnsEnabled = Boolean.valueOf(
				Util.getProperty(config, "enabledns",
						String.valueOf(Boolean.FALSE))).booleanValue();
		this.propName = Util.getProperty(config, "pn", this.propName);
		this.propValue = Util.getProperty(config, "pv", this.propValue);
		this.responseType = Util.getProperty(config, "rt", "");

		if (this.isProxyRequired) {
			this.proxyHost = Util.getProperty(config, "proxyHost",
					this.proxyHost);
			this.proxyPort = Integer.parseInt(Util.getProperty(config,
					"proxyPort",
					new StringBuilder().append("").append(this.proxyPort)
							.toString()));
			this.proxyUsername = Util.getProperty(config, "proxyUsername",
					this.proxyUsername);
			this.proxyPassword = Util.getProperty(config, "proxyPassword",
					this.proxyPassword);
		}
		if ((config.containsKey("webContent"))
				&& (config.get("webContent").toString().equalsIgnoreCase("yes"))) {
			this.isHtmlResponseRequired = true;
		}
		if (config.get("dynamicparams") != null) {
			this.dynamicparams = ((HashMap) config.get("dynamicparams"));
		}
		this.soTimeOut = Util.parseInt(
				Util.getProperty(config, "sotimeout", ""), -1);
		this.storeHtmlResp = Boolean.valueOf(
				Util.getProperty(config, "storeHtmlResp",
						String.valueOf(Boolean.FALSE))).booleanValue();

		this.userroles = Util.getProperty(config, "userroles", this.userroles);
		this.locationId = Util.getProperty(config, "locid", new StringBuilder()
				.append("").append(this.locationId).toString());
		this.basicAuthUsername = Util.getProperty(config, "un",
				this.basicAuthUsername);

		this.ntlmDomainName = Util.getProperty(config, "ntlmdomain",
				this.ntlmDomainName);

		this.basicAuthUsername = trim(this.basicAuthUsername);
		this.basicAuthPassword = Util.getProperty(config, "ps",
				this.basicAuthPassword);
		this.basicAuthPassword = trim(this.basicAuthPassword);
		this.context = Util.getProperty(config, "context", this.context);
		int logging = Util.parseInt(Util.getProperty(config, "log", "0"), 0);
		if (logging == 1) {
			this.printlogs = true;
		}
		this.retry = Boolean
				.valueOf(
						Util.getProperty(config, "retry",
								String.valueOf(Boolean.FALSE))).booleanValue();
		this.remoteAgent = Boolean.valueOf(
				Util.getProperty(config, "isRemoteAgent",
						String.valueOf(Boolean.FALSE))).booleanValue();
		String reqHostName = Util.getProperty(config, "reqHostName", null);
		String reqHostIP = Util.getProperty(config, "reqHostIP", null);
		String reqHostPort = "80";

		this.idcLocUrl = new StringBuilder().append("https://")
				.append(Util.getProperty(config, "postUrl", this.postUrl))
				.append("/receivemultipartdata").toString();
		this.retryDelay = Integer.parseInt(Util.getProperty(config,
				"retryDelay",
				new StringBuilder().append("").append(this.retryDelay)
						.toString()));

		this.ssl_protocols = Util.getProperty(config, "ssl_protocol",
				this.ssl_protocols);

		this.formSubmissionMethod = Util.getProperty(config, "m",
				this.formSubmissionMethod);
		this.requestBodyContent = Util.getProperty(config, "rbc",
				this.requestBodyContent);
		this.timeout = Integer
				.parseInt(Util.getProperty(config, "t", new StringBuilder()
						.append("").append(this.timeout).toString()));
		this.queryString = Util.getProperty(config, "PD", this.queryString);
		if ((this.formSubmissionMethod.startsWith("X"))
				|| (this.formSubmissionMethod.startsWith("J"))) {
			this.formSubmissionMethod = (this.formSubmissionMethod
					.contains("PUT") ? "U" : "P");
			if ((this.queryString.contains("{"))
					&& (this.queryString.contains(":"))
					&& (!this.queryString.contains("="))) {
				this.requestBodyContent = "J";
			} else {
				this.requestBodyContent = "X";
			}
		}

		this.currentstatus = Integer.parseInt(Util.getProperty(config,
				"currentstatus",
				new StringBuilder().append("").append(this.currentstatus)
						.toString()));
		this.url = Util.getProperty(config, "a", this.url);

		this.caseSensitive = Integer.parseInt(Util.getProperty(config,
				"keyword_case",
				new StringBuilder().append("").append(this.caseSensitive)
						.toString()));
		this.xpathString = Util.getProperty(config, "xpath", this.xpathString);
		this.regExString = Util.getProperty(config, "regex", this.regExString);
		this.regExContentStatusAlertType = Integer.parseInt(Util.getProperty(
				config, "regex_alert",
				new StringBuilder().append(this.regExContentStatusAlertType)
						.append("").toString()));
		if (trim(this.regExString).length() > 0) {
			this.isRegExConfigured = Boolean.TRUE.booleanValue();
		}
		if (trim(this.xpathString).length() > 0) {
			this.isXPathConfigured = Boolean.TRUE.booleanValue();
		}
		if (config.get("acc") != null) {
			this.availableMatchString = Util.getProperty(config, "acc",
					this.availableMatchString);
			this.availableMatchStrings = getStrings(this.availableMatchString);
			if (this.availableMatchStrings.size() > 0) {
				this.isAvailabilityStringConfigured = Boolean.TRUE
						.booleanValue();
			}
			this.availabilityContentStatusAlertType = Integer.parseInt(Util
					.getProperty(config, "avail_alert", new StringBuilder()
							.append(this.availabilityContentStatusAlertType)
							.append("").toString()));
		}
		if (config.get("ucc") != null) {
			this.unAvailableMatchString = Util.getProperty(config, "ucc",
					this.unAvailableMatchString);
			this.unAvailableMatchStrings = getStrings(this.unAvailableMatchString);
			if (this.unAvailableMatchStrings.size() > 0) {
				this.isUnAvailabilityStringConfigured = Boolean.TRUE
						.booleanValue();
			}
			this.unAvailabilityContentStatusAlertType = Integer.parseInt(Util
					.getProperty(config, "unavail_alert", new StringBuilder()
							.append(this.unAvailabilityContentStatusAlertType)
							.append("").toString()));
		}
		if (config.get("md") != null) {
			this.ismd5ContentConfigured = Boolean.valueOf(
					Util.getProperty(config, "md",
							String.valueOf(Boolean.FALSE))).booleanValue();
		}

		if (this.ismd5ContentConfigured) {
			this.xmd5String = Util.getProperty(config, "xmd5", "");
		}
		this.xcontentLength = Long.parseLong(Util.getProperty(config,
				"xconlen", new StringBuilder().append(this.xcontentLength)
						.append("").toString()));
		if (config.get("cc") != null) {
			this.iscontentChange = Boolean.valueOf(
					Util.getProperty(config, "cc",
							String.valueOf(Boolean.FALSE))).booleanValue();
		}
		if (config.get("probeproxy") != null) {
			this.isProbeProxyRequired = Boolean.valueOf(
					Util.getProperty(config, "probeproxy",
							String.valueOf(Boolean.FALSE))).booleanValue();
			if (this.isProbeProxyRequired) {
				this.proxyHost = ServerAgentConstants.getProxyHost();
				this.proxyPort = Integer.parseInt(ServerAgentConstants
						.getProxyPort());
				this.proxyUsername = ServerAgentConstants.getProxyUser();
				this.proxyPassword = ServerAgentConstants.getProxyPwd();
			}
		}
		if (config.get("returnRespStr") != null) {
			this.returnRespStr = Boolean.valueOf(
					Util.getProperty(config, "returnRespStr",
							String.valueOf(Boolean.FALSE))).booleanValue();
		}
	}

	private void doDataCollection() {
		urlReqCount += 1;
		HttpConnection con = null;
		this.clientToUse = trim(this.clientToUse);
		con = new HttpConnection(this.url);
		con.setLogger(logger);
		if (this.soTimeOut > 0) {
			con.setSOTimeOut(this.soTimeOut);
		}
		con.setContext(this.context);
		con.setSSLProtocols(this.ssl_protocols);
		con.setNameServer(this.customDomainResolver);
		con.setNameServerEnabled(this.dnsEnabled);
		Properties urlProp = new Properties();
		Boolean iscontentTypeHeaderAlreadySet = Boolean.FALSE;
		if (this.currentstatus == 1) {
			urlProp.put("currentstatus", "AVAILABLE");
		} else {
			urlProp.put("currentstatus", "UNAVAILABLE");
		}
		urlProp.put("userrole", this.userroles);
		con.setConnectionProperties(urlProp);
		if (!ObjectHolder.isSite24x7()) {
			con.setRedirectionLimit(100);
		}
		if ((this.isProxyRequired)
				|| ((this.isProbeProxyRequired) && (!getHostIp().startsWith(
						"192.168")))) {
			con.setProxyDetails(this.proxyHost, this.proxyPort,
					this.proxyUsername, this.proxyPassword);
		}
		con.setTimeout(this.timeout * 1000);
		ArrayList headerslist = new ArrayList();

		Properties encodingProps = new Properties();
		encodingProps.put("headername", "Accept-Encoding");
		encodingProps.put("headervalue", "gzip");
		NVPair gzipNVPair = new NVPair("Accept-Encoding", "gzip");
		encodingProps.put("nvpair", gzipNVPair);
		headerslist.add(encodingProps);
		if (this.useragent.length() > 0) {
			Properties useragentProps = new Properties();
			useragentProps.put("headername", "User-Agent");
			useragentProps.put("headervalue", this.useragent);
			NVPair useragentNVPair = new NVPair("User-Agent", this.useragent);
			useragentProps.put("nvpair", useragentNVPair);
			headerslist.add(useragentProps);
		}
		getHeaderNamesAndValues(this.headernames, this.headervalues,
				headerslist);

		if ((this.dynamicparams != null)
				&& (this.dynamicparams.containsKey("REFERER_URL"))
				&& (!this.clientToUse.equals("4"))) {
			Properties propss = new Properties();
			propss.put("headername", "Referer");
			propss.put("headervalue",
					(String) this.dynamicparams.get("REFERER_URL"));
			NVPair nvpair = new NVPair("Referer",
					(String) this.dynamicparams.get("REFERER_URL"));
			propss.put("nvpair", nvpair);
			headerslist.add(propss);
		}

		NVPair[] allheadersNVPair = new NVPair[headerslist.size()];
		for (int z = 0; z < headerslist.size(); z++) {
			Properties headerprops = (Properties) headerslist.get(z);
			String headername = headerprops.getProperty("headername");
			if ((!iscontentTypeHeaderAlreadySet.booleanValue())
					&& (headername.toLowerCase().equals("content-type"))) {
				iscontentTypeHeaderAlreadySet = Boolean.TRUE;
			}

			if (headername.toLowerCase().equals("host")) {
				String headervalue = headerprops.getProperty("headervalue");
				con.setHostHeader(headervalue);
			} else {
				NVPair headerNVPair = (NVPair) headerprops.get("nvpair");
				allheadersNVPair[z] = headerNVPair;
			}
		}
		con.setDefaultHeaders(allheadersNVPair);
		Response rsp = null;
		StringBuffer sbf = new StringBuffer();
		try {
			if ((this.formSubmissionMethod.matches("P|U|A"))
					&& (this.requestBodyContent.matches("X|J|T"))) {
				this.url = regenerateQueryString(this.url, this.dynamicparams);
				NVPair noCacheNVPair = new NVPair("Pragma", "no-cache");
				NVPair keepaliveNVPair = new NVPair("Connection", "Keep-Alive");
				NVPair acceptNVPair = new NVPair("Accept", "*/*");
				NVPair contenttypeNVPair = null;
				NVPair[] headers = { noCacheNVPair, acceptNVPair,
						keepaliveNVPair };
				if (!iscontentTypeHeaderAlreadySet.booleanValue()) {
					if (this.requestBodyContent.equals("X")) {
						contenttypeNVPair = new NVPair("Content-type",
								"text/xml; charset=UTF-8");
					} else if (this.requestBodyContent.equals("J")) {
						contenttypeNVPair = new NVPair("Content-type",
								"application/json");
					} else {
						contenttypeNVPair = new NVPair("Content-type",
								"text/plain");
					}
					headers = new NVPair[] { noCacheNVPair, acceptNVPair,
							keepaliveNVPair, contenttypeNVPair };
				}
				if (this.formSubmissionMethod.equals("U")) {
					rsp = con.PutXml(Util.getURI(this.url), this.queryString,
							headers);
				} else if (this.formSubmissionMethod.equals("A")) {
					rsp = con.PatchXml(Util.getURI(this.url), this.queryString,
							headers);
				} else {
					rsp = con.PostXml(Util.getURI(this.url), this.queryString,
							headers);
				}
				if (rsp.getEffectiveURL() != null) {
					this.refererUrl = rsp.getEffectiveURL().toString();
				} else if (rsp.getOriginalURI() != null) {
					this.refererUrl = rsp.getOriginalURI().toURL().toString();
				}
			} else if (this.formSubmissionMethod.matches("P|U|A")) {
				StringTokenizer tokens = new StringTokenizer(this.queryString,
						"&");
				NVPair[] formelements = null;
				ArrayList list = new ArrayList();
				while (tokens.hasMoreTokens()) {
					String keyvalue = tokens.nextToken();
					if (keyvalue.startsWith("$")) {
						keyvalue = keyvalue.substring(1);
					}
					int equalIndex = keyvalue.indexOf("=");
					String paramname = "";
					String paramvalue = "";
					if (equalIndex > 0) {
						paramname = keyvalue.substring(0, equalIndex);
						paramname = URLDecoder.decode(paramname);
						paramvalue = keyvalue.substring(equalIndex + 1);
						String s = null;
						if (this.dynamicparams != null) {
							s = (String) this.dynamicparams.get(paramname);
						}
						if (s != null) {
							paramvalue = s;
							if (paramvalue.startsWith("TEXT:-:")) {
								String[] stt = paramvalue.split(":-:");
								paramvalue = "";
								if (stt.length > 3) {
									paramvalue = stt[3];
								}
							} else if (paramvalue.startsWith("COOKIE:-:")) {
								String[] stt = paramvalue.split(":-:");
								paramvalue = "";
								if (stt.length > 2) {
									paramvalue = stt[2];
								}
							} else if (paramvalue.startsWith("URL:-:")) {
								String[] stt = paramvalue.split(":-:");
								paramvalue = "";
								if (stt.length > 3) {
									paramvalue = stt[3];
								}
							}
						} else {
							paramvalue = URLDecoder.decode(paramvalue);
						}
						sbf.append(new StringBuilder().append("\nparamname=")
								.append(paramname).toString());
						sbf.append(new StringBuilder().append("\nparamvalue=")
								.append(paramvalue).toString());
					}
					NVPair nvpair = new NVPair(paramname, paramvalue);
					list.add(nvpair);
				}

				formelements = (NVPair[]) list.toArray(new NVPair[list.size()]);
				this.url = regenerateQueryString(this.url, this.dynamicparams);
				NVPair noCacheNVPair = new NVPair("Pragma", "no-cache");
				NVPair acceptNVPair = new NVPair("Accept", "*/*");
				NVPair keepaliveNVPair = new NVPair("Connection", "Keep-Alive");
				NVPair[] headers = { noCacheNVPair, acceptNVPair,
						keepaliveNVPair };
				if (this.formSubmissionMethod.equals("U")) {
					rsp = con.Put(Util.getURI(this.url), formelements, headers);
				} else if (this.formSubmissionMethod.equals("A")) {
					rsp = con.Patch(Util.getURI(this.url), formelements,
							headers);
				} else {
					rsp = con
							.Post(Util.getURI(this.url), formelements, headers);
				}
				if (rsp.getEffectiveURL() != null) {
					this.refererUrl = rsp.getEffectiveURL().toString();
				} else if (rsp.getOriginalURI() != null) {
					this.refererUrl = rsp.getOriginalURI().toURL().toString();
				}
			} else {
				if (this.url.indexOf("?") != -1) {
					this.queryString = this.url.substring(
							this.url.indexOf("?") + 1, this.url.length());
					this.url = this.url.substring(this.url.indexOf("?"));
				}
				String tempuri = Util.getURI(this.url);
				if ((this.queryString != null)
						&& (this.queryString.trim().length() > 0)) {
					if ((this.dynamicparams != null)
							&& (this.dynamicparams.size() > 0)) {
						if (tempuri.indexOf(63) != -1) {
							int in = tempuri.indexOf(63);
							if (this.queryString.equals("")) {
								this.queryString = new StringBuilder()
										.append(tempuri.substring(in + 1))
										.append("&").append(this.queryString)
										.toString();
							}
							tempuri = tempuri.substring(0, in);
						}
						this.queryString = this.queryString.replace("$", "");
					}

					if (tempuri.indexOf(63) == -1) {
						tempuri = new StringBuilder().append(tempuri)
								.append("?").append(this.queryString)
								.toString();
					} else {
						tempuri = new StringBuilder().append(tempuri)
								.append("&").append(this.queryString)
								.toString();
					}
					tempuri = regenerateQueryString(tempuri, this.dynamicparams);
				}
				NVPair noCacheNVPair = new NVPair("Pragma", "no-cache");
				NVPair acceptNVPair = new NVPair("Accept", "*/*");
				NVPair keepaliveNVPair = new NVPair("Connection", "Keep-Alive");
				NVPair[] headers = { noCacheNVPair, acceptNVPair,
						keepaliveNVPair };
				if (this.formSubmissionMethod.equals("H")) {
					rsp = con.Head(tempuri.trim(), "", headers);
					int status = rsp.getStatusCode();
					if (status >= 400) {
						this.availability = 0;
						this.retry = Boolean.TRUE.booleanValue();
					} else {
						Enumeration enum1 = rsp.listHeaders();
						if (enum1 != null) {
							while (enum1.hasMoreElements()) {
								String k = (String) enum1.nextElement();
								if (k.equalsIgnoreCase("Content-Length")) {
									this.headcontentlength = rsp
											.getHeaderAsInt("Content-Length");
									break;
								}
							}

						}

						this.headcontentlength = 0L;
					}

				} else {
					if (this.formSubmissionMethod.equals("D")) {
						rsp = con.Delete(tempuri.trim(), "", headers);
					} else {
						rsp = con.Get(tempuri.trim(), "", headers);
					}
					if (rsp.getEffectiveURL() != null) {
						this.refererUrl = rsp.getEffectiveURL().toString();
					} else if (rsp.getOriginalURI() != null) {
						this.refererUrl = rsp.getOriginalURI().toURL()
								.toString();
					}
				}
			}

			byte[] wbresponse = rsp.getData();
			String contentype = rsp.getHeader("Content-Type");
			String charset = getCharSet(contentype);

			if (wbresponse != null) {
				if (charset == null) {
					this.htmlresponse = new String(wbresponse);
				} else {
					this.htmlresponse = readInput(wbresponse, charset);
				}

			}

			this.responsecode = rsp.getStatusCode();
			this.resolvedip = rsp.getIP();
			this.responseHeaders = rsp.getResponseHeaders();
			this.requestHeaders = rsp.getRequestHeaders();
			if (this.availability != 0) {
				if (this.responsecode >= 400) {
				} else {
				}
			}
			if ((ObjectHolder.isSite24x7())
					&& (!ObjectHolder.isProbe())
					&& (this.availability == 0)
					&& ((this.monitortype.equals("URL")) || (this.context
							.equals("default"))) && (this.responsecode == 615)) {
				this.clonedConfig.put("recheckScreenShotDC", "true");
				this.clonedConfig.put("maxLinksAllowed", Integer.valueOf(500));
				this.clonedConfig.put("maxPageSizeAllowed",
						Integer.valueOf(5120));
				this.clonedConfig.put("mt", "SCREENSHOT");
				this.clonedConfig.put("rmt", "URL");
				this.clonedConfig.put("md", "false");
				this.clonedConfig.put("t", Integer.valueOf(90));
				this.clonedConfig.put("retryDelay", Integer.valueOf(200));
				this.clonedConfig.put("dctimeout", Integer.valueOf(240));

				HashMap responsemap =null;
				this.availability = Util.parseInt(
						(String) responsemap.get("availability"),
						this.availability);

				if (this.availability == 1) {
					this.responsetime = Util.parseLong(
							(String) responsemap.get("responsetime"), -1L);

					this.dnstime = Util.parseLong(
							(String) responsemap.get("dnstime"), -1L);
					if ((this.dnstime < 0L)
							|| (this.dnstime > this.responsetime)) {
						this.dnstime = 0L;
					}
					this.connectiontime = Util.parseLong(
							(String) responsemap.get("connectiontime"), -1L);
					if ((this.connectiontime < 0L)
							|| (this.connectiontime > this.responsetime)) {
						this.connectiontime = 0L;
					}
					this.firstbytetime = Util.parseLong(
							(String) responsemap.get("firstbytetime"), -1L);
					if ((this.firstbytetime < 0L)
							|| (this.firstbytetime > this.responsetime)) {
						this.dnstime = 0L;
					}
					this.lastbytetime = Util.parseLong(
							(String) responsemap.get("lastbytetime"), -1L);
					if ((this.lastbytetime < 0L)
							|| (this.lastbytetime > this.responsetime)) {
						this.lastbytetime = 0L;
					}
					this.responsecode = Util.parseInt(
							(String) responsemap.get("responsecode"), -1);
					this.errorCode = Util.parseInt(
							(String) responsemap.get("error_code"), -1);
					this.availableFailedString = ((String) responsemap
							.get("availableFailedString"));
					this.unAvailableFailedString = ((String) responsemap
							.get("unAvailableFailedString"));
					this.regExString = ((String) responsemap.get("regExString"));
					this.dfsBlockId = ((String) responsemap.get("dfsbid"));
					this.dfsFilePath = ((String) responsemap.get("dfsfp"));

					parseScreenshotXmlResponse((String) responsemap
							.get("xmlResponse"));
				}
			}
		} catch (Exception e) {
		} finally {
			if (((this.monitortype.equals("URL")) || (this.context
					.equals("default"))) && (con != null)) {
				con.shutdown(this.context);
			}
			urlReqCount -= 1;
		}
	}

	public static void shutdown(String context) {
		HttpClientHolder.removeContext(context);
		HttpClientHolder.removeHttpClient(context);
	}

	public static int parseInt(String value, int defaultvalue) {
		try {
			value = value.trim();
			int val = Integer.parseInt(value);
			return val;
		} catch (Exception e) {
		}
		return defaultvalue;
	}

	

	private ArrayList<Properties> getHeaderNamesAndValues(String headers,
			String values, ArrayList<Properties> headerslist) {
		if ((headers != null) && (values != null)
				&& (headers.trim().length() > 0)
				&& (values.trim().length() > 0)) {
			String[] headertokens = headers.split(this.headersDelimitter);
			String[] valuetokens = values.split(this.headersDelimitter);
			Properties headerprops = new Properties();
			int k = 500;
			for (int i = 0; i < headertokens.length; i++) {
				String headername = headertokens[i];
				if (i >= valuetokens.length)
					continue;
				String headervalue = valuetokens[i];
				if ((headername == null) || (headervalue == null)
						|| (headername.trim().length() <= 0)
						|| (headervalue.trim().length() <= 0))
					continue;
				String key = String.valueOf(k);
				headerprops = new Properties();
				headerprops.setProperty("key", key);
				headerprops.setProperty("headername", headername);
				headerprops.setProperty("headervalue", headervalue);
				NVPair nvPair = new NVPair(headername, headervalue);
				headerprops.put("nvpair", nvPair);
				headerslist.add(headerprops);
				k++;
			}

		}

		return headerslist;
	}

	private String regenerateQueryString(String url,
			Map<String, String> dyanamicparams) {
		int question_index = url.indexOf("?");
		int length = url.length() - 1;
		if ((question_index > 0) && (length != question_index)) {
			String qstring = url.substring(question_index + 1);
			StringTokenizer qstringtokens = new StringTokenizer(qstring, "&");
			String finalstring = "";

			while (qstringtokens.hasMoreTokens()) {
				String qstringparam = qstringtokens.nextToken();

				if (qstringparam.indexOf("=") != -1) {
					String qstringparamname = qstringparam.substring(0,
							qstringparam.indexOf("="));
					String qstringparamvalue = qstringparam.substring(
							qstringparam.indexOf("=") + 1,
							qstringparam.length());
					String vv = null;
					if ((dyanamicparams != null)
							&& (dyanamicparams.get(qstringparamname) != null)) {
						vv = (String) dyanamicparams.get(qstringparamname);
					}

					if ((vv != null) && (vv.trim().length() > 0)) {
						qstringparamvalue = vv;
					}
					if (qstringparamvalue.startsWith("TEXT:-:")) {
						String[] stt = qstringparamvalue.split(":-:");
						qstringparamvalue = "";
						if (stt.length > 3) {
							qstringparamvalue = stt[3];
						}
					} else if (qstringparamvalue.startsWith("COOKIE:-:")) {
						String[] stt = qstringparamvalue.split(":-:");
						qstringparamvalue = "";
						if (stt.length > 2) {
							qstringparamvalue = stt[2];
						}
					} else if (qstringparamvalue.startsWith("URL:-:")) {
						String[] stt = qstringparamvalue.split(":-:");
						qstringparamvalue = "";
						if (stt.length > 3) {
							qstringparamvalue = stt[3];
						}
					} else if ((!isEncoded(qstringparamvalue)) && (!this.retry)) {
						qstringparamvalue = URLEncoder
								.encode(qstringparamvalue);
					}
					finalstring = new StringBuilder().append(finalstring)
							.append(qstringparamname).append("=")
							.append(qstringparamvalue).append("&").toString();
				} else {
					finalstring = new StringBuilder().append(finalstring)
							.append(qstringparam).append("&").toString();
				}
			}
			if (finalstring.length() > 0) {
				finalstring = finalstring
						.substring(0, finalstring.length() - 1);
			} else {
				finalstring = "";
			}
			url = new StringBuilder()
					.append(url.substring(0, url.indexOf("?") + 1))
					.append(finalstring).toString();
		}
		return url;
	}

	private boolean isEncoded(String param) {
		String[] splEncodings = { "%3B", "%3F", "%2F", "%3A", "%23", "%26",
				"%3D", "%2B", "%24", "%2C", "%20", "%25", "%3C", "%3E", "%7E",
				"%25" };
		for (int i = 0; i < splEncodings.length; i++) {
			if (param.indexOf(splEncodings[i]) != -1) {
				return Boolean.TRUE.booleanValue();
			}
		}
		return Boolean.FALSE.booleanValue();
	}

	private String getCharSet(String contenttype) {
		ArrayList availablecharsets = new ArrayList();

		for (Map.Entry en : Charset.availableCharsets().entrySet()) {
			availablecharsets.add(en.getKey());
		}
		try {
			StringTokenizer tokens = new StringTokenizer(contenttype, ";");
			tokens.nextToken();
			if (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				token = token.trim();
				tokens = new StringTokenizer(token, "=");
				String charset = "ISO-8859-1";
				if (tokens.hasMoreTokens()) {
					token = tokens.nextToken();
					token = token.trim();
					if (token.equalsIgnoreCase("charset")) {
						charset = tokens.nextToken().trim();
					}
				}
				if ((availablecharsets.contains(charset))
						|| (availablecharsets.contains(charset.toLowerCase()))
						|| (availablecharsets.contains(charset.toUpperCase()))) {
					return charset;
				}
			}
		} catch (Exception e) {
		}

		return null;
	}

	public static String readInput(byte[] buf, String contenttype) {
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = null;
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(buf);
			isr = new InputStreamReader(bis, contenttype);
			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();

			String str = buffer.toString();
			return str;
		} catch (IOException e) {

		} finally {
			try {
				bis.close();
				isr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contenttype;
	}

	private static String trim(String str) {
		try {
			str = str.trim();
		} catch (Exception e) {
			str = "";
		}
		return str;
	}

	private void doJSONCheck() {
		try {
			JSONArray jsonArray = new JSONArray();

			this.htmlresponse = this.htmlresponse.trim();
			String resp = this.htmlresponse;
			if (this.caseSensitive != 1) {
				resp = this.htmlresponse.toLowerCase();
				this.propName = this.propName.toLowerCase();
				this.propValue = this.propValue.toLowerCase();
			}
			JSONObject[] jsonObject = new JSONObject[jsonArray.length()];
			if ((resp.charAt(0) == '[')
					&& (resp.charAt(resp.length() - 1) == ']')) {
				jsonArray = new JSONArray(resp);
				for (int i = 0; i < jsonArray.length(); i++) {
					jsonObject[i] = new JSONObject(jsonArray.get(i).toString());
				}
			} else {
				jsonObject = new JSONObject[1];
				jsonObject[0] = new JSONObject(resp);
			}
			if ((this.propName.length() > 1)
					&& (this.propName.charAt(0) == '"')
					&& (this.propName.charAt(this.propName.length() - 1) == '"')) {
				this.propName = this.propName.substring(1,
						this.propName.length() - 1);
			}
			if ((this.propValue.length() > 1)
					&& (this.propValue.charAt(0) == '"')
					&& (this.propValue.charAt(this.propValue.length() - 1) == '"')) {
				this.propValue = this.propValue.substring(1,
						this.propValue.length() - 1);
			}
			for (JSONObject jsonObj : jsonObject) {
				if (jsonObj.has(this.propName)) {
					if (this.propValue.length() > 0) {
						if (jsonObj.get(this.propName).toString()
								.equals(this.propValue)) {
							this.jsonFailedString = "";
							break;
						}

						this.jsonFailedString = new StringBuilder()
								.append(this.propName).append(":")
								.append(this.propValue).toString();
					} else {
						this.jsonFailedString = "";
						break;
					}
				} else {
					this.jsonFailedString = new StringBuilder()
							.append(this.propName).append(":")
							.append(this.propValue).toString();
				}
			}
		} catch (JSONException je) {
			this.jsonFailedString = "Invalid JSON";
		} catch (Exception e) {
		}
	}

	private void doXMLCheck() {
		try {
			String resp = this.htmlresponse;
			if (this.caseSensitive != 1) {
				resp = this.htmlresponse.toLowerCase();
				this.propName = this.propName.toLowerCase();
				this.propValue = this.propValue.toLowerCase();
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(
					resp.getBytes());
			Document xmlDocument = builder.parse(stream);
			int flag = 0;
			XPath xPath = XPathFactory.newInstance().newXPath();
			org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) xPath
					.compile(this.propName).evaluate(xmlDocument,
							XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				String element_value = nodeList.item(i).getFirstChild()
						.getNodeValue();
				if (!element_value.equals(this.propValue))
					continue;
				flag = 1;
				break;
			}

			if (flag == 0) {
				this.xmlFailedString = "XML Validation failed";
			}
		} catch (Exception e) {
		}
	}

	private void doAvailabilityCheck() {
		this.matchStrings = new ArrayList();
		this.matchStrings = this.availableMatchStrings;
		if (this.isAvailabilityStringConfigured) {
			doResponseCheck(Boolean.TRUE.booleanValue());
		}
	}

	private void doUnAvailabilityCheck() {
		this.matchStrings = new ArrayList();
		this.matchStrings = this.unAvailableMatchStrings;
		if (this.isUnAvailabilityStringConfigured) {
			doResponseCheck(Boolean.FALSE.booleanValue());
		}
	}

	private void doResponseCheck(boolean equalitycheck) {
		int stringcheckCount = this.matchStrings.size();
		if ((this.htmlresponse != null) && (this.availability == 1)
				&& (stringcheckCount > 0)) {
			try {
				for (int i = 0; i < stringcheckCount; i++) {
					String temp = (String) this.matchStrings.get(i);
					String resp = this.htmlresponse;
					if (this.caseSensitive != 1) {
						resp = this.htmlresponse.toLowerCase();
						temp = temp.toLowerCase();
					}
					if (resp.indexOf(temp) == -1) {
						if (!equalitycheck)
							continue;
						this.availableFailedString = temp;
						break;
					}

					if (equalitycheck)
						continue;
					this.unAvailableFailedString = temp;
					break;
				}

			} catch (Exception e) {
			}
		}
	}

	private ArrayList<String> getStrings(String availbilitycontent) {
		char[] arr = availbilitycontent.toCharArray();
		boolean opendoublequotes = Boolean.TRUE.booleanValue();
		String temp = "";

		ArrayList list = new ArrayList();
		for (int k = 0; k < arr.length; k++) {
			char c = arr[k];

			if (c == '\\') {
				char tempchar = arr[(k + 1)];

				if (tempchar == '"') {
					temp = new StringBuilder().append(temp)
							.append(String.valueOf(tempchar)).toString();

					k++;
					continue;
				}
			}

			if ((c != '"') && (c != ' ')) {
				temp = new StringBuilder().append(temp)
						.append(String.valueOf(c)).toString();
			}

			if (c == '"') {
				if (opendoublequotes) {
					opendoublequotes = Boolean.FALSE.booleanValue();
					if (temp.trim().equals("")) {
						continue;
					}
					list.add(temp.trim());
					temp = "";
				} else {
					opendoublequotes = Boolean.TRUE.booleanValue();
				}

			} else {
				if (c != ' ') {
					continue;
				}
				if ((opendoublequotes) && (!temp.trim().equals(""))) {
					list.add(temp.trim());
					temp = "";
				}

				temp = new StringBuilder().append(temp)
						.append(String.valueOf(c)).toString();
			}

		}

		if (!temp.trim().equals("")) {
			list.add(temp.trim());
			temp = "";
		}
		return list;
	}

	public String getURLResponse() {
		return this.htmlresponse;
	}

	public static String getDecryptedPassword(String encPassword) {

		return "";
	}

	private String getHostIp() {
		String urlIp = "";
		try {
			urlIp = InetAddress.getByName(new URL(this.url).getHost())
					.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urlIp;
	}

	private void parseScreenshotXmlResponse(String resp) {
	}
}