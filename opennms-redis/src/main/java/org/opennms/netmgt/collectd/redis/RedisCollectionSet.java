package org.opennms.netmgt.collectd.redis;

import java.util.Date;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

public class RedisCollectionSet implements CollectionSet {
	private int m_status;
	private Date m_timestamp;
	private RedisCollectionResource m_collectionResource;

	public RedisCollectionSet(CollectionAgent agent,String serviceName) {
		m_status = ServiceCollector.COLLECTION_FAILED;
		m_collectionResource = new RedisCollectionResource(agent,serviceName);
	}

	@Override
	public int getStatus() {
		return m_status;
	}
	
	public RedisCollectionResource getRedisCollectionResource(){
		return m_collectionResource;
	}
	
	public void setStatus(int m_status){
		this.m_status = m_status;
	}

	@Override
	public void visit(CollectionSetVisitor visitor) {
		visitor.visitCollectionSet(this);
		m_collectionResource.visit(visitor);
		visitor.completeCollectionSet(this);
	}

	@Override
	public boolean ignorePersist() {
		return false;
	}

	@Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}

	public void setCollectionTimestamp(Date timestamp) {
		this.m_timestamp = timestamp;
	}

}
