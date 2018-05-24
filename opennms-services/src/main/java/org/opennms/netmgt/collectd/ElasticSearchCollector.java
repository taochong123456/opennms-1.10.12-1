package org.opennms.netmgt.collectd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.httpdatacollection.Attrib;

/**
 * collect ElasticSearch data
 * 
 * **/
public class ElasticSearchCollector extends HttpCollector {
	public void doCollection(final HttpCollectionSet collectionSet,
			final HttpCollectionResource collectionResource)
			throws HttpCollectorException {
		Properties props = new Properties();
		String address = collectionSet.getAgent().getAddress().toString()
				.substring(1);
		int port = collectionSet.getPort();
		props.put("HostName", address);
		props.put("Port", port + "");
		ElasticSearchClusterDataCollector elasticSearch = new ElasticSearchClusterDataCollector();
		elasticSearch.startDatacollection(props);
		Map<String, String> responseDatas = elasticSearch.getResponseMap();
		final List<Attrib> attribDefs = collectionSet.getUriDef()
				.getAttributes().getAttribCollection();
		final AttributeGroupType groupType = new AttributeGroupType(
				collectionSet.getUriDef().getName(), "all");
		List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
		for (final Attrib attribDef : attribDefs) {
			final String type = attribDef.getType();
			String attrValue = responseDatas.get(attribDef.getAlias()) + "";
			final HttpCollectionAttribute bute = new HttpCollectionAttribute(
					collectionResource, new HttpCollectionAttributeType(
							attribDef, groupType), attribDef.getAlias(), type,
					attrValue);
			butes.add(bute);

		}
		collectionSet.storeResults(butes, collectionResource);
	}
}
