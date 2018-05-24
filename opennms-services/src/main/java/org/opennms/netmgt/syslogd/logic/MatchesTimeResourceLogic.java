package org.opennms.netmgt.syslogd.logic;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.syslogd.analyze.ResourceTransfer;
import org.opennms.netmgt.syslogd.analyze.TimeParser;
import org.opennms.netmgt.syslogd.analyze.TimeResource;

public class MatchesTimeResourceLogic extends LogicUnit {
	/**
	 * 时间资源映射Map
	 */
	static Map<String, TimeResource> TimeResourceMap = Collections
			.synchronizedMap(new HashMap<String, TimeResource>());

	@Override
	public boolean judge(Object left, Object right) {
		// TODO Auto-generated method stub

		if (left == null || right == null)
			return false;

		long leftD = transToLong(left);

		TimeResource TimeResource = getTimeResourceByID((String) right);
		return judgeTimeRange(leftD, TimeResource);

	}

	/**
	 * 删除指定资源
	 * 
	 * @param id
	 * @return
	 */
	public static TimeResource removeResource(String id) {
		return TimeResourceMap.remove(id);
	}

	// /**
	// * 更新资源
	// * @param id
	// * @param pr
	// */
	// public static void updateResource(String id,TimeResource pr){
	// portResourceMap.put(id, pr);
	// }
	/**
	 * 更新资源
	 * 
	 * @param id
	 * @param content
	 */
	public static void updateResource(String id, String content) {
		TimeResource origin = TimeResourceMap.get(id);
		if (origin != null) {// 被过滤器的本操作符使用
			TimeParser p = new TimeParser();
			p.parse(new StringReader(content + "\n"));
			TimeResource pr = p.getTimeResource();
			TimeResourceMap.put(id, pr);
		}
	}

	/**
	 * 获取端口资源
	 * 
	 * @param id
	 * @return
	 */
	private TimeResource getTimeResourceByID(String id) {
		return null;
	}

	/**
	 * 判断指定时间是否属于指定时间资源
	 * 
	 * @param leftD
	 * @param TimeResource
	 */
	boolean judgeTimeRange(long leftD, TimeResource TimeResource) {
		return ResourceTransfer.testTimeResource(TimeResource, leftD);
	}

	public static void main(String[] args) {
		MatchesTimeResourceLogic mpr = new MatchesTimeResourceLogic();
		System.out.println(mpr.judge(System.currentTimeMillis(), "3"));
	}
}
