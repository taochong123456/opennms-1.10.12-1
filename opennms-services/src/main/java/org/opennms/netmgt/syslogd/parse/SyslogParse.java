package org.opennms.netmgt.syslogd.parse;
import java.text.*;
import java.util.*;
public class SyslogParse implements EventParse {
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MMM dd HH:mm:ss", Locale.US);
	private SimpleDateFormat dateFormat1 = new SimpleDateFormat(
			"MMM dd HH:mm:ss yyyy", Locale.US);
	private String collectorName = null;
	private String collectorAddr = null;
	private Calendar calendar = Calendar.getInstance();

	public SyslogParse() {
		collectorAddr = "127.0.0.1";
		collectorName = "localhost";
	}

	public static SyslogParse getInstance() {
		return new SyslogParse();
	}

	public String format(Date date) throws IllegalArgumentException {

		String rest = dateFormat.format(date);
		return rest;
	}

	public SIMEventObject parse(String message, String host) {
		int lbIdx = message.indexOf('<');
		int rbIdx = message.indexOf('>');

		if (lbIdx < 0 || rbIdx < 0 || lbIdx >= (rbIdx - 1)) {
			return parseRawSyslog(message, host);
		}

		int priCode = 0;
		String priStr = message.substring(lbIdx + 1, rbIdx);

		try {
			priCode = Integer.parseInt(priStr);
		} catch (NumberFormatException ex) {
			return parseRawSyslog(message, host);
		}

		int priority = 1;
		message = message.substring(rbIdx + 1, message.length());
		boolean stdMsg = true;
		if (message.length() < 16) {
			stdMsg = false;
		} else if (message.charAt(3) != ' ' || message.charAt(6) != ' '
				|| message.charAt(9) != ':' || message.charAt(12) != ':'
				|| message.charAt(15) != ' ') {
			stdMsg = false;
		}

		long timestamp = System.currentTimeMillis();
		long occurtime = 0;
		if (stdMsg) {
			try {
				occurtime = dateFormat1.parse(
						message.substring(0, 15) + " "
								+ calendar.get(Calendar.YEAR)).getTime();
			} catch (ParseException ex) {
			}
			message = message.substring(16);
		}
		String processName = "";
		SIMEventObject logOb = new SIMEventObject();
		logOb.ID = 0;
		if (occurtime > 0)
			logOb.occurTime = occurtime;
		else
			logOb.occurTime = timestamp;
		logOb.msg = message;
		logOb.devAddr = host;
		logOb.sysType = 3;
		logOb.collectType = 1;
		logOb.collectorAddr = collectorAddr;
		logOb.collectorName = collectorName;
		return logOb;
	}

	private SIMEventObject parseRawSyslog(String message, String host) {
		long timestamp = System.currentTimeMillis();
		SIMEventObject logOb = new SIMEventObject();
		logOb.ID = 0;
		logOb.occurTime = timestamp;
		logOb.msg = message;
		logOb.devAddr = host;
		logOb.sysType = 3;
		logOb.collectType = 1;
		logOb.collectorAddr = collectorAddr;
		logOb.collectorName = collectorName;
		return logOb;
	}
}
