package org.opennms.netmgt.syslogd.analyze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class EventServiceConfig {
	private Logger logger = Logger.getLogger(getClass().getName());
	private static String configfile = System.getProperty("manageserver.dir")
			+ "/ext/audit/eventservice.properties";
	private static EventServiceConfig serviceconfig = null;

	private static Properties config = new Properties();

	private EventServiceConfig() {

	}

	/**
	 * 获取配置实例
	 * 
	 * @return
	 */
	public static EventServiceConfig getEventServiceConfig() {
		if (serviceconfig == null)
			serviceconfig = new EventServiceConfig();
		try {
			FileInputStream in = new FileInputStream(new File(configfile));
			config.load(in);
			in.close();
		} catch (IOException e) {
		}
		return serviceconfig;
	}

	/**
	 * 读取事件服务配置
	 * 
	 * @param key
	 * @return
	 */
	public String getConfig(String key) {
		return config.getProperty(key);
	}

	/**
	 * 设置事件服务配置
	 * 
	 * @param key
	 * @param value
	 */
	public void setConfig(String key, String value) {
		config.setProperty(key, value);
	}

	/**
	 * 保存配置
	 * 
	 */
	public void save() {
		try {
			config.store(new FileOutputStream(new File(configfile)),
					"Save event service config");
		} catch (IOException e) {
			logger.info("Write event service config: ", e);
		}

	}

}
