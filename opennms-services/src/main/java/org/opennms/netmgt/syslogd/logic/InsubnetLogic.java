package org.opennms.netmgt.syslogd.logic;

public class InsubnetLogic extends LogicUnit {

	@Override
	/**
	 * ip属于操做符
	 */
	public boolean judge(Object left, Object right) {
		return false;
	}

}
