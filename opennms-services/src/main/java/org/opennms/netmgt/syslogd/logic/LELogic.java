package org.opennms.netmgt.syslogd.logic;

public class LELogic extends LogicUnit {
	@Override
	/**
	 * 小于等于比较。该函数目前具有局限性，对Float与Double类型之间的比较存在误差。
	 */
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;

		double leftD = transToDouble(left);
		double rightD = transToDouble(right);
		return leftD <= rightD;
	}

}
