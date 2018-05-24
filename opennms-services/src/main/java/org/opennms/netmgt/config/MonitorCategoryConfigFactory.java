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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.monitor.MonitorCategory;

/**
 * <P>
 * This class loads and presents the monitor category configuration file
 * </P>
 *
 * @author Chong
 */
public class MonitorCategoryConfigFactory {

     /** The singleton instance. */
     private static MonitorCategoryConfigFactory m_instance;

     /** Boolean indicating if the init() method has been called. */
     protected boolean initialized = false;

     /** Timestamp of the WMI collection config, used to know when to reload from disk. */
     protected static long m_lastModified;

     private static MonitorCategory m_config;

     /**
      * <p>Constructor for RedisDataCollectionConfigFactory.</p>
      *
      * @param configFile a {@link java.lang.String} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      * @throws java.io.IOException if any.
      */
     public MonitorCategoryConfigFactory(String configFile) throws MarshalException, ValidationException, IOException {
         InputStream is = null;

         try {
             is = new FileInputStream(configFile);
             initialize(is);
         } finally {
             if (is != null) {
                 IOUtils.closeQuietly(is);
             }
         }
     }

     /**
      * <p>Constructor for RedisDataCollectionConfigFactory.</p>
      *
      * @param is a {@link java.io.InputStream} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public MonitorCategoryConfigFactory(InputStream is) throws MarshalException, ValidationException {
         initialize(is);
     }

     private void initialize(InputStream stream) throws MarshalException, ValidationException {
         log().debug("initialize: initializing WMI collection config factory.");
         m_config = CastorUtils.unmarshal(MonitorCategory.class, stream);
     }
     
     /**
      * Be sure to call this method before calling getInstance().
      *
      * @throws java.io.IOException if any.
      * @throws java.io.FileNotFoundException if any.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
         if (m_instance == null) {
             File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.MONITORCATEGORY_CONFIG_FILE_NAME);
             m_instance = new MonitorCategoryConfigFactory(cfgFile.getPath());
             m_lastModified = cfgFile.lastModified();
         }
     }

     /**
      * Singleton static call to get the only instance that should exist
      *
      * @return the single factory instance
      * @throws java.lang.IllegalStateException
      *             if init has not been called
      */
     public static synchronized MonitorCategoryConfigFactory getInstance() {

         if (m_instance == null) {
             throw new IllegalStateException("You must call RedisDataCollectionConfigFactory.init() before calling getInstance().");
         }
         return m_instance;
     }

     /**
      * <p>getConfig</p>
      *
      */
     public synchronized static MonitorCategory getConfig() {
         return m_config;
     }

     private ThreadCategory log() {
         return ThreadCategory.getInstance();
     }

 }

