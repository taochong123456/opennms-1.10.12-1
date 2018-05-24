package org.opennms.web.rest.support;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServerMapDTOItem {
	private String icon;
	private String name;
	private String label;
    private int count;
    
	public ServerMapDTOItem() {
	}

	@XmlElement(name = "icon")
	public String getIcon() {
		return icon;
	}

	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	@XmlElement(name = "label")
	public String getLabel() {
		return label;
	}

	@XmlElement(name = "count")
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

}