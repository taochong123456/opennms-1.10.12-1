package org.opennms.netmgt.syslogd.logic;

public class StartswithLogic extends LogicUnit {

	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;
		return left.toString().startsWith(right.toString());
	}

}
