package org.opennms.netmgt.collectd.redis;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.RedisPeerFactory;
import org.opennms.netmgt.config.redis.authen.Authen;

public class RedisAgentState {
	private IRedisClient redisClient;
	private InetAddress m_address;

	public RedisAgentState(final InetAddress address) {
		m_address = address;
		redisClient = new RedisClient();
	}

	public IRedisClient getRedisClient() {
		return redisClient;
	}

	public void setRedisClient(IRedisClient redisClient) {
		this.redisClient = redisClient;
	}

	public void connect() {
		try {
			RedisPeerFactory.init();
		} catch (MarshalException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO if authen is null throw exception
		Authen authen = RedisPeerFactory.getInstance().getAuthenConfig(m_address);
		if(authen == null){
			throw new UnsupportedOperationException("redis not supported");
		}
		redisClient.connect(authen.getIp(), Integer.parseInt(authen.getPort()));
	}

	public void close() {
		redisClient.disConnect();
	}
	
	public boolean isClosed(){
		if(redisClient == null||(redisClient.isClosed())){
			return true;
		}
		return false;
	}

}
