package org.opennms.netmgt.collectd.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import redis.clients.jedis.Jedis;

/**
 * The implement of redis connect
 * */
public class RedisClient implements IRedisClient {
	Jedis jed;

	@Override
	public Map<String, String> getRedisInfo() {
		StringTokenizer st = new StringTokenizer(jed.info(), "\r\n");
		Map<String, String> infoMap = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			String eachLine = st.nextToken();
			if ((eachLine.trim().equals(""))
					|| (eachLine.trim().startsWith("#"))) {
				continue;
			}
			int sep = eachLine.indexOf(":");
			String key = eachLine.substring(0, sep);
			String val = eachLine.substring(sep + 1);
			infoMap.put(key, val);
		}
		return infoMap;
	}

	@Override
	public void connect(String ip, int port) {
		jed = new Jedis(ip, port);
	}

	@Override
	public void disConnect() {
		jed.close();
	}

	@Override
	public boolean isClosed() {
		if(jed != null){
			return jed.isConnected();
		}
		return true;
	}

}
