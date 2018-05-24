package org.opennms.web.rest.support;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resources")
public final class ResourceDTOCollection extends JaxbListWrapper<ResourceDTO> {

    private static final long serialVersionUID = 1L;

    public ResourceDTOCollection() {
        super();
    }

    public ResourceDTOCollection(Collection<? extends ResourceDTO> resources) {
        super(resources);
    }

    @XmlElement(name = "resource")
    public List<ResourceDTO> getObjects() {
        return super.getObjects();
    }
}