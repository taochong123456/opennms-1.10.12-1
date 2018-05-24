package org.opennms.web.rest.support;



import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HeatMapDTOCollection {
    private List<HeatMapDTOItem> heatMapDTOItems = new ArrayList<HeatMapDTOItem>();

    public HeatMapDTOCollection() {
    }

    public void setHeatMapDTOItems(List<HeatMapDTOItem> heatMapDTOItems) {
        this.heatMapDTOItems = heatMapDTOItems;
    }
    //TODO transform to json not right
    @XmlElement(name = "children")
    public List<HeatMapDTOItem> getHeatMapDTOItems() {
        return heatMapDTOItems;
    }
}