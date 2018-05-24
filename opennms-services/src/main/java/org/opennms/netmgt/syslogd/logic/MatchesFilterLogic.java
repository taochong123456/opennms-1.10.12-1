package org.opennms.netmgt.syslogd.logic;

import org.opennms.netmgt.syslogd.analyze.FilterDelegate;

public class MatchesFilterLogic extends LogicUnit {
	@Override
	public boolean judge(Object left, Object right) {
		if (left == null)
			return false;
		// if(left instanceof FilterDelegate && right instanceof Map){
		if (left instanceof FilterDelegate) {
			FilterDelegate filter = (FilterDelegate) left;
			return filter.isSatisfied(right);
		} else {
			// System.out.println("MatchesfilterLogic:L "+left!=null?left.getClass():"null"+" R "+right!=null?right.getClass():"null");
			String special = "Left operand "
					+ (left != null ? left.getClass() : "null")
					+ " Rright operand "
					+ (right != null ? right.getClass() : "null");
			throw new IllegalArgumentException(
					"illegal  MatchesfilterLogic params. Need FilterDelegate."
							+ special);
		}
	}

}
