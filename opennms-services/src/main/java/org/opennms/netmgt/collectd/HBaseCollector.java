package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.jmx.connectors.Jsr160ConnectionFactory;
import org.opennms.protocols.jmx.connectors.Jsr160ConnectionWrapper;

public class HBaseCollector extends JMXCollector{
	private static final String SERVICENAME = "HBase";
	public Jsr160ConnectionWrapper getMBeanServerConnection(Map<String, Object> map,
			InetAddress address) {
		return Jsr160ConnectionFactory.getMBeanServerConnection(map,
				address);
	}
	
	public HBaseCollector(){
		super();
        setServiceName(SERVICENAME);
	}
	public CollectionSet collect(CollectionAgent agent, EventProxy eproxy,
			Map<String, Object> map) {
		HBaseDataCollector hbaseCollector = new HBaseDataCollector();
		InetAddress ipaddr = (InetAddress) agent.getAddress();
		String collDir = serviceName();
		JMXCollectionSet collectionSet = new JMXCollectionSet(agent, collDir);
		collectionSet.setCollectionTimestamp(new Date());
		List<JMXCollectionResource> collectionResources = collectionSet.getResources();
		Map<String, Attrib> nameToAttr = JMXDataCollectionConfigFactory
				.getInstance().getNameToAttrib(SERVICENAME);
		Jsr160ConnectionWrapper connection = null;
		try {
			connection = getMBeanServerConnection(map, ipaddr);
			if (connection == null) {
				return collectionSet;
			}
			hbaseCollector.startDatacollection(connection);
			Map<String, String> basicInfos = hbaseCollector.getResultMap();
			JMXCollectionResource m_collectionResource = new JMXCollectionResource(agent, collDir);
			for (Iterator iterator = basicInfos.keySet().iterator(); iterator
					.hasNext();) {
				String key = (String) iterator.next();
				String value = basicInfos.get(key);
				AttributeGroupType attribGroupType = new AttributeGroupType(
						key, "all");
				JMXDataSource dataSource = new JMXDataSource();
				Attrib attr = nameToAttr.get(key);
				if (attr == null) {
					continue;
				}
				dataSource.setName(attr.getName());
				dataSource.setType(attr.getType());
				JMXCollectionAttributeType jmxCollectionAttributeType = new JMXCollectionAttributeType(
						dataSource, null, null, attribGroupType);
				m_collectionResource.setAttributeValue(
						jmxCollectionAttributeType, value);
     			collectionResources.add(m_collectionResource);

			}
		} catch (final Exception e) {
			LogUtils.errorf(this, e, "Error getting MBeanServer");
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
		return collectionSet;

	}

}
