package org.opennms.protocols.http;

import java.util.logging.Logger;
import org.apache.http.protocol.BasicHttpContext;

public class LocalContextThreadLocal {
	private static Logger urllogger = Logger.getLogger("URL");
	public static final ThreadLocal<BasicHttpContext> LOCAL_CONTEXT_THREAD_LOCAL = new ThreadLocal();

	public static void set(BasicHttpContext user) {
		LOCAL_CONTEXT_THREAD_LOCAL.set(user);
	}

	public static void unset() {
		LOCAL_CONTEXT_THREAD_LOCAL.remove();
	}

	public static BasicHttpContext get() {
		return (BasicHttpContext) LOCAL_CONTEXT_THREAD_LOCAL.get();
	}
}