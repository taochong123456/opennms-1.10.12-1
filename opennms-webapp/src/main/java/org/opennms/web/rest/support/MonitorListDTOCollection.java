package org.opennms.web.rest.support;



import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MonitorListDTOCollection {
    private List<MonitorRowDTOCollection> monitorRowItems = new ArrayList<MonitorRowDTOCollection>();
	@XmlElement(name = "monitorRow")
	public List<MonitorRowDTOCollection> getMonitorRowItems() {
		return monitorRowItems;
	}

	public void setMonitorRowItems(List<MonitorRowDTOCollection> monitorRowItems) {
		this.monitorRowItems = monitorRowItems;
	}
	
	public void addMonitorRow(MonitorRowDTOCollection row){
		monitorRowItems.add(row);
	}
	

}