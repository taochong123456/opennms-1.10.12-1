package org.opennms.protocols.http;

import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

public class HttpClientHolder {
	private static HashMap<String, DefaultHttpClient> clientMap = new HashMap();
	private static HashMap<String, SyncBasicHttpContext> contextMap = new HashMap();

	public static synchronized DefaultHttpClient getHttpClient(String context) {
		if ((context.equals("default")) || (context.equals("probe"))) {
			return createMultiThreadHttpClient();
		}

		if (!clientMap.containsKey(context)) {
			DefaultHttpClient dhc = createMultiThreadHttpClient();
			clientMap.put(context, dhc);
		}

		return (DefaultHttpClient) clientMap.get(context);
	}

	public static DefaultHttpClient createMultiThreadHttpClient() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		ClientConnectionManager cm = null;
		try {
			cm = new BasicClientConnectionManager(schemeRegistry);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e);
		}
		DefaultHttpClient httpclient = new DefaultHttpClient(cm);
		return httpclient;
	}

	public static synchronized void removeHttpClient(String context) {
	}

	public static synchronized SyncBasicHttpContext getContext(String context) {
		if (!contextMap.containsKey(context)) {
			SyncBasicHttpContext shc = new SyncBasicHttpContext(
					new BasicHttpContext());
			contextMap.put(context, shc);
		}

		return (SyncBasicHttpContext) contextMap.get(context);
	}

	public static synchronized void removeContext(String context) {
		try {
			if ((context != null) && (contextMap.containsKey(context))) {
				contextMap.remove(context);
			}
		} catch (Exception e) {
		}
	}

	private static TrustManager[] getTrustingManager() {
		TrustManager[] trustAllCerts = { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };
		return trustAllCerts;
	}
}