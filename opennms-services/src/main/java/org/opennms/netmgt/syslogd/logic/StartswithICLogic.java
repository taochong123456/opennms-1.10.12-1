package org.opennms.netmgt.syslogd.logic;

public class StartswithICLogic extends LogicUnit {

	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;
		return left.toString().toLowerCase()
				.startsWith(right.toString().toLowerCase());
	}

}
