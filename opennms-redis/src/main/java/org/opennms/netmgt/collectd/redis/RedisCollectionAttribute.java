package org.opennms.netmgt.collectd.redis;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

public class RedisCollectionAttribute extends AbstractCollectionAttribute
		implements CollectionAttribute {
	String m_alias;
	String m_value;
	RedisCollectionResource m_resource;
	CollectionAttributeType m_attribType;

	RedisCollectionAttribute(RedisCollectionResource resource,
			CollectionAttributeType attribType, String alias, String value) {
		super();
		m_resource = resource;
		m_attribType = attribType;
		m_alias = alias;
		m_value = value;
	}

	@Override
	public String getType() {
		return m_attribType.getType();
	}

	@Override
	public CollectionAttributeType getAttributeType() {
		return m_attribType;
	}

	@Override
	public String getName() {
		return m_alias;
	}

	@Override
	public String getNumericValue() {
		return m_value;
	}

	@Override
	public CollectionResource getResource() {
		return m_resource;
	}

	@Override
	public String getStringValue() {
		return m_value;
	}

	@Override
	public boolean shouldPersist(ServiceParameters params) {
		return true;
	}

}
