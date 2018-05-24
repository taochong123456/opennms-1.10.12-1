package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.redis.RedisAgentState;
import org.opennms.netmgt.collectd.redis.RedisCollectionAttributeType;
import org.opennms.netmgt.collectd.redis.RedisCollectionResource;
import org.opennms.netmgt.collectd.redis.RedisCollectionSet;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.RedisDataCollectionConfigFactory;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.redis.Attrib;
import org.opennms.netmgt.config.redis.RedisCollection;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;

/**
 * This class is designed to be used by the performance collection daemon to
 * collect various redis performance metrics from a remote server.
 * @author Chong
 * */
public class RedisCollector implements ServiceCollector {
	private HashMap<String, AttributeGroupType> m_groupTypeList = new HashMap<String, AttributeGroupType>();
	private HashMap<String, RedisCollectionAttributeType> m_attribTypeList = new HashMap<String, RedisCollectionAttributeType>();

	@Override
	public void initialize(Map<String, String> parameters)
			throws CollectionInitializationException {
		try {
			RedisDataCollectionConfigFactory.init();
			initializeRrdDirs();
			initDatabaseConnectionFactory();
		} catch (MarshalException e) {
			LogUtils.errorf(this, e, "Unable to initialize RedisCollector");
		} catch (ValidationException e) {
			LogUtils.errorf(this, e, "Unable to initialize RedisCollector");
		} catch (FileNotFoundException e) {
			LogUtils.errorf(this, e, "Unable to initialize RedisCollector");
		} catch (IOException e) {
			LogUtils.errorf(this, e, "Unable to initialize RedisCollector");
		}

	}

	private void initializeRrdDirs() {
		/*
		 * If the RRD file repository directory does NOT already exist, create
		 * it.
		 */
		File f = new File(RedisDataCollectionConfigFactory.getConfig()
				.getRrdRepository());
		if (!f.isDirectory()) {
			if (!f.mkdirs()) {
				throw new RuntimeException(
						"Unable to create RRD file "
								+ "repository.  Path doesn't already exist and could not make directory: ");
			}
		}
	}

	private void initDatabaseConnectionFactory() {
		try {
			DataSourceFactory.init();
		} catch (Exception e) {
		}
	}

	@Override
	public void initialize(CollectionAgent agent, Map<String, Object> parameters)
			throws CollectionInitializationException {

	}

	@Override
	public void release(CollectionAgent agent) {

	}
    /**
     * core redis collection method
     * **/
	@Override
	public CollectionSet collect(CollectionAgent agent, EventProxy eproxy,
			Map<String, Object> parameters) throws CollectionException {
		String collectionName = ParameterMap
				.getKeyedString(parameters, "collection", ParameterMap
						.getKeyedString(parameters, "redis-collection", null));
		RedisCollection collection = RedisDataCollectionConfigFactory
				.getInstance().getRedisCollection(collectionName);
		loadAttributeGroupList(collection);
		loadAttributeTypeList(collection);
		RedisCollectionSet collectorSet = new RedisCollectionSet(agent,collectionName);
		collectorSet.setCollectionTimestamp(new Date());
		RedisAgentState agentState = new RedisAgentState(agent.getInetAddress());
		Map<String, String> redisInfo = null;
		try {
			agentState.connect();
			redisInfo = agentState.getRedisClient().getRedisInfo();
			RedisCollectionResource resource = collectorSet
					.getRedisCollectionResource();
			for (final Attrib attr : collection.getRedis().getAttrib()) {
				AttributeGroupType attributeGroup = new AttributeGroupType(
						attr.getName(), "all");
				CollectionAttributeType collectionAttributeType = new RedisCollectionAttributeType(
						attr, attributeGroup);
				resource.setAttributeValue(collectionAttributeType,
						redisInfo.get(attr.getName()));
			}
			collectorSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
		} catch (Exception e) {
			LogUtils.errorf(this, e, "get redis info failed");
		} finally {
			if(!agentState.isClosed()){
				agentState.close();
			}
		}
		return collectorSet;
	}

	private void loadAttributeGroupList(final RedisCollection collection) {
		for (final Attrib attr : collection.getRedis().getAttrib()) {
			final AttributeGroupType attribGroupType = new AttributeGroupType(
					attr.getName(), attr.getType());
			m_groupTypeList.put(attr.getName(), attribGroupType);
		}
	}

	private void loadAttributeTypeList(final RedisCollection collection) {
		for (final Attrib attr : collection.getRedis().getAttrib()) {
			final AttributeGroupType attribGroupType = m_groupTypeList.get(attr
					.getName());
			final RedisCollectionAttributeType attribType = new RedisCollectionAttributeType(
					attr, attribGroupType);
			m_attribTypeList.put(attr.getName(), attribType);
		}

	}

	@Override
	public RrdRepository getRrdRepository(String collectionName) {
		return RedisDataCollectionConfigFactory.getInstance().getRrdRepository(
				collectionName);
	}

	@Override
	public void release() {

	}

}
