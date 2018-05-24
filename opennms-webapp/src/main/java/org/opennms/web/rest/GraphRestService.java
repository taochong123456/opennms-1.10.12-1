package org.opennms.web.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.rest.support.GraphNameCollection;
import org.opennms.web.rest.support.PrefabGraphCollection;
import org.opennms.web.rest.support.ResourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Read-only API for retrieving graph definitions and determining
 * the list of supported graphs on a particular resource.
 *
 * @author jwhite
 */
@Component("graphRestService")
@Path("graphs")
public class GraphRestService extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private GraphDao m_graphDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public GraphNameCollection getGraphNames() {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getAllPrefabGraphs()) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("{graphName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public PrefabGraph getGraphByName(@PathParam("graphName") final String graphName) {
        try {
            return m_graphDao.getPrefabGraph(graphName);
        } catch (ObjectRetrievalFailureException e) {
        	return null;
        }
		
    }

    @GET
    @Path("for/{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public GraphNameCollection getGraphNamesForResource(@PathParam("resourceId") final String resourceId) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        if (resource == null) {
        }
        return getGraphNamesForResource(resource);
    }

    private GraphNameCollection getGraphNamesForResource(final OnmsResource resource) {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getPrefabGraphsForResource(resource)) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getGraphResourcesForNode(@PathParam("nodeCriteria") final String nodeCriteria, @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
           return null;
        }

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        if (resource == null) {
        	return null;
        }

        final ResourceVisitor visitor = new ResourceVisitor(this);
        final ResourceDTO resourceDTO = ResourceDTO.fromResource(resource, depth);
        visitor.visit(resourceDTO);

        return resourceDTO;
    }
    
    

    @XmlRootElement(name = "graph-resource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class GraphResourceDTO {
        @XmlElement(name="resource")
        private ResourceDTO m_resource;

        @XmlElement(name="prefab-graphs")
        private PrefabGraphCollection m_prefabGraphs;

        @SuppressWarnings("unused")
        private GraphResourceDTO() {
            super();
        }

        public GraphResourceDTO(final ResourceDTO resource, final PrefabGraphCollection graphs) {
            m_resource = resource;
            m_prefabGraphs = graphs;
        }
    }

    public static final class ResourceVisitor {
        private final Map<String,PrefabGraph> m_graphs = Maps.newLinkedHashMap();
        private final GraphRestService m_service;

        public ResourceVisitor(GraphRestService service) {
            m_service = service;
        }

        public void visit(final ResourceDTO resource) {
            // first, decorate the DTO with the list of graph names
            final GraphNameCollection graphNames = m_service.getGraphNamesForResource(resource.getResource());
            resource.setGraphNames(graphNames.getObjects());

            // then, get the prefab graphs for these graph names if we don't have them already
            for (final String graphName : graphNames) {
                if (!m_graphs.containsKey(graphName)) {
                    m_graphs.put(graphName, m_service.getGraphByName(graphName));
                }
            }

            // finally, recurse if necessary
            if (resource.getChildren() != null) {
                for (final ResourceDTO r : resource.getChildren()) {
                    this.visit(r);
                }
            }
        }

        public PrefabGraphCollection getGraphs() {
            return new PrefabGraphCollection(m_graphs.values());
        }
    }
}
