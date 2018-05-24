package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.collectd.redis.RedisAgentState;

public class RedisPlugin  extends AbstractPlugin {
	private final static String PROTOCOL_NAME = "REDIS";

	@Override
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	@Override
	public boolean isProtocolSupported(InetAddress address) {
		throw new UnsupportedOperationException("Undirected TCP checking not supported");
	}

	@Override
	public boolean isProtocolSupported(InetAddress address,
			Map<String, Object> qualifiers) {
		RedisAgentState redisAgentState = new RedisAgentState(address);
		try {
			redisAgentState.connect();
		} catch (Exception e) {
            LogUtils.errorf(this, e, "An error occurred isProtocolSupported.");
			return false;
		}finally{
			if(!redisAgentState.isClosed()){
				redisAgentState.close();
			}
		}
		return true;
	}

}
