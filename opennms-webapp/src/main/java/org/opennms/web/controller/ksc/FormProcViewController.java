/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.ksc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.KscReportEditor;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>FormProcViewController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcViewController extends AbstractController implements InitializingBean {

    public enum Parameters {
        action,
        domain,
        timespan,
        type,
        report,
        graphtype
    }

    public enum Actions {
        Customize,
        Update,
        Exit
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get Form Variables
        int report_index = -1; 
        String override_timespan = null;
        String override_graphtype = null;
        String report_action = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.action.toString()));
        String domain = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.domain.toString()));
        if (report_action == null) {
            throw new MissingParameterException ("action", new String[] {"action", "report", "type"});
        }
        String report_type = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.type.toString()));
        if (report_type == null) {
            throw new MissingParameterException ("type", new String[] {"action", "report", "type"});
        }

        if (Actions.Customize.toString().equals(report_action) || Actions.Update.toString().equals(report_action)) {
            String r_index = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.report.toString()));
            if (r_index != null && !r_index.equals("null")) {
               report_index = WebSecurityUtils.safeParseInt(r_index); 
            } else if (domain == null) {
                throw new MissingParameterException("report or domain", new String[] {"report or domain" , "type"});
            }
            override_timespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.timespan.toString()));
            if ((override_timespan == null) || override_timespan.equals("null")) {
                override_timespan = "none";
            }
            override_graphtype = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphtype.toString()));
            if (override_graphtype == null || override_graphtype.equals("null")) {
                override_graphtype = "none";
            }
            if (Actions.Customize.toString().equals(report_action)) {
             // Fetch the KscReportEditor or create one if there isn't one already
                KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), false);
                
                if (report_type.equals("node")) {
                    editor.loadWorkingReport(m_kscReportService.buildNodeReport(report_index)); 
                } else if (report_type.equals("domain")) {
                    editor.loadWorkingReport(m_kscReportService.buildDomainReport(domain));
                } else { 
                    editor.loadWorkingReport(getKscReportFactory(), report_index);
                }
                
                // Now inject any override characteristics into the working report model
                Report working_report = editor.getWorkingReport();
                for (int i=0; i<working_report.getGraphCount(); i++) {
                    Graph working_graph = working_report.getGraph(i);
                    if (!override_timespan.equals("none")) { 
                        working_graph.setTimespan(override_timespan); 
                    }
                    if (!override_graphtype.equals("none")) { 
                        working_graph.setGraphtype(override_graphtype); 
                    }
                }
            }
        } else { 
            if (!Actions.Exit.toString().equals(report_action)) {
                throw new ServletException ("Invalid Parameter contents for report_action");
            }
        }
        
        if (Actions.Update.toString().equals(report_action)) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("type", report_type);

            if (report_index >= 0) {
                modelAndView.addObject("report", report_index);
            }
            if (domain != null) {
                modelAndView.addObject("domain", domain);
            }
            if (override_timespan != null) { 
                modelAndView.addObject("timespan", override_timespan);
            }
            if (override_graphtype != null) { 
                modelAndView.addObject("graphtype", override_graphtype);
            }

            return modelAndView;
        } else if (Actions.Customize.toString().equals(report_action)) { 
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (Actions.Exit.toString().equals(report_action)) {
            return new ModelAndView("redirect:/KSC/index.htm");
        } else {
            throw new IllegalArgumentException("Parameter action of '" + report_action + "' is not supported.  Must be one of: Update, Customize, or Exit");
        }
    }

    /**
     * <p>getKscReportFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    /**
     * <p>setKscReportFactory</p>
     *
     * @param kscReportFactory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }

    /**
     * <p>getKscReportService</p>
     *
     * @return a {@link org.opennms.web.svclayer.KscReportService} object.
     */
    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    /**
     * <p>setKscReportService</p>
     *
     * @param kscReportService a {@link org.opennms.web.svclayer.KscReportService} object.
     */
    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

   

}
