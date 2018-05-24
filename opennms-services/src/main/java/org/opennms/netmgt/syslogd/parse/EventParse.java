package org.opennms.netmgt.syslogd.parse;

public interface EventParse {
	public SIMEventObject parse(String message, String host);
}
