package org.opennms.web.rest;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("infoRestService")
@Path("info")
@Transactional
public class InfoRestService extends OnmsRestService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo() throws ParseException {
    	//FIXME need to get from installer.properties
//        final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();
        final Map<String,String> info = new TreeMap<String,String>();
        info.put("displayVersion", "1.10.12");
        info.put("version", "1.10.12");
        info.put("packageName", "APM");
        info.put("packageDescription", "APM");
        final JSONObject jo = new JSONObject(info);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }
}
