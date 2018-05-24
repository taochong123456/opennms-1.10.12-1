package org.opennms.netmgt.syslogd.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class IncategoryLogic extends LogicUnit {

	private static Logger logger = Logger.getLogger(IncategoryLogic.class);

	private String querySql = "select * from net_devattrrelation r1,net_device r2 where r1.dev_id = r2.ID and r1.attr_id= ? and r2.IP=?";

	/**
	 * 属性路径与属性节点id对应的关系表。
	 */
	private static Map<String, Long> attrPathIDRelationMap = Collections
			.synchronizedMap(new HashMap<String, Long>());

	/**
	 * 获得属性路径对应叶结点的属性id.
	 * 
	 * @param attributePath
	 * @return
	 */
	private static long getNetdeviceAttributeID(String attributePath) {
		// long start = System.currentTimeMillis();
		if (attributePath == null)
			return -1;
		Long id = attrPathIDRelationMap.get(attributePath);
		if (id == null) {
			id = getNetdeviceAttributeIDInline(attributePath);
			attrPathIDRelationMap.put(attributePath, id);
		}
		// long end = System.currentTimeMillis();
		// System.out.println(end-start);
		return id;
	}

	/**
	 * 获得属性路径对应叶结点的属性id.
	 * 
	 * @param attributePath
	 * @return
	 */
	private static long getNetdeviceAttributeIDInline(String attributePath) {
		return 0;
	}

	/**
	 * 更新attrPathIDRelationMap表，此情况在添加资产属性时发生。
	 * 
	 * @param attributeID
	 */
	public static void updateIncategoryLogic(long attributeID) {
		String attributePath = getAttributePathInline(attributeID);
		Long lastAttributeID = attrPathIDRelationMap.get(attributePath);
		if (lastAttributeID != null && lastAttributeID != attributeID)
			attrPathIDRelationMap.put(attributePath, attributeID);
	}

	/**
	 * 根据属性id获得该属性路径
	 * 
	 * @param attributeID
	 * @return
	 */
	private static String getAttributePathInline(long attributeID) {
		return "";
	}

	/**
	 * 判断给定ip的设备是否具有指定属性路径的内容
	 * 
	 * @param ip
	 * @param attributePath
	 * @return
	 */
	private boolean hasAttribute(String ip, String attributePath) {
		return false;
	}

	@Override
	/**
	 * left:设备ip right:属性路径，比如"/机密/高"
	 */
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;
		if (right instanceof Object[]) {
			Object[] inParams = (Object[]) right;
			for (int i = 0; i < inParams.length; i++) {
				if (hasAttribute(left.toString(), inParams[i].toString()))
					return true;
			}
			return false;
		} else
			throw new IllegalArgumentException(
					"illegal  in params. Need Object[]");

	}

}
