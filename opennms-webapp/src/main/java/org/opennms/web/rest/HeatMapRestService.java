package org.opennms.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.web.rest.support.HeatMapDTOCollection;
import org.opennms.web.rest.support.HeatMapDTOItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component("heatMapRestService")
@Path("heatmap")
public class HeatMapRestService extends OnmsRestService {
    /**
     * Property and default value for category filtering
     */
    private static final String CATEGORY_FILTER_PROPERTY_KEY = "org.opennms.heatmap.categoryFilter";
    private static final String CATEGORY_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for foreign source filtering
     */
    private static final String FOREIGNSOURCE_FILTER_PROPERTY_KEY = "org.opennms.heatmap.foreignSourceFilter";
    private static final String FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for service filtering
     */
    private static final String SERVICE_FILTER_PROPERTY_KEY = "org.opennms.heatmap.serviceFilter";
    private static final String SERVICE_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for handling only unacknowledged alarms
     */
    private static final String ONLY_UNACKNOWLEDGED_PROPERTY_KEY = "org.opennms.heatmap.onlyUnacknowledged";
    private static final String ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT = "false";

    private static final Logger LOG = LoggerFactory.getLogger(HeatMapRestService.class);

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private AlarmDao m_alarmDao;

    /**
     * Transforms a list of heatmap elements to a json map.
     *
     * @param heatMapElements the list of heatmap elements
     * @return the map for the json response
     */
    private HeatMapDTOCollection transformResults(List<HeatMapElement> heatMapElements, String filter) {
        /**
         * the item list
         */
        final List<HeatMapDTOItem> itemList = new ArrayList<HeatMapDTOItem>();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> elementSizes = new HashMap<String, Integer>();

        /**
         * counter for total of services
         */
        int totalServices = 0;

        /**
         * iterate over the heatmap elements and increase the total
         * number of services. This is later used to compute the sizes
         * for the heatmap boxes...
         */
        for (HeatMapElement heatMapElement : heatMapElements) {
            if (heatMapElement.getServicesTotal() > 0) {
                /**
                 * Apply filter here if not null
                 */
                if (filter == null || heatMapElement.getName().matches(filter)) {
                    elementSizes.put(heatMapElement.getName(), heatMapElement.getServicesTotal());

                    HeatMapDTOItem heatMapDTOItem = new HeatMapDTOItem();
                    heatMapDTOItem.setId(heatMapElement.getName());
                    heatMapDTOItem.setElementId(heatMapElement.getId());
                    heatMapDTOItem.setColor(Lists.newArrayList(heatMapElement.getColor()));
                    itemList.add(heatMapDTOItem);

                    totalServices += heatMapElement.getServicesTotal();
                }
            }
        }

        /**
         * now iterate over the results and set the size attribute for
         * each entry...
         */
        for (HeatMapDTOItem heatMapDTOItem : itemList) {
            int servicesInEntity = elementSizes.get(heatMapDTOItem.getId());
            double size = (double) servicesInEntity / (double) totalServices;
            heatMapDTOItem.setSize(Lists.newArrayList(Double.valueOf(size)));
        }

        /**
         * create the "outer" map and add the list to it...
         */
        HeatMapDTOCollection heatMapDTOCollection = new HeatMapDTOCollection();
        heatMapDTOCollection.setHeatMapDTOItems(itemList);

        return heatMapDTOCollection;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/categories")
    public Response outagesByCategories() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(CATEGORY_FILTER_PROPERTY_KEY, CATEGORY_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/foreignSources")
    public Response outagesByForeignsources() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("foreignsource", "0", null, null, "foreignsource");
        return Response.ok(transformResults(heatMapElements, System.getProperty(FOREIGNSOURCE_FILTER_PROPERTY_KEY, FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/monitoredServices")
    public Response outagesByServices() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("service.servicename", "service.serviceid", null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(SERVICE_FILTER_PROPERTY_KEY, SERVICE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByCategory/{category}")
    public Response outagesOfNodesByCategory(@PathParam("category") final String category) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "categories.categoryname", category);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByForeignSource/{foreignSource}")
    public Response outagesOfNodesByForeignSource(@PathParam("foreignSource") final String foreignSource) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "foreignsource", foreignSource);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByMonitoredService/{monitoredService}")
    public Response outagesOfNodesByService(@PathParam("monitoredService") final String monitoredService) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "service.servicename", monitoredService);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/categories")
    public Response alarmsByCategories() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", processAcknowledged, null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(CATEGORY_FILTER_PROPERTY_KEY, CATEGORY_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/foreignSources")
    public Response alarmsByForeignsources() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("foreignsource", "0", processAcknowledged, null, null, "foreignsource");
        return Response.ok(transformResults(heatMapElements, System.getProperty(FOREIGNSOURCE_FILTER_PROPERTY_KEY, FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/monitoredServices")
    public Response alarmsByServices() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("service.servicename", "service.serviceid", processAcknowledged, null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(SERVICE_FILTER_PROPERTY_KEY, SERVICE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByCategory/{category}")
    public Response alarmsOfNodesByCategory(@PathParam("category") final String category) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "categories.categoryname", category);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByForeignSource/{foreignSource}")
    public Response alarmsOfNodesByForeignSource(@PathParam("foreignSource") final String foreignSource) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "foreignsource", foreignSource);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByMonitoredService/{monitoredService}")
    public Response alarmsOfNodesByService(@PathParam("monitoredService") final String monitoredService) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "service.servicename", monitoredService);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }
}
