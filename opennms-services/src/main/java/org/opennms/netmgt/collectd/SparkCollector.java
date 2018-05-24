package org.opennms.netmgt.collectd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.JsonTransform;
import org.opennms.netmgt.collectd.HttpCollector.HttpCollectionAttribute;
import org.opennms.netmgt.collectd.HttpCollector.HttpCollectionAttributeType;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.httpdatacollection.Attrib;

/**
 * Collect spark data via URI
 * 
 */
public class SparkCollector extends HttpCollector {
	public void doCollection(final HttpCollectionSet collectionSet,
			final HttpCollectionResource collectionResource)
			throws HttpCollectorException {
		Properties props = new Properties();
		String address = collectionSet.getAgent().getAddress().toString()
				.substring(1);
		int port = collectionSet.getPort();
		props.put("HostName", address);
		props.put("Port", port + "");
		ApacheSparkWorkerDatacollector sparkWorkerCollector = new ApacheSparkWorkerDatacollector();
		sparkWorkerCollector.startDatacollection(props);
		Map<String, String> sparkDataMap = sparkWorkerCollector
				.getResponseMap();
		final List<Attrib> attribDefs = collectionSet.getUriDef()
				.getAttributes().getAttribCollection();
		final AttributeGroupType groupType = new AttributeGroupType(
				collectionSet.getUriDef().getName(), "all");
		List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
		for (final Attrib attribDef : attribDefs) {
			final String type = attribDef.getType();
			String attrValue = sparkDataMap.get(attribDef.getAlias()) + "";
			final HttpCollectionAttribute bute = new HttpCollectionAttribute(
					collectionResource, new HttpCollectionAttributeType(
							attribDef, groupType), attribDef.getAlias(), type,
					attrValue);
			butes.add(bute);

		}
		collectionSet.storeResults(butes, collectionResource);
	}
}
