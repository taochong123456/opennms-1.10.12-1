package org.opennms.netmgt.syslogd.analyze;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventFieldValueCheck implements EventAction {

	@Override
	public void free() {

	}

	@Override
	public String getName() {
		return null;
	}

	private boolean isIP(String ip) {

		return false;
	}

	private boolean isMac(String mac) {
		return true;
	}

	@Override
	public int handle(SIMEventObject event) {

		event.sysType = event.sysType & 0Xff;
		event.collectType = event.collectType & 0xff;
		if (!event.collectorAddr.trim().equals("")
				&& !isIP(event.collectorAddr)) {
			event.collectorAddr = "error";
		}
		if (event.collectorName.length() > 50)
			event.collectorName = event.collectorName.substring(0, 45) + "...";
		if (event.category != null)
			event.category = event.category & 0xff;
		if (event.type != null)
			event.type = event.type.intValue() & 0xffff;

		if (event.priority != null)
			event.priority = event.priority & 0Xff;
		if (event.protocol != null)
			event.protocol = event.protocol.intValue() & 0xffff;
		if (event.appProtocol != null)
			event.appProtocol = event.appProtocol.intValue() & 0xffff;
		if (event.object != null)
			event.object = event.object.intValue() & 0xffff;
		if (event.policy != null)
			event.policy = event.policy.intValue() & 0xffff;
		if (event.resource.length() > 255)
			event.resource = event.resource.substring(0, 250) + "...";
		if (event.action != null)
			event.action = event.action.intValue() & 0Xff;

		if (event.intent.length() > 255) {
			event.intent = event.intent.substring(0, 250) + "...";
		}
		if (event.result != null)
			event.result = event.result.intValue() & 0Xff;

		if (!event.sAddr.trim().equals("") && !isIP(event.sAddr)) {
			event.sAddr = "error";
		}
		if (event.sPort != null)
			event.sPort = event.sPort.intValue() & 0xffff;

		if (event.sUserName.length() > 50) {
			event.sUserName = event.sUserName.substring(0, 45) + "...";
		}

		if (!event.sMAC.trim().equals("") && !isMac(event.sMAC)) {
			event.sMAC = "error";
		}

		if (!event.stAddr.trim().equals("") && !isIP(event.stAddr)) {
			event.stAddr = "error";
		}
		if (event.stPort != null)
			event.stPort = event.stPort.intValue() & 0xffff;

		if (!event.dAddr.trim().equals("") && !isIP(event.dAddr)) {
			event.dAddr = "error";
		}
		if (event.dPort != null)
			event.dPort = event.dPort.intValue() & 0xffff;

		if (event.dUserName.length() > 50) {
			event.dUserName = event.dUserName.substring(0, 45) + "...";
		}
		if (!event.dMAC.trim().equals("") && !isMac(event.dMAC)) {
			event.dMAC = "error";
		}
		if (!event.dtAddr.trim().equals("") && !isIP(event.dtAddr)) {
			event.dtAddr = "error";
		}
		if (event.dtPort != null)
			event.dtPort = event.dtPort.intValue() & 0xffff;
		if (!event.devAddr.trim().equals("") && !isIP(event.devAddr)) {
			event.devAddr = "error";
		}

		if (event.devName.length() > 50) {
			event.devName = event.devName.substring(0, 45) + "...";
		}
		if (event.devCategory != null)
			event.devCategory = event.devCategory.intValue() & 0xffff;
		if (event.devType != null)
			event.devType = event.devType.intValue() & 0xffff;

		if (event.devVendor.length() > 50) {
			event.devVendor = event.devVendor.substring(0, 45) + "...";
		}

		if (event.devProduct.length() > 50) {
			event.devProduct = event.devProduct.substring(0, 45) + "...";
		}

		if (event.programType.length() > 50) {
			event.programType = event.programType.substring(0, 45) + "...";
		}

		if (event.customS1.length() > 255) {
			event.customS1 = event.customS1.substring(0, 250) + "...";
		}

		if (event.customS2.length() > 255) {
			event.customS2 = event.customS2.substring(0, 250) + "...";
		}

		if (event.customS3.length() > 255) {
			event.customS3 = event.customS3.substring(0, 250) + "...";
		}

		if (event.customS4.length() > 255) {
			event.customS4 = event.customS4.substring(0, 250) + "...";
		}
		if (event.name.length() > 255) {
			event.name = event.name.substring(0, 250) + "...";
		}

		return 0;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public void setName(String name) {

	}

}
