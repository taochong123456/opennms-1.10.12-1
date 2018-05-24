package org.opennms.netmgt.syslogd.analyze;

import java.util.Map;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public interface FilterPlugin {
	/**
	 * 根据传入的event对象和预先设置的参数，进行逻辑判断。
	 * 
	 * @param event
	 * @param parameters
	 * @return 符合该plugin的逻辑返回true，否则返回false
	 */
	public boolean judge(SIMEventObject event, String[] parameters);

	/**
	 * 根据传入的event对象和预先设置的参数，进行逻辑判断。
	 * 
	 * @param event
	 *            类型为Map，key为小写字母的事件属性【属性来自SIMEventObject定义】，值为该事件属性对应的对象。
	 * @param parameters
	 * @return 符合该plugin的逻辑返回true，否则返回false
	 */
	public boolean judge(Map event, String[] parameters);
}
