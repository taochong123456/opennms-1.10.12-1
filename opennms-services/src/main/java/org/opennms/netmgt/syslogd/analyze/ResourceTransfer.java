package org.opennms.netmgt.syslogd.analyze;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Calendar;
public class ResourceTransfer {
	public static final int EVERYTIME = 100;
	public static void walkSyntaxTree(SyntaxNode root, int position) {
		String production = "Terminal";
		for (int i = 0; i < position; i++) {
			System.out.print(" ");
		}
		System.out.println("Rule:" + root.getProduction() + " " + production
				+ " [token:" + root.getNodeValue().tokenType + ","
				+ root.getNodeValue().sval + "," + root.getNodeValue().dval
				+ "]" + " childrenSize:" + root.getChildrenNodes().size());
		for (int i = 0; i < root.getChildrenNodes().size(); i++) {
			walkSyntaxTree((SyntaxNode) root.getChildrenNodes().get(i),
					position + 4);
		}
	}

	public static boolean testPortResource(PortResource pr, long port) {
		boolean result = false;
		if (pr == null)
			return false;
		if (pr.getSegments() == null) {
			if (pr.getEndPort() == -1) {
				if (pr.getStartPort() == port)
					result = true;
			} else {
				if (pr.getStartPort() <= port && pr.getEndPort() >= port)
					result = true;
			}
		} else {
			for (int i = 0; i < pr.getSegments().size(); i++) {
				result = testPortResource(pr.getSegments().get(i), port);
				if (result == true)
					break;
			}
		}

		if (pr.getLogicFlag() == 1)
			result = !result;

		return result;
	}

	public static boolean testIPResource(IPResource ipResource, long ip) {
		boolean result = false;
		if (ipResource == null)
			return false;
		if (ipResource.getSegments() == null) {
			if (ipResource.getInternEndIP() == -1) {
				if (ipResource.getInternStartIP() == ip)
					result = true;
			} else {
				if (ipResource.getInternStartIP() <= ip
						&& ipResource.getInternEndIP() >= ip)
					result = true;
			}
		} else {
			for (int i = 0; i < ipResource.getSegments().size(); i++) {
				result = testIPResource(ipResource.getSegments().get(i), ip);
				if (result == true)
					break;
			}
		}

		if (ipResource.getLogicFlag() == 1)
			result = !result;

		return result;
	}

	public static boolean testTimeResource(TimeResource timeResource, long time) {
		boolean result = false;
		if (timeResource == null)
			return false;
		if (timeResource.getSegments() == null) {
			result = testSingleTimeResource(timeResource, time);
		} else {
			for (int i = 0; i < timeResource.getSegments().size(); i++) {
				result = testTimeResource(timeResource.getSegments().get(i),
						time);
				if (result == true)
					break;
			}
		}

		if (timeResource.getLogicFlag() == 1)
			result = !result;

		return result;
	}

	private static boolean testSingleTimeResource(TimeResource timeResource,
			long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);

		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTimeInMillis(time);

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTimeInMillis(time);

		if (timeResource.getStartYear() != EVERYTIME) {// 设置了年的比较
			startCalendar.set(Calendar.YEAR, timeResource.getStartYear());
			endCalendar.set(Calendar.YEAR, timeResource.getEndYear());
		}
		if (timeResource.getStartMonth() != EVERYTIME) {// 设置了月的比较
			startCalendar.set(Calendar.MONTH, timeResource.getStartMonth() - 1);// -1是因为java的年从0计数，TimeResource从1计数
			endCalendar.set(Calendar.MONTH, timeResource.getEndMonth() - 1);
		}

		if (timeResource.getTimeFlag() == 0) {// 每月的第几天
			if (timeResource.getStartDay() != EVERYTIME) {// 设置了天的比较
				startCalendar.set(Calendar.DAY_OF_MONTH,
						timeResource.getStartDay());
				endCalendar
						.set(Calendar.DAY_OF_MONTH, timeResource.getEndDay());
			}
		} else {// 每周的第几天
			if (timeResource.getStartWeek() != EVERYTIME) {// 设置了周的比较
				startCalendar.set(Calendar.WEEK_OF_MONTH,
						timeResource.getStartWeek());
				endCalendar.set(Calendar.WEEK_OF_MONTH,
						timeResource.getEndWeek());
			}
			if (timeResource.getStartDay() != EVERYTIME) {// 设置了天的比较
				startCalendar.set(Calendar.DAY_OF_WEEK,
						timeResource.getStartDay());
				endCalendar.set(Calendar.DAY_OF_WEEK, timeResource.getEndDay());
			}
		}

		if (timeResource.getStartHour() != EVERYTIME) {// 设置了小时的比较
			startCalendar
					.set(Calendar.HOUR_OF_DAY, timeResource.getStartHour());
			endCalendar.set(Calendar.HOUR_OF_DAY, timeResource.getEndHour());
		}
		if (timeResource.getStartMinute() != EVERYTIME) {// 设置了分的比较
			startCalendar.set(Calendar.MINUTE, timeResource.getStartMinute());
			endCalendar.set(Calendar.MINUTE, timeResource.getEndMinute());
		}
		if (timeResource.getStartSecond() != EVERYTIME) {// 设置了秒的比较
			startCalendar.set(Calendar.SECOND, timeResource.getStartSecond());
			endCalendar.set(Calendar.SECOND, timeResource.getEndSecond());
		}

		long start = startCalendar.getTimeInMillis();
		long end = endCalendar.getTimeInMillis();

		// System.out.println("[---------------------------------");
		// System.out.println(new java.util.Date(start));
		// System.out.println(new java.util.Date(end));
		// System.out.println(new java.util.Date(time));
		// System.out.println("---------------------------------]");
		if (time >= start && time <= end)
			return true;
		else
			return false;
	}

	public static void printPortResource(PortResource pr, StringBuffer sb) {
		if (pr == null) {
			System.out.println(pr);
			return;
		}
		if (pr.getLogicFlag() == 1)
			sb.append("!");
		if (pr.getSegments() == null) {
			sb.append(pr.getStartPort());
			if (pr.getEndPort() != -1) {
				sb.append(":" + pr.getEndPort());
			}
		} else {
			sb.append("(");
			printPortResource(pr.getSegments().get(0), sb);
			for (int i = 1; i < pr.getSegments().size(); i++) {
				sb.append(",");
				printPortResource(pr.getSegments().get(i), sb);
			}
			sb.append(")");
		}
	}

	public static void printIPResource(IPResource pr, StringBuffer sb) {
		if (pr == null) {
			System.out.println(pr);
			return;
		}
		if (pr.getLogicFlag() == 1)
			sb.append("!");
		if (pr.getSegments() == null) {
			sb.append("\"");
			sb.append(pr.getStartIP());
			if (pr.getEndIP() != null) {
				sb.append(":" + pr.getEndIP());
			}
			if (pr.getIpMask() != -1) {
				sb.append("/" + pr.getIpMask());
			}
			sb.append("\"");
		} else {
			sb.append("(");
			printIPResource(pr.getSegments().get(0), sb);
			for (int i = 1; i < pr.getSegments().size(); i++) {
				sb.append(",");
				printIPResource(pr.getSegments().get(i), sb);
			}
			sb.append(")");
		}
	}

	public static void printTimeResource(TimeResource pr, StringBuffer sb) {
		if (pr == null) {
			System.out.println(pr);
			return;
		}
		if (pr.getLogicFlag() == 1)
			sb.append("!");
		if (pr.getSegments() == null) {
			if (pr.getTimeFlag() == 0) {
				sb.append("D:");
				sb.append(" Y ");
				sb.append(pr.getStartYear());
				sb.append(" M ");
				sb.append(pr.getStartMonth());
				sb.append(" D ");
				sb.append(pr.getStartDay());
				sb.append(' ');
				sb.append(pr.getStartHour());
				sb.append(':');
				sb.append(pr.getStartMinute());
				sb.append(':');
				sb.append(pr.getStartSecond());
				sb.append(" - ");
				sb.append(" Y ");
				sb.append(pr.getEndYear());
				sb.append(" M ");
				sb.append(pr.getEndMonth());
				sb.append(" D ");
				sb.append(pr.getEndDay());
				sb.append(' ');
				sb.append(pr.getEndHour());
				sb.append(':');
				sb.append(pr.getEndMinute());
				sb.append(':');
				sb.append(pr.getEndSecond());

				// 'D' ':' 'Y' NUM 'M' NUM 'D' NUM NUM ':' NUM ':' NUM '-' 'Y'
				// NUM 'M' NUM 'D' NUM NUM ':' NUM ':' NUM

			} else {
				sb.append("W:");
				sb.append(" Y ");
				sb.append(pr.getStartYear());
				sb.append(" M ");
				sb.append(pr.getStartMonth());
				sb.append(" W ");
				sb.append(pr.getStartWeek());
				sb.append(" D ");
				sb.append(pr.getStartDay());
				sb.append(' ');
				sb.append(pr.getStartHour());
				sb.append(':');
				sb.append(pr.getStartMinute());
				sb.append(':');
				sb.append(pr.getStartSecond());
				sb.append(" - ");
				sb.append(" Y ");
				sb.append(pr.getEndYear());
				sb.append(" M ");
				sb.append(pr.getEndMonth());
				sb.append(" W ");
				sb.append(pr.getEndWeek());
				sb.append(" D ");
				sb.append(pr.getEndDay());
				sb.append(' ');
				sb.append(pr.getEndHour());
				sb.append(':');
				sb.append(pr.getEndMinute());
				sb.append(':');
				sb.append(pr.getEndSecond());
			}
		} else {
			sb.append("(");
			printTimeResource(pr.getSegments().get(0), sb);
			for (int i = 1; i < pr.getSegments().size(); i++) {
				sb.append(",");
				printTimeResource(pr.getSegments().get(i), sb);
			}
			sb.append(")");
		}
	}

	public static void main(String[] args) {

		TimeParser p = new TimeParser(false);
		try {
			p.parse(new FileReader("test.txt"));
			StringBuffer result = new StringBuffer();
			printTimeResource(p.getTimeResource(), result);
			System.out.println(result);

			p.parse(new StringReader(result.toString() + "\n"));
			result = new StringBuffer();
			printTimeResource(p.getTimeResource(), result);
			System.out.println(result);
			// System.out.println(testPortResurce(p.getPortResource(),100));
			// walkSyntaxTree(p.syntaxTree(new FileReader("test.txt")),5);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(IPTools.ipToLong("10.1.1.1"));
		// System.out.println(IPTools.ipToLong("192.1.1.1"));
		// System.out.println(IPTools.ipToLong("222.1.1.2"));
		// System.out.println(IPTools.ipToLong("222.1.1.3"));

	}

}
