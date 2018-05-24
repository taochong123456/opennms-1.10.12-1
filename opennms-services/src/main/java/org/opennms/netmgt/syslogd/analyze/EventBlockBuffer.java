package org.opennms.netmgt.syslogd.analyze;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

class EventBlockBuffer {
	// 数组中发生时可最早事件的接收时间，也做为时间的查找索引。
	long receptTime;
	// 发生时刻最早的事件在数组的下标
	int index = 0;

	/**
	 * 相关的事件对像数组
	 */
	SIMEventObject[] buffer;

	EventBlockBuffer next;

	EventBlockBuffer pre;
}