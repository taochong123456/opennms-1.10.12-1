package org.opennms.netmgt.syslogd.logic;

public class InICLogic extends LogicUnit {

	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;
		if (right instanceof Object[]) {
			Object[] inParams = (Object[]) right;
			for (int i = 0; i < inParams.length; i++) {
				if (inParams[i].toString().toLowerCase()
						.equals(left.toString().toLowerCase()))
					return true;
			}
			return false;
		} else
			throw new IllegalArgumentException(
					"illegal  in params. Need Object[]");
	}

}
