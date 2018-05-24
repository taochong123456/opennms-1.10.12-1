package org.opennms.web.rest.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MonitorRowDTOCollection {

	private List<MonitorMapDTOCollection> monitorDTOItems = new ArrayList<MonitorMapDTOCollection>();

	@XmlElement(name = "group")
	public List<MonitorMapDTOCollection> getMonitorDTOItems() {
		return monitorDTOItems;
	}

	public void setMonitorDTOItems(List<MonitorMapDTOCollection> monitorDTOItems) {
		this.monitorDTOItems = monitorDTOItems;
	}

	public void addMonitorItems(MonitorMapDTOCollection monitor) {
		monitorDTOItems.add(monitor);
	}
}