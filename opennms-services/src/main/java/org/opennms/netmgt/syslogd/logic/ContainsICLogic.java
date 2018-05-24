package org.opennms.netmgt.syslogd.logic;

import org.opennms.netmgt.syslogd.analyze.StringTools;

public class ContainsICLogic extends LogicUnit {
	@Override
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;

		return StringTools.contains(left.toString().toLowerCase(), right
				.toString().toLowerCase());
	}

}
