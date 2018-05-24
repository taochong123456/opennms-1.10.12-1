package org.opennms.netmgt.collectd.redis;

import java.io.File;

import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

public class RedisCollectionResource extends AbstractCollectionResource {
	private int m_nodeId;
	private String serviceName;
	public RedisCollectionResource(CollectionAgent agent, String serviceName) {
		super(agent);
		m_nodeId = agent.getNodeId();
		this.serviceName = serviceName;
	}

	@Override
	public String getResourceTypeName() {
		return "node";
	}

	@Override
	public String getParent() {
		return m_nodeId+"";
	}

	@Override
	public String getInstance() {
		return null;
	}

	@Override
	public int getType() {
		return -1;
	}

	@Override
	public boolean rescanNeeded() {
		return false;
	}

	@Override
	public boolean shouldPersist(ServiceParameters params) {
		return true;
	}
	
	 public void setAttributeValue(CollectionAttributeType type, String value) {
		 RedisCollectionAttribute attr = new RedisCollectionAttribute(this, type, type.getName(), value);
         addAttribute(attr);
     }

     @Override
     public File getResourceDir(RrdRepository repository) {
         return new File(repository.getRrdBaseDir(), Integer.toString(m_agent.getNodeId())+File.separator+serviceName);
     }

}
