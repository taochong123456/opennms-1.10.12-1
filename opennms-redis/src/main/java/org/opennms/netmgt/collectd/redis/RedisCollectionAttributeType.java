package org.opennms.netmgt.collectd.redis;

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.redis.Attrib;

public class RedisCollectionAttributeType implements CollectionAttributeType {
	Attrib m_attribute;
	AttributeGroupType m_groupType;

	public RedisCollectionAttributeType(final Attrib attribute,
			final AttributeGroupType groupType) {
		m_groupType = groupType;
		m_attribute = attribute;
	}

	@Override
	public String getType() {
		return m_attribute.getType();
	}

	@Override
	public String getName() {
		return m_attribute.getName();
	}

	@Override
	public AttributeGroupType getGroupType() {
		return m_groupType;
	}

	@Override
	public void storeAttribute(CollectionAttribute attribute,
			Persister persister) {
		if ("string".equalsIgnoreCase(m_attribute.getType())) {
			persister.persistStringAttribute(attribute);
		} else {
			persister.persistNumericAttribute(attribute);
		}
	}
}
