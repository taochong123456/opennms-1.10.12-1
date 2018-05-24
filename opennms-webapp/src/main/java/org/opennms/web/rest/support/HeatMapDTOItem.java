package org.opennms.web.rest.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HeatMapDTOItem {
    private String id;
    private Integer elementId;
    private List<Double> color = new ArrayList<Double>();
    private List<Double> size = new ArrayList<Double>();

    public HeatMapDTOItem() {
    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "elementId")
    public Integer getElementId() {
        return elementId;
    }

    public void setElementId(Integer elementId) {
        this.elementId = elementId;
    }

    @XmlElement(name = "color")
    public List<Double> getColor() {
        return color;
    }

    public void setColor(List<Double> color) {
        this.color = color;
    }

    @XmlElement(name = "size")
    public List<Double> getSize() {
        return size;
    }

    public void setSize(List<Double> size) {
        this.size = size;
    }
}