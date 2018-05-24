/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.redis.authen.Authen;
import org.opennms.netmgt.config.redis.authen.RedisConfig;
import org.springframework.core.io.FileSystemResource;

/**
 * This class is the main repository for redis configuration information used by
 * the capabilities daemon. When this class is loaded configuration into memory
 */
public class RedisPeerFactory {
	/**
	 * The singleton instance of this factory
	 */
	private static RedisPeerFactory m_singleton = null;

	/**
	 * The config class loaded from the config file
	 */
	private static RedisConfig m_config;

	/**
	 * This member is set to true if the configuration file has been loaded.
	 */
	private static boolean m_loaded = false;

	/**
	 * Private constructor
	 * 
	 * @exception java.io.IOException
	 *                Thrown if the specified config file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException
	 *                Thrown if the file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException
	 *                Thrown if the contents do not match the required schema.
	 * 
	 * @param configFile
	 *            the path to the config file to load in.
	 */
	private RedisPeerFactory(String configFile) throws IOException,
			MarshalException, ValidationException {
		m_config = CastorUtils.unmarshal(RedisConfig.class,
				new FileSystemResource(configFile));
	}

	/**
	 * <p>
	 * Constructor for RedisPeerFactory.
	 * </p>
	 * 
	 * @param stream
	 *            a {@link java.io.InputStream} object.
	 * @throws org.exolab.castor.xml.MarshalException
	 *             if any.
	 * @throws org.exolab.castor.xml.ValidationException
	 *             if any.
	 */
	public RedisPeerFactory(InputStream stream) throws MarshalException,
			ValidationException {
		m_config = CastorUtils.unmarshal(RedisConfig.class, stream);
	}

	/**
	 * Load the config from the default config file and create the singleton
	 * instance of this factory.
	 * 
	 * @exception java.io.IOException
	 *                Thrown if the specified config file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException
	 *                Thrown if the file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException
	 *                Thrown if the contents do not match the required schema.
	 * @throws java.io.IOException
	 *             if any.
	 * @throws org.exolab.castor.xml.MarshalException
	 *             if any.
	 * @throws org.exolab.castor.xml.ValidationException
	 *             if any.
	 */
	public static synchronized void init() throws IOException,
			MarshalException, ValidationException {
		if (m_loaded) {
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		File cfgFile = ConfigFileConstants
				.getFile(ConfigFileConstants.REDIS_CONFIG_FILE_NAME);

		log().debug("init: config file path: " + cfgFile.getPath());

		m_singleton = new RedisPeerFactory(cfgFile.getPath());

		m_loaded = true;
	}

	private static ThreadCategory log() {
		return ThreadCategory.getInstance(RedisPeerFactory.class);
	}

	/**
	 * Reload the config from the default config file
	 * 
	 * @exception java.io.IOException
	 *                Thrown if the specified config file cannot be read/loaded
	 * @exception org.exolab.castor.xml.MarshalException
	 *                Thrown if the file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException
	 *                Thrown if the contents do not match the required schema.
	 * @throws java.io.IOException
	 *             if any.
	 * @throws org.exolab.castor.xml.MarshalException
	 *             if any.
	 * @throws org.exolab.castor.xml.ValidationException
	 *             if any.
	 */
	public static synchronized void reload() throws IOException,
			MarshalException, ValidationException {
		m_singleton = null;
		m_loaded = false;

		init();
	}

	/**
	 * Package-private access. Should only be used for unit testing.
	 */
	RedisConfig getConfig() {
		return m_config;
	}

	/**
	 * Saves the current settings to disk
	 * 
	 * @throws java.lang.Exception
	 *             if saving settings to disk fails.
	 */
	public static synchronized void saveCurrent() throws Exception {
		optimize();

		// Marshal to a string first, then write the string to the file. This
		// way the original config
		// isn't lost if the XML from the marshal is hosed.
		StringWriter stringWriter = new StringWriter();
		Marshaller.marshal(m_config, stringWriter);
		if (stringWriter.toString() != null) {
			Writer fileWriter = new OutputStreamWriter(
					new FileOutputStream(ConfigFileConstants
							.getFile(ConfigFileConstants.REDIS_CONFIG_FILE_NAME)),
					"UTF-8");
			fileWriter.write(stringWriter.toString());
			fileWriter.flush();
			fileWriter.close();
		}

		reload();
	}

	/**
	 * Combine specific and range elements so that RedisPeerFactory has to spend
	 * less time iterating all these elements. TODO This really should be pulled
	 * up into PeerFactory somehow, but I'm not sure how (given that
	 * "Definition" is different for both SNMP and Redis. Maybe some sort of
	 * visitor methodology would work. The basic logic should be fine as it's
	 * all IP address manipulation
	 * 
	 * @throws UnknownHostException
	 */
	static void optimize() throws UnknownHostException {
	}

	/**
	 * Return the singleton instance of this factory.
	 * 
	 * @return The current factory instance.
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the factory has not yet been initialized.
	 */
	public static synchronized RedisPeerFactory getInstance() {
		if (!m_loaded)
			throw new IllegalStateException(
					"The WmiPeerFactory has not been initialized");

		return m_singleton;
	}

	/**
	 * <p>
	 * setInstance
	 * </p>
	 * 
	 * @param singleton
	 *            a {@link org.opennms.netmgt.config.RedisPeerFactory} object.
	 */
	public static synchronized void setInstance(RedisPeerFactory singleton) {
		m_singleton = singleton;
		m_loaded = true;
	}

	/**
	 * <p>
	 * getAgentConfig
	 * </p>
	 * 
	 * @param agentInetAddress
	 *            a {@link java.net.InetAddress} object.
	 */
	public synchronized Authen getAuthenConfig(InetAddress agentInetAddress) {
		Authen[] authens = m_config.getAuthen();
		for (Authen authen : authens) {
			if (agentInetAddress.toString().equals("/"+authen.getIp())) {
				return authen;
			}
		}
		return null;

	}

	/**
	 * <p>
	 * getRedisConfig
	 * </p>
	 * 
	 * @return a {@link org.opennms.netmgt.config.redis.RedisConfig object.
	 */
	public static RedisConfig getRedisConfig() {
		return m_config;
	}

	/**
	 * <p>
	 * setWmiConfig
	 * </p>
	 * 
	 * @param m_config
	 *            a {@link org.opennms.netmgt.config.redis.RedisConfig} object.
	 */
	public static synchronized void setRedisConfig(RedisConfig m_config) {
		RedisPeerFactory.m_config = m_config;
	}
}
