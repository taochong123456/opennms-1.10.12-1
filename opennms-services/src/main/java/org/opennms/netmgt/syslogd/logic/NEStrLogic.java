package org.opennms.netmgt.syslogd.logic;

public class NEStrLogic extends LogicUnit {

	@Override
	public boolean judge(Object left, Object right) {

		if (left == null) {
			if (right == null)
				return true;
			else
				return false;
		} else {
			if (right == null)
				return false;
		}

		return !left.toString().equals(right.toString());
	}

}
