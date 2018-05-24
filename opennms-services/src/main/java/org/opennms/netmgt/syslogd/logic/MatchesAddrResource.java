package org.opennms.netmgt.syslogd.logic;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.syslogd.analyze.IPParser;
import org.opennms.netmgt.syslogd.analyze.IPResource;
import org.opennms.netmgt.syslogd.analyze.ResourceTransfer;

public class MatchesAddrResource extends LogicUnit {

	/**
	 * 端口资源映射Map
	 */
	static Map<String, IPResource> ipResourceMap = Collections
			.synchronizedMap(new HashMap<String, IPResource>());

	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;

		long leftD = 1;
		IPResource IPResource = getIPResourceByID((String) right);
		return judgeIPRange(leftD, IPResource);

	}

	/**
	 * 删除指定资源
	 * 
	 * @param id
	 * @return
	 */
	public static IPResource removeResource(String id) {
		return ipResourceMap.remove(id);
	}

	// /**
	// * 更新资源
	// * @param id
	// * @param pr
	// */
	// public static void updateResource(String id,IPResource pr){
	// portResourceMap.put(id, pr);
	// }
	/**
	 * 更新资源
	 * 
	 * @param id
	 * @param content
	 */
	public static void updateResource(String id, String content) {
		IPResource origin = ipResourceMap.get(id);
		if (origin != null) {// 被过滤器的本操作符使用
			IPParser p = new IPParser();
			p.parse(new StringReader(content + "\n"));
			IPResource pr = p.getIPResource();
			ipResourceMap.put(id, pr);
		}
	}

	/**
	 * 获取端口资源
	 * 
	 * @param id
	 * @return
	 */
	private IPResource getIPResourceByID(String id) {
		return null;
	}

	/**
	 * 判断指定IP是否属于指定IP地址资源
	 * 
	 * @param leftD
	 * @param IPResource
	 */
	boolean judgeIPRange(long leftD, IPResource IPResource) {
		return ResourceTransfer.testIPResource(IPResource, leftD);
	}

	public static void main(String[] args) {
		MatchesAddrResource mpr = new MatchesAddrResource();
		System.out.println(mpr.judge("202.38.64.1", "2"));
	}
}
