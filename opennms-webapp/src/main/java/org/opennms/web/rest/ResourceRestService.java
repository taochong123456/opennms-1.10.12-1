package org.opennms.web.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.rest.support.ResourceDTO;
import org.opennms.web.rest.support.ResourceDTOCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Read-only API for retrieving resources.
 *
 * @author jwhite
 */
@Component("resourceRestService")
@Path("resources")
public class ResourceRestService extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTOCollection getResources(@DefaultValue("1") @QueryParam("depth") final int depth) {
        List<ResourceDTO> resources = Lists.newLinkedList();
        for (OnmsResource resource : m_resourceDao.findTopLevelResources()) {
            resources.add(ResourceDTO.fromResource(resource, depth));
        }
        return new ResourceDTOCollection(resources);
    }

    @GET
    @Path("{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getResourceById(@PathParam("resourceId") final String resourceId,
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
    	String resourceIdStr = null;
		try {
			resourceIdStr = URLDecoder.decode(resourceId,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        OnmsResource resource = m_resourceDao.getResourceById(resourceIdStr);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }

        return ResourceDTO.fromResource(resource, depth);
    }

    @DELETE
    @Path("{resourceId}")
    @Transactional(readOnly=false)
    public void deleteResourceById(@PathParam("resourceId") final String resourceId) {
        /*final boolean found = m_resourceDao.deleteResourceById(ResourceId.fromString(resourceId));

        if (!found) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }*/
    }


    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getResourceForNode(@PathParam("nodeCriteria") final String nodeCriteria,
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "No node found with criteria '{}'.", nodeCriteria);
        }

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource found for node with id {}.", "" + node.getId());
        }

        return ResourceDTO.fromResource(resource, depth);
    }
}
