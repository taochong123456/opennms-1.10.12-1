package org.opennms.web.rest.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MonitorMapDTOCollection {
	private List<ServerMapDTOItem> categoryDTOItems = new ArrayList<ServerMapDTOItem>();
    private String label;
	public void addServerItem(ServerMapDTOItem serverItem) {
		categoryDTOItems.add(serverItem);
	}

	@XmlElement(name = "server")
	public List<ServerMapDTOItem> getCategoryDTOItems() {
		return categoryDTOItems;
	}
	@XmlElement(name = "label")
	public String getLabel() {
		return label;
	}

	public void setCategoryDTOItems(List<ServerMapDTOItem> categoryDTOItems) {
		this.categoryDTOItems = categoryDTOItems;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}