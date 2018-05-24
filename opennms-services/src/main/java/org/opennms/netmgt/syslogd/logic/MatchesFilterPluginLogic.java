package org.opennms.netmgt.syslogd.logic;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.syslogd.analyze.FilterPlugin;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class MatchesFilterPluginLogic extends LogicUnit {

	@Override
	public boolean judge(Object left, Object right) {
		// TODO Auto-generated method stub
		if (left == null || right == null)
			return false;

		if (left instanceof FilterPlugin) {// 左操作数需要为FilterPlugin类型
			if (right instanceof List) {// 右操作数需要为List类型，并且List(0)为SIMEventObject或Map，List(1)为String[]
				List arguments = (List) right;
				if (arguments.size() >= 2) {
					if (arguments.get(0) instanceof SIMEventObject) {
						return ((FilterPlugin) left).judge(
								(SIMEventObject) arguments.get(0),
								arguments.get(1) != null ? (String[]) arguments
										.get(1) : null);
					} else if (arguments.get(0) instanceof Map) {
						return ((FilterPlugin) left).judge(
								(Map) arguments.get(0),
								arguments.get(1) != null ? (String[]) arguments
										.get(1) : null);
					}
				}
			}
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
		return false;
	}

}
