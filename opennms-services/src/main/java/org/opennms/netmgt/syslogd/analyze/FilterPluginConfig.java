package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.xpath.XPath;

public class FilterPluginConfig {
	/**
	 * 日志接口。
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private static FilterPluginConfig filterPluginConfig = null;

	private static String filterPluginConfigFilePath = System
			.getProperty("manageserver.dir") + "/ext/audit/filterconfig.xml";

	/**
	 * xml解析器。
	 */
	private XmlManager filterPluginConfigXMLManager = new XmlManager();

	/**
	 * 注册的过滤器插件类型。
	 */
	private List<String[]> registeredFilterPlugintypes;

	/**
	 * 注册的过滤器插件类
	 */
	private Map<String, FilterPlugin> pluginMap = new HashMap<String, FilterPlugin>();

	/**
	 * 加载filterconfig.xml。
	 * 
	 */
	private void loadFilterConfigFile() {
		try {
			filterPluginConfigXMLManager.read(filterPluginConfigFilePath);

		} catch (Exception e) {
			logger.error("Read filerPconfig.xml error." + e);
		}
	}

	/**
	 * 获取事件元数据.每一个xml元素都名为type,有ID,Name,Description,Visible,Class五个属性。
	 * 
	 * @param rootElement
	 * @param xpathStr
	 * @return
	 */
	private List<String[]> fetchMetaData(Element rootElement, String xpathStr) {
		List<String[]> result = new ArrayList<String[]>();

		try {
			List elements = XPath.selectNodes(rootElement, xpathStr);

			for (int i = 0; i < elements.size(); i++) {
				Element dicElement = (Element) elements.get(i);

				if (dicElement != null) {
					String[] values = new String[5];
					values[0] = dicElement.getAttributeValue("ID") != null ? dicElement
							.getAttributeValue("ID") : "";
					values[1] = dicElement.getAttributeValue("Name") != null ? dicElement
							.getAttributeValue("Name") : "";
					values[2] = dicElement.getAttributeValue("Description") != null ? dicElement
							.getAttributeValue("Description") : "";
					values[3] = dicElement.getAttributeValue("Visible") != null ? dicElement
							.getAttributeValue("Visible") : "true";
					values[4] = dicElement.getAttributeValue("Class") != null ? dicElement
							.getAttributeValue("Class") : "";
					if (StringTools.hasContent(values[0]))// 保证有ID值
						result.add(values);
				}
			}

		} catch (Exception e) {
			logger.error("fetch filter plugin metadata error,", e);
		}
		return result;

	}

	private void setRegisteredFilterPlugintypes(
			List<String[]> registeredFilterPlugintypes) {
		this.registeredFilterPlugintypes = registeredFilterPlugintypes;
	}

	/**
	 * 初始化过滤器动作类型。
	 * 
	 */
	private void initFilterPluginTypes() {
		try {
			// 设置过滤器描述信息
			setRegisteredFilterPlugintypes(fetchMetaData(
					filterPluginConfigXMLManager.getRootElement(),
					"/filterconfig/system/registered-filterplugintypes/type"));
			// 加载类
			if (registeredFilterPlugintypes != null) {
				for (int i = 0; i < registeredFilterPlugintypes.size(); i++) {
					try {
						ClassLoader classloader = Thread.currentThread()
								.getContextClassLoader();
						// 将type的id和对应的class加载进pluginMap
						pluginMap.put(
								registeredFilterPlugintypes.get(i)[0],
								(FilterPlugin) classloader.loadClass(
										registeredFilterPlugintypes.get(i)[4])
										.newInstance());
					} catch (Exception e) {
						logger.error("load filterplugin "
								+ registeredFilterPlugintypes.get(i)[0] + " "
								+ registeredFilterPlugintypes.get(i)[4]
								+ " error");
					}
				}
			}

		} catch (Exception e) {
			logger.error("init filterplugin metadata error,", e);
		}

	}

	/**
	 * 初始化，加载xml文件，获取注册的plugin类型
	 */
	private void init() {
		loadFilterConfigFile();
		initFilterPluginTypes();
	}

	/**
	 * 测试打印。
	 * 
	 * @param values
	 */
	private void debug_print(List<String[]> valueList) {

		if (valueList == null)
			return;
		for (int i = 0; i < valueList.size(); i++) {
			String[] values = valueList.get(i);
			if (values.length >= 5) {
				if (logger.isDebugEnabled())
					logger.debug("load filter ID:" + values[0] + " Name:"
							+ values[1] + " Desc:" + values[2] + " Visible:"
							+ values[3] + " Class:" + values[4]);
			} else {
				if (logger.isDebugEnabled())
					logger.debug("load filter ID:" + values[0] + " Name:"
							+ values[1] + " Desc:" + values[2]);
			}

		}
	}

	/**
	 * 获得配置实例。
	 * 
	 * @return
	 */
	public static FilterPluginConfig getFilterPluginConfig() {
		if (filterPluginConfig != null)
			return filterPluginConfig;
		synchronized (FilterPluginConfig.class) {
			filterPluginConfig = new FilterPluginConfig();
			filterPluginConfig.init();
		}
		return filterPluginConfig;
	}

	/**
	 * 获得全部注册的过滤器类型.列表的每个元素的结构为String[5],对应xml文件中的一个type元素,String[0]对应"ID",
	 * String[1]对应"Name",String[2]对应"Description,String[3]对应"Visible,String[4]对应
	 * "Class"
	 * 
	 * @return
	 */
	public List<String[]> getRegisteredFilterPlugintypes() {
		return registeredFilterPlugintypes;
	}

	/**
	 * 根据过滤器ID获取FilterPlugin对像
	 * 
	 * @param typeID
	 * @return
	 */
	public FilterPlugin getFilterPluginByID(String typeID) {
		return pluginMap.get(typeID);
	}

	/**
	 * 获取注册的过滤器插件类型。列表的每个元素的结构为String[3],对应xml文件中的一个type元素,String[0]对应"ID",
	 * String[1]对应"Name",String[2]对应"Description"
	 * 
	 * @return
	 */
	public List<String[]> getVisibleFilterPluginTypes() {
		List<String[]> temp = getRegisteredFilterPlugintypes();
		List<String[]> result = new ArrayList<String[]>();
		if (temp != null) {
			for (int i = 0; i < temp.size(); i++) {
				String[] plugin = temp.get(i);
				if (plugin != null && plugin.length >= 5) {
					String visible = plugin[3];// 第四个元素是visible属性
					if (visible != null
							&& visible.toLowerCase().trim().equals("false")) {
					} else {
						String[] copy = new String[3];
						copy[0] = plugin[0];
						copy[1] = plugin[1];
						copy[2] = plugin[2];
						result.add(copy);
					}
				}
			}
		}
		return result;
	}

}
