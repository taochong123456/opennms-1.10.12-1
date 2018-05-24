package org.opennms.netmgt.syslogd.logic;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.syslogd.analyze.Parser;
import org.opennms.netmgt.syslogd.analyze.PortResource;
import org.opennms.netmgt.syslogd.analyze.ResourceTransfer;

public class MatchesPortResourceLogic extends LogicUnit {
	/**
	 * 端口资源映射Map
	 */
	static Map<String, PortResource> portResourceMap = Collections
			.synchronizedMap(new HashMap<String, PortResource>());

	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;

		long leftD = transToLong(left);
		PortResource portResource = getPortResourceByID((String) right);
		return judgePortRange(leftD, portResource);

	}

	/**
	 * 删除指定资源
	 * 
	 * @param id
	 * @return
	 */
	public static PortResource removeResource(String id) {
		return portResourceMap.remove(id);
	}

	// /**
	// * 更新资源
	// * @param id
	// * @param pr
	// */
	// public static void updateResource(String id,PortResource pr){
	// portResourceMap.put(id, pr);
	// }
	/**
	 * 更新资源
	 * 
	 * @param id
	 * @param content
	 */
	public static void updateResource(String id, String content) {
		PortResource origin = portResourceMap.get(id);
		if (origin != null) {// 被过滤器的本操作符使用
			Parser p = new Parser();
			p.parse(new StringReader(content + "\n"));
			PortResource pr = p.getPortResource();
			portResourceMap.put(id, pr);
		}
	}

	/**
	 * 获取端口资源
	 * 
	 * @param id
	 * @return
	 */
	private PortResource getPortResourceByID(String id) {
		return null;
	}

	/**
	 * 判断指定端口是否属于指定端口资源
	 * 
	 * @param leftD
	 * @param portResource
	 */
	boolean judgePortRange(long leftD, PortResource portResource) {
		return ResourceTransfer.testPortResource(portResource, leftD);
	}

	public static void main(String[] args) {
		MatchesPortResourceLogic mpr = new MatchesPortResourceLogic();
		System.out.println(mpr.judge(550, "1"));
	}
}
