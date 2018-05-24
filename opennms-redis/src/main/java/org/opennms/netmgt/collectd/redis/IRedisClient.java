package org.opennms.netmgt.collectd.redis;

import java.util.Map;

/**
 * redis connection interface
 * 
 * **/
public interface IRedisClient {
	public Map<String, String> getRedisInfo();
    /**
     * connect to redis
     * */
	public void connect(String ip, int port);
    /**
     * close the connect
     * */
	public void disConnect();
	/**
	 * is closed
	 * **/
	public boolean isClosed();
	
}
