package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.collectd.redis.RedisAgentState;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class RedisMonitor extends AbstractServiceMonitor{

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
		Double responseTime = null;
		final NetworkInterface<InetAddress> iface = svc.getNetInterface();
		// Get the address we're going to poll.
		final InetAddress ipv4Addr = iface.getAddress();
		RedisAgentState redisAgentState = new RedisAgentState(ipv4Addr);
		try {
			redisAgentState.connect();
			redisAgentState.getRedisClient().getRedisInfo();
			serviceStatus = PollStatus.SERVICE_AVAILABLE;
		} catch (Exception e) {
            LogUtils.errorf(this, e, "An error occurred isProtocolSupported.");
		}finally{
			if(!redisAgentState.isClosed()){
				redisAgentState.close();
			}
		}
		return PollStatus.get(serviceStatus, "unknow", responseTime);
	}

}
