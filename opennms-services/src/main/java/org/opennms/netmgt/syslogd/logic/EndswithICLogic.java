package org.opennms.netmgt.syslogd.logic;
public class EndswithICLogic extends LogicUnit {
	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;
		return left.toString().toLowerCase()
				.endsWith(right.toString().toLowerCase());
	}

}
