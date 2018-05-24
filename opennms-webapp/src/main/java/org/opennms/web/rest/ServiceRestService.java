package org.opennms.web.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for service entity
 *
 * @author Chong
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("services")
@Transactional
public class ServiceRestService  extends OnmsRestService{
	
	@Autowired
	private MonitoredServiceDao monitorServiceDao;
	
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("{serviceParam}")
    public OnmsMonitoredServiceList getServices(@PathParam("serviceParam") final String serviceParam) {
        readLock();
        try {
			List<OnmsMonitoredService> monitorServices = monitorServiceDao.findByType(serviceParam);
			OnmsMonitoredServiceList monitorServiceList = new OnmsMonitoredServiceList(monitorServices);
            return monitorServiceList;
		} catch (Exception e) {
            throw getException(Status.BAD_REQUEST, e.getMessage());
		}finally{
			readUnlock();
		}
       
    }
}
