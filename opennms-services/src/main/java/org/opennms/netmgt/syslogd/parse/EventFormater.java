package org.opennms.netmgt.syslogd.parse;
import java.util.Map;
public interface EventFormater {
	public void initDeviceMap(Map devMap);
	public SIMEventObject[] format(SIMEventObject event);
}
