package org.opennms.protocols.http;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.opennms.core.http.NVPair;
import org.opennms.core.http.ObjectHolder;
import org.opennms.core.http.Util;

public class HttpConnection {
	DefaultHttpClient httpclient = null;

	HttpHost target = null;

	int redirection_limit = 10;
	boolean handle_redirects = true;
	String originalURL = "";
	Properties conProp = new Properties();
	private boolean retry = false;

	long responseTime = 0L;
	private long dnsResolveTime = 0L;

	public String execContext = "";
	public List<String> nameServers = new ArrayList();

	boolean readContent = true;

	public String resolvedIP = "";
	private Logger logger = Logger.getLogger("HTTP");

	private String file = "";
	private String query = "";
	private NVPair[] headers;
	boolean nsEnabled = false;
	private static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
	private static final String ECC_DISABLED_CIPHERSUITES = "TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,SSL_RSA_WITH_RC4_128_MD5,TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
	private String ntlmdomain = "";

	private boolean isProxyRequired = false;
	private String proxyHost = "";
	private int proxyPort = 80;

	private int sotimout = 60;

	public void setNtlmDomain(String ntlmdomain) {
		this.ntlmdomain = ntlmdomain;
	}

	public HttpConnection(String url) {
		setURL(url);
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setProxyDetails(String host, int port, String proxyUser,
			String proxyPassWord) {
		this.isProxyRequired = true;
		this.proxyHost = host;
		this.proxyPort = port;
		String domain = System.getProperty("proxy.ntlm.domain");

		HttpHost proxy = new HttpHost(host, port);

		if ((proxyUser != null) && (proxyUser.indexOf("\\") != -1)) {
			String[] userdomainName = proxyUser.split("\\\\");
			domain = userdomainName[0];
			proxyUser = userdomainName[1];
		}

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				proxyUser, proxyPassWord);

		this.httpclient.getParams().setParameter("http.route.default-proxy",
				proxy);
		this.httpclient.getCredentialsProvider().setCredentials(
				new AuthScope(host, port), creds);
		if (domain == null) {
			domain = "";
		}
		String wkStat = System.getProperty("proxy.ntlm.workstation");
		if (wkStat == null) {
			wkStat = "";
		}
		NTCredentials ntcreds = new NTCredentials(proxyUser, proxyPassWord,
				wkStat, domain);
		this.httpclient.getCredentialsProvider().setCredentials(
				new AuthScope(host, port), ntcreds);
		LocalContextThreadLocal.get().setAttribute("donotproxyfor", "192.168");
	}

	public void setTargetAuthDetails(String targetHost, int port,
			String userName, String passWord) {
		if ((userName != null) && (passWord != null) && (!userName.equals(""))
				&& (!passWord.equals(""))) {
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
					userName, passWord);
			this.httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(targetHost, port, AuthScope.ANY_REALM,
							"basic"), creds);
			this.httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(targetHost, port, AuthScope.ANY_REALM,
							"digest"), creds);
			String domain = System.getProperty("ntlm.domain");
			if ((this.ntlmdomain != null) && (!this.ntlmdomain.equals(""))) {
				domain = this.ntlmdomain;
			}
			if (domain == null) {
				domain = "";
			}
			String wkStat = System.getProperty("ntlm.workstation");
			if (wkStat == null) {
				wkStat = "";
			}
			NTCredentials ntcreds = new NTCredentials(userName, passWord,
					wkStat, domain);
			this.httpclient.getCredentialsProvider().setCredentials(
					AuthScope.ANY, ntcreds);
		}
	}

	public void setHandleRedirection(boolean redirect) {
		this.handle_redirects = redirect;
		if (this.httpclient != null) {
			this.httpclient.getParams().setParameter(
					"http.protocol.handle-redirects",
					Boolean.valueOf(this.handle_redirects));
		}
	}

	public void setRedirectionLimit(int limit) {
		this.redirection_limit = limit;
		if (this.httpclient != null) {
			this.httpclient.getParams().setParameter(
					"http.protocol.max-redirects",
					Integer.valueOf(this.redirection_limit));
		}
	}

	public void setReadContent(boolean state) {
		this.readContent = state;
	}

	public HttpConnection(String prot, String host, int port) {
		this.httpclient = HttpClientHolder.createMultiThreadHttpClient();

		this.target = new HttpHost(host, port, prot);
	}

	public void setURL(String url) {
		this.originalURL = url;
		try {
			URL targetURL = new URL(url);
			int port = -1;
			if (targetURL.getPort() != -1) {
				port = targetURL.getPort();
			}

			this.target = new HttpHost(targetURL.getHost(), port, targetURL
					.getProtocol().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setConnectionProperties(Properties prop) {
		this.conProp = prop;
		if (this.conProp == null) {
			this.conProp = new Properties();
		}
	}

	public void setSOTimeOut(int sotimout) {
		this.sotimout = sotimout;
	}

	public void setNameServer(String nameServer) {
		if ((nameServer != null) && (nameServer.trim().length() > 2)) {
			StringTokenizer tkn = new StringTokenizer(nameServer, ",");
			while (tkn.hasMoreElements()) {
				String nameServer_val = tkn.nextToken().trim();
				if (nameServer_val.length() > 0) {
					this.nameServers.add(nameServer_val);
				}
			}
		}
	}

	public void setContext(String context) {
		this.execContext = context;
		String downloadSize = "102400";
		if (this.conProp.containsKey("userrole")) {
			if (this.conProp.get("userrole").equals("FREEUSER")) {
				downloadSize = "102400";
			} else {
				downloadSize = "256000";
			}
		} else {
			downloadSize = "102400";
		}
		if (!context.equalsIgnoreCase("default")) {
			this.httpclient = HttpClientHolder.getHttpClient(context);

			LocalContextThreadLocal.set(HttpClientHolder.getContext(context));
			if (context.equalsIgnoreCase("probe")) {
				downloadSize = "30024000";
				LocalContextThreadLocal.get().setAttribute("retryProtocols",
						ServerAgentConstants.cOMMUNICATIONPROTOCOLS);
			}
			LocalContextThreadLocal.get().setAttribute("downloadSize",
					downloadSize);
		} else {
			this.httpclient = HttpClientHolder.getHttpClient(context);

			LocalContextThreadLocal.set(new BasicHttpContext());
			LocalContextThreadLocal.get().setAttribute("downloadSize",
					downloadSize);
		}
		LocalContextThreadLocal
				.get()
				.setAttribute(
						"retryCipherSuites",
						"TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,SSL_RSA_WITH_RC4_128_MD5,TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

		this.httpclient.getParams().setParameter("http.protocol.version",
				HttpVersion.HTTP_1_1);
		this.httpclient.getParams().setParameter(
				"http.protocol.expect-continue", Boolean.valueOf(false));
		this.httpclient.getParams().setParameter(
				"http.protocol.strict-transfer-encoding",
				Boolean.valueOf(false));

		this.httpclient.getParams().setParameter(
				"http.protocol.handle-redirects", Boolean.valueOf(true));
		this.httpclient.getParams().setParameter("http.protocol.max-redirects",
				Integer.valueOf(this.redirection_limit));
		this.httpclient.getParams()
				.setParameter("http.protocol.allow-circular-redirects",
						Boolean.valueOf(true));
		this.httpclient.getParams().setParameter(
				"http.protocol.single-cookie-header", Boolean.valueOf(true));
		if ((this.conProp.containsKey("currentstatus"))
				&& (!this.conProp.get("currentstatus").equals("AVAILABLE"))) {
			this.httpclient.getParams().setParameter("http.socket.timeout",
					Integer.valueOf(60000));
		} else {
			this.httpclient.getParams().setParameter("http.socket.timeout",
					Integer.valueOf(this.sotimout * 1000));
		}
		this.httpclient
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
	}

	public void setTimeout(long timeout) {
		this.httpclient.getParams().setParameter("http.connection.timeout",
				Integer.valueOf(new Long(timeout).intValue()));
	}

	public void setDefaultHeaders(NVPair[] headers) {
		List header = new ArrayList();
		for (int i = 0; i < headers.length; i++) {
			try {
				if (headers[i] != null) {
					Header h = new BasicHeader(headers[i].getName(),
							headers[i].getValue());
					header.add(h);
				}
			} catch (Exception e) {
			}
		}

		if (header.size() > 0) {
			this.httpclient.getParams().setParameter("http.default-headers",
					header);
		}
	}

	public void setHostHeader(String header) {
		LocalContextThreadLocal.get().setAttribute("HOST", header);
	}

	public Response readResponseContent(HttpResponse response, Response resp)
			throws Exception {
		InputStream is = null;
		try {
			if (response.getEntity() == null) {
				Response localResponse = resp;
				return localResponse;
			}
			Object entity = new BufferedHttpEntity(response.getEntity());
			is = ((HttpEntity) entity).getContent();

			if (is != null) {
				resp.setInputStream(is);
				long cdTime = 0L;
				long dstime = System.currentTimeMillis();
				if ((((HttpEntity) entity).getContentEncoding() != null)
						&& (((HttpEntity) entity).getContentEncoding()
								.getValue().indexOf("gzip") != -1)) {
					GZIPInputStream gzipis = new GZIPInputStream(is);
					int i = (int) ((HttpEntity) entity).getContentLength();
					if (i < 0) {
						i = 4096;
					}
					ByteArrayBuffer buffer = new ByteArrayBuffer(i);
					try {
						byte[] tmp = new byte[4096];
						int l;
						while ((l = gzipis.read(tmp)) != -1) {
							buffer.append(tmp, 0, l);
						}
					} catch (EOFException eof) {
						resp.setData(buffer.toByteArray());
					} finally {
						gzipis.close();
					}
					resp.setData(buffer.toByteArray());
				} else {
					int i = (int) ((HttpEntity) entity).getContentLength();
					if (i < 0) {
						i = 4096;
					}
					ByteArrayBuffer buffer = new ByteArrayBuffer(i);
					try {
						byte[] tmp = new byte[4096];
						int l;
						while ((l = is.read(tmp)) != -1) {
							buffer.append(tmp, 0, l);
						}
					} catch (EOFException eof) {
						resp.setData(buffer.toByteArray());
					} finally {
						is.close();
					}

					resp.setData(buffer.toByteArray());
				}

			}

			EntityUtils.consume((HttpEntity) entity);
		} catch (IOException e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}

		return (Response) resp;
	}

	public Response constructResponeOnject(HttpResponse response, Response resp) {
		try {
			if (response != null) {
				resp.setHttpClientVersion("4.0");
				resp.setStatusCode(response.getStatusLine().getStatusCode());
				resp.setHttpMessage(response.getStatusLine().getReasonPhrase());
				resp.setHttpVersion(response.getStatusLine()
						.getProtocolVersion().toString());
				resp.setResponseHeaders(response.getAllHeaders());

				if (response.getEntity() != null) {
					HttpEntity entity = response.getEntity();
					long cLength = entity.getContentLength();
					resp.setContentLength(cLength);

					if (cLength == -1L) {
						cLength = resp.getData().length;
						resp.setContentLength(cLength);
					}
					entity.consumeContent();
				}
			}

			resp.setResponseTime(this.responseTime);

			long connstarttime = 0L;
			long connendtime = 0L;
			long firstbytetime = 0L;
			long lastbytetime = 0L;
			long handshakestarttime = -1L;
			long handshakeendtime = -1L;

			resp.setDNSTime(this.dnsResolveTime);

			if (LocalContextThreadLocal.get()
					.getAttribute("handshakestarttime") != null) {
				handshakestarttime = Long.parseLong(LocalContextThreadLocal
						.get().getAttribute("handshakestarttime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("handshakeendtime") != null) {
				handshakeendtime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("handshakeendtime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("connstarttime") != null) {
				connstarttime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("connstarttime").toString());
			} else if (LocalContextThreadLocal.get().getAttribute(
					"reqstarttime") != null) {
				connstarttime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("reqstarttime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("connendtime") != null) {
				connendtime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("connendtime").toString());
			} else if (LocalContextThreadLocal.get().getAttribute(
					"reqstarttime") != null) {
				connendtime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("reqstarttime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("firstbytetime") != null) {
				firstbytetime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("firstbytetime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("lastbytetime") != null) {
				lastbytetime = Long.parseLong(LocalContextThreadLocal.get()
						.getAttribute("lastbytetime").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("SSL_CERT_DETAILS") != null) {
				this.logger.log(
						Level.INFO,
						new StringBuilder()
								.append("SSL CERT DETAILS: ")
								.append(LocalContextThreadLocal.get()
										.getAttribute("SSL_CERT_DETAILS"))
								.toString());
			}

			if ((handshakestarttime > -1L) && (handshakeendtime > -1L)) {
				resp.setSSLHandshakeTime(handshakeendtime - handshakestarttime);
			}
			if ((connendtime > 1000L) && (connstarttime > 1000L)
					&& (connendtime - connstarttime > 0L)) {
				resp.setConnectionTime(connendtime - connstarttime);
			}
			if ((firstbytetime > 1000L) && (connendtime > 1000L)
					&& (firstbytetime - connendtime > 0L)) {
				resp.setFirstByteTime(firstbytetime - connendtime);
			}
			if ((lastbytetime > 1000L) && (connendtime > 1000L)
					&& (lastbytetime - connendtime > 0L)) {
				resp.setLastByteTime(lastbytetime - connendtime);
			}
			if ((lastbytetime > 1000L) && (firstbytetime > 1000L)
					&& (lastbytetime - firstbytetime > 0L)) {
				resp.setDownloadTime(lastbytetime - firstbytetime);
			}

			if (LocalContextThreadLocal.get().getAttribute("failedips") != null) {
				resp.setFailedIP(LocalContextThreadLocal.get()
						.getAttribute("failedips").toString());
			}

			if (LocalContextThreadLocal.get().getAttribute("ip") != null) {
				String ip = (String) LocalContextThreadLocal.get()
						.getAttribute("ip");
				resp.setIP(ip);
			}
			resp.setOriginalURL(this.originalURL);
			String effectiveURL = (String) LocalContextThreadLocal.get()
					.getAttribute("lasturl");
			if (effectiveURL == null) {
				effectiveURL = this.originalURL;
			}
			resp.setEffectiveURL(effectiveURL);
			if (LocalContextThreadLocal.get().getAttribute("redirectcount") != null) {
				int redirectCount = Integer.parseInt(LocalContextThreadLocal
						.get().getAttribute("redirectcount").toString());
				resp.setNumberOfRedirects(redirectCount);
				LocalContextThreadLocal.get().removeAttribute("redirectcount");
			}

			LocalContextThreadLocal.get().removeAttribute("dnsstarttime");
			LocalContextThreadLocal.get().removeAttribute("dnsendtime");
			LocalContextThreadLocal.get().removeAttribute("handshakestarttime");
			LocalContextThreadLocal.get().removeAttribute("handshakeendtime");
			LocalContextThreadLocal.get().removeAttribute("connstarttime");
			LocalContextThreadLocal.get().removeAttribute("connendtime");
			LocalContextThreadLocal.get().removeAttribute("firstbytetime");
			LocalContextThreadLocal.get().removeAttribute("lastbytetime");
			LocalContextThreadLocal.get().removeAttribute("reqstarttime");

			LocalContextThreadLocal.get().removeAttribute("ip");

			if (LocalContextThreadLocal.get().getAttribute("session-headers") != null) {
				StringBuilder sb = new StringBuilder("");
				StringBuilder reqHeader = new StringBuilder("");
				StringBuilder rspHeader = new StringBuilder("");
				Vector sesHeaders = (Vector) LocalContextThreadLocal.get()
						.getAttribute("session-headers");
				boolean request = Boolean.TRUE.booleanValue();
				for (int i = 0; i < sesHeaders.size(); i++) {
					Header[] headers = null;
					if ((sesHeaders.elementAt(i) instanceof RequestWrapper)) {
						request = Boolean.TRUE.booleanValue();
						RequestWrapper req = (RequestWrapper) sesHeaders
								.elementAt(i);
						if (i != 0) {
							sb.append("<br/>");
						}
						sb.append(req.getRequestLine()).append("<br/>");
						reqHeader.append("<br/>");
						reqHeader.append(req.getRequestLine()).append("<br/>");
						headers = req.getAllHeaders();
					} else if ((sesHeaders.elementAt(i) instanceof HttpResponse)) {
						request = Boolean.FALSE.booleanValue();
						HttpResponse res = (HttpResponse) sesHeaders
								.elementAt(i);
						sb.append("<br/>");
						sb.append(res.getStatusLine()).append("<br/>");
						rspHeader.append("<br/>");
						rspHeader.append(res.getStatusLine()).append("<br/>");
						headers = res.getAllHeaders();
					}

					for (int j = 0; j < headers.length; j++) {
						sb.append(headers[j].getName()).append(" : ")
								.append(headers[j].getValue()).append("<br/>");
						if (request)
							reqHeader.append(headers[j].getName())
									.append(" : ")
									.append(headers[j].getValue())
									.append("<br/>");
						else {
							rspHeader.append(headers[j].getName())
									.append(" : ")
									.append(headers[j].getValue())
									.append("<br/>");
						}
					}
				}
				resp.setSessionHeaders(sb.toString());
				resp.setRequestHeaders(reqHeader.toString());
				resp.setResponseHeaders(rspHeader.toString());
				LocalContextThreadLocal.get()
						.removeAttribute("session-headers");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resp;
	}

	private HttpRequest addHeaders(HttpRequest request, NVPair[] headers) {
		if (headers == null) {
			return request;
		}

		if (headers.length > 0) {
			for (int i = 0; i < headers.length; i++) {
				try {
					BasicHeader h = new BasicHeader(headers[i].getName(),
							headers[i].getValue());
					request.addHeader(h);
				} catch (Exception ex) {
					Thread.dumpStack();
				}
			}
		}

		return request;
	}

	private List<InetAddress> resolveHost(String host, String nameServer)
			throws UnknownHostException {
		if (LocalContextThreadLocal.get().getAttribute(
				new StringBuilder().append("resolvedip-").append(host)
						.toString()) == null) {
			long startTime = System.currentTimeMillis();
			InetAddress[] inetadrs = null;
			if ((nameServer != null) && (nameServer.length() > 3)) {
				startTime = System.currentTimeMillis();
				inetadrs = DnsResolver.getInstance().resolve(nameServer, host);
			}
			if (inetadrs == null) {
				startTime = System.currentTimeMillis();
				inetadrs = InetAddress.getAllByName(host);
			}
			long endTime = System.currentTimeMillis();
			this.dnsResolveTime = (endTime - startTime);
			List addresses = new ArrayList(inetadrs.length);
			addresses.addAll(Arrays.asList(inetadrs));
			Collections.shuffle(addresses);
			LocalContextThreadLocal.get().setAttribute(
					new StringBuilder().append("resolvedip-").append(host)
							.toString(), addresses);
		}
		List addresses = (List) LocalContextThreadLocal.get().getAttribute(
				new StringBuilder().append("resolvedip-").append(host)
						.toString());
		return addresses;
	}

	public Response sendRequest(HttpRequest request) throws ConnectException,
			SocketTimeoutException, Exception {
		RedirectLocations redirectLocations = new RedirectLocations();
		LocalContextThreadLocal.get().setAttribute(
				"http.protocol.redirect-locations", redirectLocations);
		LocalContextThreadLocal.get()
				.setAttribute("tageturl", this.execContext);
		if (LocalContextThreadLocal.get().getAttribute("connstarttime") != null) {
			LocalContextThreadLocal.get().removeAttribute("connstarttime");
		}

		List<InetAddress> addresses = new ArrayList<InetAddress>();

		long starttime = System.currentTimeMillis();
		try {
			if ((ObjectHolder.isSite24x7()) && (this.nsEnabled)
					&& (this.nameServers.size() > 0)) {
				addresses = resolveHost(this.target.getHostName(),
						(String) this.nameServers.get(0));
			} else if (this.isProxyRequired) {
				addresses = resolveHost(this.proxyHost, null);
			} else {
				addresses = resolveHost(this.target.getHostName(), null);
			}

		} catch (UnknownHostException e) {
			this.logger.log(
					Level.INFO,
					new StringBuilder()
							.append("UnKnownHost Error : CurrentStatus=")
							.append(this.conProp.get("currentstatus").equals(
									"AVAILABLE")).append(", NSEnabled=")
							.append(this.nsEnabled).append(", NameServers=")
							.append(this.nameServers).toString());

			if ((this.conProp.get("currentstatus").equals("AVAILABLE"))
					&& (!this.nsEnabled) && (this.nameServers.size() > 0)) {
				int size = this.nameServers.size();
				for (int i = 0; i < size; i++) {
					String retryNameServer = (String) this.nameServers.get(i);
					try {
						this.logger
								.log(Level.INFO,
										new StringBuilder()
												.append("UnKnownHost Error : Retry with (")
												.append(i + 1)
												.append(") NameServer : ")
												.append(retryNameServer)
												.toString());
						addresses = resolveHost(this.target.getHostName(),
								retryNameServer);
					} catch (UnknownHostException ue) {
						this.logger
								.log(Level.INFO,
										new StringBuilder()
												.append("UnKnownHost Error : Retry with (")
												.append(i + 1)
												.append(") NameServer : Failed")
												.toString());
						if (i == size - 1) {
							this.logger
									.log(Level.INFO,
											"UnKnownHost Error : Retry with all NameServers Completed. So UnknownHost Exception thrown");
							throw ue;
						}
					}
					if ((addresses != null) && (addresses.size() > 0)) {
						break;
					}
				}
			} else if ((this.conProp.get("currentstatus").equals("AVAILABLE"))
					&& (this.nsEnabled) && (this.nameServers.size() > 1)) {
				int size = this.nameServers.size();
				for (int i = 1; i < size; i++) {
					String retryNameServer = (String) this.nameServers.get(i);
					try {
						this.logger
								.log(Level.INFO,
										new StringBuilder()
												.append("UnKnownHost Error : Retry with (")
												.append(i + 1)
												.append(") NameServer : ")
												.append(retryNameServer)
												.toString());
						addresses = resolveHost(this.target.getHostName(),
								retryNameServer);
					} catch (UnknownHostException ue) {
						this.logger
								.log(Level.INFO,
										new StringBuilder()
												.append("UnKnownHost Error : Retry with (")
												.append(i + 1)
												.append(") NameServer : Failed")
												.toString());
						if (i == size - 1) {
							this.logger
									.log(Level.INFO,
											"UnKnownHost Error : Retry with all NameServers Completed. So UnknownHost Exception thrown");
							throw ue;
						}
					}
					if ((addresses != null) && (addresses.size() > 0)) {
						break;
					}
				}
			} else {
				throw e;
			}
		}

		Response resp = new Response();
		HttpResponse response = null;
		Exception lastEx = null;
		for (InetAddress remoteAddress : addresses) {
			try {
				if ((remoteAddress == null)
						|| (remoteAddress.getHostAddress() == null)) {
					throw new UnknownHostException(new StringBuilder()
							.append("unable to resolve host address ")
							.append(this.target.getHostName()).toString());
				}
				int port = this.target.getPort();
				if (port == -1) {
					if (this.target.getSchemeName().equals("http")) {
						port = 80;
					} else if (this.target.getSchemeName().equals("https")) {
						port = 443;
					}
				}

				if ((!ObjectHolder.isSite24x7()) && (this.isProxyRequired)) {
					port = this.proxyPort;
				}

				InetSocketAddress sockAddress = new InetSocketAddress(
						remoteAddress, port);

				LocalContextThreadLocal.get().setAttribute(
						new StringBuilder().append(this.target.getSchemeName())
								.append("-").append(this.target.getHostName())
								.toString(), sockAddress);
				LocalContextThreadLocal.get().setAttribute("ttfbstatus",
						Boolean.FALSE);
				response = this.httpclient.execute(this.target, request,
						LocalContextThreadLocal.get());
				resp.setCookies(this.httpclient.getCookieStore().getCookies());
				if (LocalContextThreadLocal.get().getAttribute("reqstarttime") != null) {
					long starttime1 = Long.parseLong(LocalContextThreadLocal
							.get().getAttribute("reqstarttime").toString());

					starttime = starttime1;
				}

				if (this.readContent) {
					resp = readResponseContent(response, resp);
				} else if (response.getEntity() != null) {
					resp.setInputStream(response.getEntity().getContent());
				}

				Long endTime = Long.valueOf(System.currentTimeMillis());
				if (LocalContextThreadLocal.get().getAttribute("lastbytetime") != null) {
					endTime = Long.valueOf(Long
							.parseLong(LocalContextThreadLocal.get()
									.getAttribute("lastbytetime").toString()));
				}
				this.responseTime = (endTime.longValue() - starttime);

				this.responseTime = (this.dnsResolveTime + this.responseTime);

				if (LocalContextThreadLocal.get().getAttribute("waittime") != null) {
					long waittime = Long.parseLong(LocalContextThreadLocal
							.get().getAttribute("waittime").toString());
					LocalContextThreadLocal.get().removeAttribute("waittime");
					this.responseTime -= waittime;
				}

				lastEx = null;
			} catch (ClientProtocolException cpe) {
				this.logger.log(Level.INFO,
						new StringBuilder().append("ClientProtocolException :")
								.append(cpe).toString());
				Throwable cause = cpe.getCause();
				this.logger.log(
						Level.INFO,
						new StringBuilder()
								.append("ClientProtocolException Caused By :")
								.append(cause).toString());

				if ((cause instanceof RedirectException)) {
					this.logger.log(
							Level.INFO,
							new StringBuilder()
									.append("RedirectException Caused By :")
									.append(cause).toString());
					resp.setStatusCode(614);
					resp.setHttpMessage(cpe.getCause().getMessage());
					resp.setHttpClientVersion("4.0");
					resp.setHttpVersion("HTTP/1.1");
					return constructResponeOnject(null, resp);
				}
				if ((cause instanceof ProtocolException)) {

				} else if ((cause instanceof MalformedChallengeException)) {

				} else {
					throw cpe;
				}

			} catch (RedirectException re) {
				resp.setStatusCode(614);
				resp.setHttpMessage(re.getMessage());
				resp.setHttpClientVersion("4.0");
				resp.setHttpVersion("HTTP/1.1");
				return constructResponeOnject(null, resp);
			} catch (SSLPeerUnverifiedException sslex) {
				resp.setStatusCode(615);
				resp.setHttpMessage("SSL Connection Error");
				resp.setHttpClientVersion("4.0");
				resp.setHttpVersion("HTTP/1.1");
				return constructResponeOnject(null, resp);
			} catch (HttpHostConnectException conExp) {
				if (LocalContextThreadLocal.get().getAttribute("ip") != null) {
					this.resolvedIP = ((String) LocalContextThreadLocal.get()
							.getAttribute("ip"));
				}
				lastEx = conExp;
				LocalContextThreadLocal.get().removeAttribute("reqstarttime");
			} catch (ConnectTimeoutException cto) {
				if (LocalContextThreadLocal.get().getAttribute("ip") != null) {
					this.resolvedIP = ((String) LocalContextThreadLocal.get()
							.getAttribute("ip"));
				}
				lastEx = cto;
				LocalContextThreadLocal.get().removeAttribute("reqstarttime");
			} catch (SocketTimeoutException ste) {
				if (LocalContextThreadLocal.get().getAttribute("ip") != null) {
					this.resolvedIP = ((String) LocalContextThreadLocal.get()
							.getAttribute("ip"));
				}
				resp.setStatusCode(613);
				resp.setHttpMessage("Read Timeout");
				resp.setHttpClientVersion("4.0");
				resp.setHttpVersion("HTTP/1.1");
				return constructResponeOnject(null, resp);
			} catch (IllegalStateException illex) {
				lastEx = illex;
				throw illex;
			} catch (Exception e) {
				e.printStackTrace();
				if (LocalContextThreadLocal.get().getAttribute("ip") != null) {
					this.resolvedIP = ((String) LocalContextThreadLocal.get()
							.getAttribute("ip"));
				}
				lastEx = e;
				LocalContextThreadLocal.get().removeAttribute("reqstarttime");
			}
		}

		if (lastEx != null) {
			throw lastEx;
		}

		resp = constructResponeOnject(response, resp);
		if ((resp != null) && (!this.retry) && (resp.getStatusCode() == 400)
				&& (this.file.indexOf(",") != -1)) {
			this.retry = true;
			if ((request instanceof HttpGet)) {
				this.logger.log(Level.INFO,
						"As responseCode is 400(Request Error). Retry Started");
				return Get(this.file, this.query, this.headers);
			}
		}
		return resp;
	}

	public Response Delete(String file, String query, NVPair[] headers) {
		this.file = file;
		this.query = query;
		this.headers = headers;
		HttpRequest request = null;
		List qparams = new ArrayList();

		if (file.indexOf("?") != -1) {
			query = file.substring(file.indexOf("?") + 1, file.length());
			file = file.substring(0, file.indexOf("?"));
		} else if (file.indexOf(";") != -1) {
			query = file.substring(file.indexOf(";"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf(";"));
			}
		} else if (file.indexOf("#") != -1) {
			query = file.substring(file.indexOf("#"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf("#"));
			}

		}

		if (!query.equals("")) {
			String[] params = query.split("&");
			for (int i = 0; i < params.length; i++) {
				String namevalues = params[i];
				String[] kv = namevalues.split("=");
				String pname = kv[0];
				String pvalue = "";
				if (!namevalues.contains("=")) {
					pvalue = "NoNameValueSeperator";
				}
				if (kv.length > 1) {
					pvalue = kv[1];
				}

				pname = URLDecoder.decode(pname);
				pvalue = URLDecoder.decode(pvalue);
				qparams.add(new BasicNameValuePair(pname, pvalue));
			}
			try {
				URL url = new URL(this.originalURL);
				String path = url.getPath();
				int port = 80;
				if (url.getPort() != -1) {
					port = url.getPort();
				} else if (url.getProtocol().startsWith("https")) {
					port = 443;
				}

				query = URLEncodedUtils.format(qparams, "UTF-8");

				query = query.replaceAll("%3F", "?");
				query = query.replaceAll("%23", "#");
				if (this.retry) {
					query = query.replaceAll("%2C", ",");
				}
				if (path.isEmpty()) {
					path = file;
				}
				URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(),
						port, path, query, null);
				request = new HttpDelete(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			request = new HttpDelete(file);
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return null;
	}

	public Response Get(String file, String query, NVPair[] headers) {
		this.file = file;
		this.query = query;
		this.headers = headers;
		HttpRequest request = null;
		List qparams = new ArrayList();

		if (file.indexOf("?") != -1) {
			query = file.substring(file.indexOf("?") + 1, file.length());
			file = file.substring(0, file.indexOf("?"));
		} else if (file.indexOf(";") != -1) {
			query = file.substring(file.indexOf(";"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf(";"));
			}
		} else if (file.indexOf("#") != -1) {
			query = file.substring(file.indexOf("#"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf("#"));
			}

		}

		if (!query.equals("")) {
			String[] params = query.split("&");
			for (int i = 0; i < params.length; i++) {
				String namevalues = params[i];
				String[] kv = namevalues.split("=");
				String pname = kv[0];
				String pvalue = "";
				if (!namevalues.contains("=")) {
					pvalue = "NoNameValueSeperator";
				}
				if (kv.length > 1) {
					pvalue = kv[1];
				}

				pname = URLDecoder.decode(pname);
				pvalue = URLDecoder.decode(pvalue);
				qparams.add(new BasicNameValuePair(pname, pvalue));
			}
			try {
				URL url = new URL(this.originalURL);
				String path = url.getPath();
				int port = 80;
				if (url.getPort() != -1) {
					port = url.getPort();
				} else if (url.getProtocol().startsWith("https")) {
					port = 443;
				}

				query = URLEncodedUtils.format(qparams, "UTF-8");

				query = query.replaceAll("%3F", "?");
				query = query.replaceAll("%23", "#");
				query = query.replaceAll("%2F", "/");
				query = query.replaceAll("%2B", "+");
				if (this.retry) {
					query = query.replaceAll("%2C", ",");
				}
				if (path.isEmpty()) {
					path = file;
				}
				URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(),
						port, path, query, null);
				request = new HttpGet(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			request = new HttpGet(file);
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return null;
	}

	public Response Post(String file, String query, NVPair[] headers) {
		try {
			Properties prop = new Properties();
			if (!query.equals("")) {
				String[] params = query.split("&");
				for (int i = 0; i < params.length; i++) {
					int ind = params[i].indexOf("=");
					String pname = "";
					String pvalue = "";
					if (ind != -1) {
						pname = params[i].substring(0, ind);
						pvalue = params[i].substring(ind + 1,
								params[i].length());
					} else {
						pname = params[i];
					}
					prop.put(pname, pvalue);
				}
			}

			NVPair[] formData = new NVPair[prop.size()];
			Enumeration en = prop.keys();
			for (int i = 0; en.hasMoreElements(); i++) {
				String key = (String) en.nextElement();
				String value = (String) prop.get(key);
				formData[i] = new NVPair(key, value);
			}

			return Post(file, formData, headers);
		} catch (Exception e) {
		}
		return null;
	}

	public Response Post(String file, String query, NVPair[] fileData,
			NVPair[] headers) {
		try {
			Properties prop = new Properties();
			if (!query.equals("")) {
				String[] params = query.split("&");
				for (int i = 0; i < params.length; i++) {
					int ind = params[i].indexOf("=");
					String pname = "";
					String pvalue = "";
					if (ind != -1) {
						pname = params[i].substring(0, ind);
						pvalue = params[i].substring(ind + 1,
								params[i].length());
					} else {
						pname = params[i];
					}
					prop.put(pname, pvalue);
				}
			}

			NVPair[] formData = new NVPair[prop.size()];
			Enumeration en = prop.keys();
			for (int i = 0; en.hasMoreElements(); i++) {
				String key = (String) en.nextElement();
				String value = (String) prop.get(key);
				formData[i] = new NVPair(key, value);
			}

			return Post(file, formData, fileData, headers);
		}

		catch (Exception e) {
		}
		return null;
	}

	public Response Put(String file, NVPair[] formData, NVPair[] headers)
			throws UnsupportedEncodingException {
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			request = new HttpPut(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (formData != null) {
			for (int i = 0; i < formData.length; i++) {
				qparams.add(new BasicNameValuePair(formData[i].getName(),
						formData[i].getValue()));
			}
			((HttpPut) request).setEntity(new UrlEncodedFormEntity(qparams));
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return null;
	}

	public Response Patch(String file, NVPair[] formData, NVPair[] headers)
			throws UnsupportedEncodingException {
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			request = new HttpPatch(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (formData != null) {
			for (int i = 0; i < formData.length; i++) {
				qparams.add(new BasicNameValuePair(formData[i].getName(),
						formData[i].getValue()));
			}
			((HttpPatch) request).setEntity(new UrlEncodedFormEntity(qparams));
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (Exception e) {
		}
		return null;
	}

	public Response Post(String file, NVPair[] formData, NVPair[] headers)
			throws UnsupportedEncodingException {
		Response resp = new Response();
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			int port = 80;
			if (url.getPort() != -1) {
				port = url.getPort();
			} else if (url.getProtocol().startsWith("https")) {
				port = 443;
			}

			request = new HttpPost(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (formData != null) {
			for (int i = 0; i < formData.length; i++) {
				qparams.add(new BasicNameValuePair(formData[i].getName(),
						formData[i].getValue()));
			}

			((HttpPost) request).setEntity(new UrlEncodedFormEntity(qparams));
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (Exception e) {
		}
		return resp;
	}

	public Response Post(String file, NVPair[] formData, NVPair[] fileData,
			NVPair[] headers) throws UnsupportedEncodingException {
		Response resp = new Response();
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			int port = 80;
			if (url.getPort() != -1) {
				port = url.getPort();
			} else if (url.getProtocol().startsWith("https")) {
				port = 443;
			}

			request = new HttpPost(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String boundary = "=---------------------------7da3c227330a58";

		MultipartEntity reqEntity = new MultipartEntity();

		if (formData != null) {
			StringBody formParmeter;
			for (int i = 0; i < formData.length; i++) {
				formParmeter = new StringBody(formData[i].getValue());
			}

		}

		if (fileData != null) {
			FileBody fileParameter;
			for (int i = 0; i < fileData.length; i++) {
				fileParameter = new FileBody(new File(
						"./licenses/LICENSE_DWR.html"));
			}

		}

		((HttpPost) request).setEntity(reqEntity);
		System.out.println(((HttpPost) request).getEntity().getContentLength());

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return resp;
	}

	public Response PostXml(String file, String xmlData, NVPair[] headers)
			throws ParseException, IOException {
		Response resp = new Response();
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			int port = 80;
			if (url.getPort() != -1) {
				port = url.getPort();
			} else if (url.getProtocol().startsWith("https")) {
				port = 443;
			}

			request = new HttpPost(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		((HttpPost) request).setEntity(new StringEntity(xmlData));

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return resp;
	}

	public Response PutXml(String file, String xmlData, NVPair[] headers)
			throws ParseException, IOException {
		Response resp = new Response();
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			int port = 80;
			if (url.getPort() != -1) {
				port = url.getPort();
			} else if (url.getProtocol().startsWith("https")) {
				port = 443;
			}

			request = new HttpPut(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		((HttpPut) request).setEntity(new StringEntity(xmlData));

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return resp;
	}

	public Response PatchXml(String file, String xmlData, NVPair[] headers)
			throws ParseException, IOException {
		Response resp = new Response();
		HttpRequest request = null;
		List qparams = new ArrayList();
		try {
			URL url = new URL(this.originalURL);
			int port = 80;
			if (url.getPort() != -1) {
				port = url.getPort();
			} else if (url.getProtocol().startsWith("https")) {
				port = 443;
			}

			request = new HttpPatch(url.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		((HttpPatch) request).setEntity(new StringEntity(xmlData));

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return resp;
	}

	public Response PostMultipart(String file, NVPair[] formData,
			MultipartNVPair[] fileData, NVPair[] headers) {
		System.out.println(new StringBuilder().append("IN PostMultipart")
				.append(fileData).toString());
		HttpRequest request = null;
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			try {
				URL url = new URL(this.originalURL);
				request = new HttpPost(url.getFile());
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (formData != null) {
				for (int i = 0; i < formData.length; i++) {
					reqEntity.addPart(formData[i].getName(),
							new StringBody(formData[i].getValue(),
									"text/plain", Charset.forName("UTF-8")));
				}

			}

			if (fileData != null) {
				for (int i = 0; i < fileData.length; i++) {
					reqEntity.addPart(fileData[i].getName(), new FileBody(
							(File) fileData[i].getValue(), "application/zip"));
				}
			}

			((HttpPost) request).setEntity(reqEntity);

			request = addHeaders(request, headers);
			try {
				return sendRequest(request);
			} catch (HttpHostConnectException conExp) {
				throw new ConnectException(conExp.getMessage());
			} catch (ConnectTimeoutException cto) {
				throw new SocketTimeoutException(cto.getMessage());
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			System.out.println(new StringBuilder()
					.append("EXCEPTION in PostMultipart EXCEP=").append(e)
					.toString());
			e.printStackTrace();
		}
		return null;
	}

	public Response Head(String file, String query, NVPair[] headers) {
		HttpRequest request = null;
		List qparams = new ArrayList();

		if (file.indexOf("?") != -1) {
			query = file.substring(file.indexOf("?") + 1, file.length());
			file = file.substring(file.indexOf("?"));
		} else if (file.indexOf(";") != -1) {
			query = file.substring(file.indexOf(";"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf(";"));
			}
		} else if (file.indexOf("#") != -1) {
			query = file.substring(file.indexOf("#"), file.length());
			if (!query.trim().equals("")) {
				file = file.substring(0, file.indexOf("#"));
			}

		}

		if (!query.equals("")) {
			String[] params = query.split("&");
			for (int i = 0; i < params.length; i++) {
				String namevalues = params[i];
				String[] kv = namevalues.split("=");
				String pname = kv[0];
				String pvalue = "";
				if (!namevalues.contains("=")) {
					pvalue = "NoNameValueSeperator";
				}
				if (kv.length > 1) {
					pvalue = kv[1];
				}
				qparams.add(new BasicNameValuePair(pname, pvalue));
			}
			try {
				URL url = new URL(this.originalURL);
				int port = 80;
				if (url.getPort() != -1) {
					port = url.getPort();
				} else if (url.getProtocol().startsWith("https")) {
					port = 443;
				}

				query = URLEncodedUtils.format(qparams, "UTF-8");

				query = query.replace("%3F", "?");
				query = query.replace("%23", "#");
				URI uri = URIUtils.createURI(url.getProtocol(), url.getHost(),
						port, url.getPath(), query, null);
				request = new HttpHead(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			request = new HttpHead(file);
		}

		request = addHeaders(request, headers);
		try {
			return sendRequest(request);
		} catch (HttpHostConnectException conExp) {
		} catch (ConnectTimeoutException cto) {
		} catch (Exception e) {
		}
		return null;
	}

	public void shutdown(String context) {
		if (this.httpclient != null) {
			this.httpclient.getConnectionManager().shutdown();
		}
		HttpClientHolder.removeHttpClient(context);
		HttpClientHolder.removeContext(context);
	}

	public void setNameServerEnabled(boolean nsStatus) {
		this.nsEnabled = nsStatus;
	}

	public void setSSLProtocols(String sslProtocols) {
		try {
			sslProtocols = Util.trim(sslProtocols);
			if ((sslProtocols.length() > 2)
					&& (LocalContextThreadLocal.get() != null)) {
				LocalContextThreadLocal.get().setAttribute("protocols",
						sslProtocols);
				LocalContextThreadLocal.get().setAttribute("retryProtocols",
						sslProtocols);
				this.logger.log(
						Level.INFO,
						new StringBuilder()
								.append("Setting SSL protocols ")
								.append(LocalContextThreadLocal.get()
										.getAttribute("protocols")).toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public List<Cookie> getCookies() {
		return this.httpclient.getCookieStore().getCookies();
	}

	public static void main(String[] args) {
		try {
			String url = "http://www.site24x7.com/";
			HttpConnection con = new HttpConnection(url);
			con.setSOTimeOut(120000);
			con.setContext("default");
			con.setNameServerEnabled(false);

			con.setTimeout(15000L);
			NVPair[] allheadersNVPair = new NVPair[2];
			NVPair useragentNVPair = new NVPair("User-Agent", "Site24x7 Agent");
			NVPair accept = new NVPair("Accept-Encoding", "gzip");

			allheadersNVPair[0] = accept;
			allheadersNVPair[1] = useragentNVPair;

			con.setDefaultHeaders(allheadersNVPair);

			con.setHostHeader("ptreklam.com.tr");

			Response rsp = con.Get(url, "", null);
		} catch (Exception e) {
			Response rsp;
			e.printStackTrace();
		}
	}
}