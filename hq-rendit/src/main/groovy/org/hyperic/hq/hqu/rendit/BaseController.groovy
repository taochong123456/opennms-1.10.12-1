/**
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 *  "derived work".
 *
 *  Copyright (C) [2009-2010], VMware, Inc.
 *  This file is part of HQ.
 *
 *  HQ is free software; you can redistribute it and/or modify
 *  it under the terms version 2 of the GNU General Public License as
 *  published by the Free Software Foundation. This program is distributed
 *  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *
 */

package org.hyperic.hq.hqu.rendit

import java.text.SimpleDateFormat

import org.hyperic.hq.authz.server.session.AuthzSubject
import org.hyperic.hq.authz.server.session.Resource
import org.hyperic.hq.authz.shared.ResourceManager;
import org.hyperic.hq.context.Bootstrap;
import org.hyperic.hq.authz.server.session.ResourceManagerImpl
import org.hyperic.hq.appdef.shared.AppdefEntityID
import org.hyperic.hq.hqu.shared.UIPluginManager;
import org.hyperic.hq.hqu.rendit.html.FormGenerator
import org.hyperic.hq.hqu.rendit.html.HtmlUtil
import org.hyperic.hq.hqu.rendit.helpers.AgentHelper
import org.hyperic.hq.hqu.rendit.helpers.AlertHelper
import org.hyperic.hq.hqu.rendit.helpers.AuditHelper
import org.hyperic.hq.hqu.rendit.helpers.EscalationHelper
import org.hyperic.hq.hqu.rendit.helpers.ResourceHelper
import org.hyperic.hq.hqu.rendit.helpers.MetricHelper
import org.hyperic.hq.hqu.rendit.helpers.UserHelper
import org.hyperic.hq.hqu.rendit.helpers.AutodiscoveryHelper
import org.hyperic.hq.hqu.rendit.helpers.RoleHelper

import org.hyperic.hq.hqu.rendit.RequestInvocationBindings;
import org.hyperic.hq.hqu.rendit.render.RenderFrame


import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.FileItemStream


import org.hyperic.hq.hqu.server.session.Attachment

import org.json.JSONObject

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder	    

/**
 * The base controller is invoked by the dispatcher when it detects that
 * a controller method is being requested.
 */
abstract class BaseController { 
    Log log = LogFactory.getLog(this.getClass()) 
    
    String             action          // Current action being executed
    File               pluginDir       // Directory of plugin containing us
    String             controllerName  // Name of the controller
    HQUPlugin          plugin          
    String             template        // Default template when rendering
     
    private beforeFilters = []         // Closures to run prior to any actions
    private RequestInvocationBindings invokeArgs  // Info about the request
    private File    viewDir            // Path to plugin/app/views
    private boolean rendered           // Have we already performed a render?
    private         localeBundle = [:] // l10n bundle, must support getAt()
    private         jsonMethods = []   /* Names of methods which will 
                                          automatically encode JSON */
    private         xmlMethods= []     /* Names of methods going to XML */

    /**
     * Called immediately after the controller is fully constructed.
     *
     * This method should be over-written to setup instance variables.
     * Usage of the constructor in controllers is not recommended and
     * soon deprecated.
     */
    protected void init() {
    }
    
    /**
     * Add a closure that will be executed prior to any controller action 
     * methods.  If the filter returns true, then filter-execution will stop
     * and the request will be aborted.
     */
    protected void addBeforeFilter(Closure filter) {
        beforeFilters << filter
    }
    
    /**
     * Set a list of methods which are set to return a Map which will be
     * translated to JSON and sent via 'text/json-comment-filtered'.
     *
     * @param meths  A list of strings: the names of methods which will
     *               return Maps to translate to JSON Objects
     */
    protected void setJSONMethods(List meths) {
        jsonMethods = meths
    }
    
    protected void setXMLMethods(List meths) {
        xmlMethods = meths
    }

    /**
     * Controllers can use this method to force rendering to not occur.  This
     * is useful for controller methods that don't necessarily want to send
     * data to the client.
     */
    protected void setRendered(boolean isRendered) {
        rendered = isRendered
    }

    protected boolean getDumpScripts() {
        false
    }
    
    /**
     * Repeatedly calls the passed closure with a {@link FileItemStream} 
     * containing upload data.
     */
     protected void eachUpload(Closure yield) {
         ServletFileUpload upload = new ServletFileUpload()
         FileItemIterator iter = upload.getItemIterator(invokeArgs.request)
         while (iter.hasNext()) {
             FileItemStream item = iter.next()
    
             yield(item)
         }
    }
         
    protected String getUpload(argName) {
        def res
        eachUpload() { upload ->
            if (upload.fieldName == argName)
                res = upload.openStream().getText("utf-8")
        }
        res
    }

    def getLocaleBundle() {
        localeBundle
    }
    
    void setLocaleBundle(newBundle) {
        this.localeBundle = newBundle
    }
    
    void setControllerName(String name) {
        this.controllerName = name
    }
    
    protected setInvokeArgs(args) {
        this.invokeArgs = args
    }
    
    def getInvokeArgs() { invokeArgs }
    
    void setPluginDir(File pluginDir) {
        this.pluginDir = pluginDir
        this.viewDir   = new File(pluginDir, "views")
    }
    
    File getViewDir() {
        this.viewDir
    }

    boolean logRequests() {
        true
    }

    /**
     * Called by the dispatcher when a controller action is dispatched.
     *
     * If the execution of the action does not explicitly perform any 
     * rendering, the view of the current action will be displayed.
     */
    def dispatchRequest() {
	    def start  = System.currentTimeMillis()
		def params = invokeArgs.request.parameterMap
		if (log.debugEnabled) {
		    log.debug "Dispatching Request: action=${action} params=${params}"
		}

	    rendered = false

	    def lowestClass = this.metaClass.theClass
	    def meth = this.metaClass.methods.find {n -> 
            // XXX:  Comparing the classes directly seems not to work, so 
	        //       comparing the names for now.
	        n.name == action && n.declaringClass.name == lowestClass.name 
	    }
	    
	    if (!meth) {
	        log.error "404!   Action [${this.class.name}.${action}] not found"
	        invokeArgs.response.sendError(404)
	        return
	    }
	    
	    if (logRequests()) {
	       log.info "Invoking method: ${action} with ${params}"
	    } else if (log.debugEnabled) {
	       log.debug "Invoking method: ${action} with ${params}"
	    }
	    
	    try {
	        for (f in beforeFilters) {
	        	if (f(params))
	        	    return;
	        }
	        
	        def methRes
	        def xmlWriter
	        def xmlBuilder
			if (action in xmlMethods) {
			    xmlWriter  = new StringWriter()
			    xmlBuilder = new MarkupBuilder(xmlWriter) 
			    methRes    = invokeMethod(action, [xmlBuilder] + params)
			} else {
			    methRes = invokeMethod(action, params)
			}
			    
		    if (action in jsonMethods) {
		        def json = methRes as JSONObject
				render(inline:"/* ${json} */", 
				       contentType:'text/json-comment-filtered')
		    } else if (action in xmlMethods) {
		        if (xmlBuilder == methRes) 
		            render(inline:"${xmlWriter}", contentType:'text/xml')
		        else
		            render(inline:"${methRes}", contentType:'text/xml')
		    }
		    
    		if (!rendered) {
    		    render([action : action])
	        }
	    } finally {
	        if (log.debugEnabled) {
    		    log.debug "Executed $controllerName:$action in " +   
    	        	      "${System.currentTimeMillis() - start} ms"
	        }
	    }
    }
    
    /**
     * Specifies additional methods available to rendered scripts.  
     * Controllers may override this method if they want to add additional
     * commands (but will likely also want to merge it with the results
     * from this base method)
     */
    def getBaseRenderLocals() {
		[h           : HtmlUtil.&escapeHtml,
		 format_date : this.&formatDate]
    }
    
    String formatDate(String fmt, Date d) {
        SimpleDateFormat df = new SimpleDateFormat(fmt, locale) 
        df.format(d)
    }
    
    Locale getLocale() {
        invokeArgs.request.locale
    }
    
	protected void redirectTo(opts) {
	    def response = invokeArgs.response

	    rendered = true
	    def targetUrl = response.encodeRedirectURL(HtmlUtil.urlFor(opts))
		response.sendRedirect(targetUrl)
	}
    
	/**
	 * Render XML to the remote client in a streamed fashion.  
	 * The opts map is able to specify both the encoding and contentType
	 * of the target stream.
	 * 
	 * This method cannot be used in conjuction with other render() methods
	 * which output content directly to the client, as this method sets the
	 * response headers for content type.
	 * 
	 * For example:
	 * 
	 *     renderXml() {
	 *         userList() {
	 *             user(name: 'Joe', id: 34)
	 *             user(name: 'Bob', id: 35)
	 *         }
	 *     }
	 *     
	 * Or:
	 *     renderXml(encoding:'utf-8', contentType:'x-fancyXml') {
	 *         properties() {
	 *             prop(name: 'maxMem', value:'1024m')
	 *         }
	 *     }
	 */
	protected void renderXml(Map opts = [encoding:"utf-8",
	                                     contentType:'text/xml'], 
	                         Closure c) 
	{
	    rendered = true
        invokeArgs.response.setContentType(opts.contentType)
        StreamingMarkupBuilder b = new StreamingMarkupBuilder()
        b.encoding = opts.encoding
        b.bind { mkp.xmlDeclaration()
                 out << c
        }.writeTo(invokeArgs.response.writer)
	}
	
    protected void render(opts) {
        opts = (opts == null) ? [:] : opts

        rendered = true
        opts['createDefaultOutput'] = {
            def outStream = invokeArgs.response.outputStream
            new OutputStreamWriter(outStream, "UTF-8")
        }
        opts['setContentType'] = { contentType ->
            invokeArgs.response.setContentType(contentType)
        }
        def locals    = opts.get('locals', [:])
        def newLocals = new HashMap(baseRenderLocals)
        newLocals.putAll(locals)
        opts = new HashMap(opts) // Clone
        opts['locals'] = newLocals
        if (!opts['template'])
            opts['template'] = template
        if (!opts['partialDir'])
            opts['partialDir'] = new File(viewDir, controllerName) 
                
        new RenderFrame(opts, this).render()
    }

    /**
     * This urlFor can be used to take the current request context into 
     * account when rendering URLs relative to the current request.
     *
     * See also:  HtmlUtil.urlFor
     */
    public String urlFor(opts) {
        def req = invokeArgs.request
        def path = [invokeArgs.contextPath]
		def encode = false
		
        if (!opts.resource && !opts.asset) {
            path += invokeArgs.servletPath.split('/') as List
            // Trim off the last element
            if (path[-1].endsWith('.hqu'))
                path = path[0..-2]
        } else if(opts.asset) {
            path += invokeArgs.servletPath.split('/') as List
            if (path[-1].endsWith('.hqu'))
                path = path[0..-3]
        }
        
		if (opts['encodeUrl'] != null) {
			encode = opts['encodeUrl']
			
			opts.remove('encodeUrl')	
		}
		
        path = path.findAll{it}.join('/')
		
		if (opts['absolute'] == null) {
			opts += [absolute:path]
		}
		
		def url = HtmlUtil.urlFor(opts)
		
		if (encode) {
			url = invokeArgs.response.encodeURL(url)
		}
		
		url
    }
    
    public String buttonTo(opts) {
        HtmlUtil.buttonTo(opts + [urlFor:this.&urlFor])
    }
    
    public String linkTo(text, opts) {
        HtmlUtil.linkTo(text, opts + [urlFor:this.&urlFor])
    }
    
    protected AuthzSubject getUser() {
        invokeArgs.user 
    }

    private agentHelperInternal = null
    protected AgentHelper getAgentHelper() {
        if (agentHelperInternal == null) {
            agentHelperInternal = new AgentHelper(user)
        }
        agentHelperInternal
    }

    private alertHelperInternal = null
    protected AlertHelper getAlertHelper() {
        if (alertHelperInternal == null) {
            alertHelperInternal = new AlertHelper(user)
        }
        alertHelperInternal
    }

    private auditHelperInternal = null
    protected AuditHelper getAuditHelper() {
        if (auditHelperInternal == null) {
            auditHelperInternal = new AuditHelper(user)
        }
        auditHelperInternal
    }

    private resourceHelperInternal = null
    protected ResourceHelper getResourceHelper() {
        if (resourceHelperInternal == null) {
            resourceHelperInternal = new ResourceHelper(user)
        }
        resourceHelperInternal
    }

    private metricHelperInternal = null
    protected MetricHelper getMetricHelper() {
        if (metricHelperInternal == null) {
            metricHelperInternal = new MetricHelper(user)
        }
        metricHelperInternal
    }

    private userHelperInternal = null
    protected UserHelper getUserHelper() {
        if (userHelperInternal == null) {
            userHelperInternal = new UserHelper(user)
        }
        userHelperInternal
    }

    private escalationHelperInternal = null
    protected EscalationHelper getEscalationHelper() {
        if (escalationHelperInternal == null) {
            escalationHelperInternal = new EscalationHelper(user)
        }
        escalationHelperInternal
    }

    private autodiscoveryHelperInternal = null
    protected AutodiscoveryHelper getAutodiscoveryHelper() {
        if (autodiscoveryHelperInternal == null) {
            autodiscoveryHelperInternal = new AutodiscoveryHelper(user)
        }
        autodiscoveryHelperInternal
    }

    private roleHelperInternal = null
    protected RoleHelper getRoleHelper() {
        if (roleHelperInternal == null) {
            roleHelperInternal = new RoleHelper(user)
        }
        roleHelperInternal
    }

    /**
     * Get the attachment point for the view associated with the current 
     * request.
     */
    protected Attachment getViewAttachment() {
        def uiMan = Bootstrap.getBean(UIPluginManager.class)
        def attachId = invokeArgs.request.parameterMap.getOne('attachId').toInteger()
        uiMan.findAttachmentById(attachId)
    }
    
    /**
     * Get the resource that is currently being viewed.  
     */
    protected Resource getViewedResource() {
        def aeid = invokeArgs.request.parameterMap.getOne('eid')
        if (aeid == null) {
            return null
        }
        
        def id = new AppdefEntityID(aeid)
        Bootstrap.getBean(ResourceManager.class).findResource(id)
    }

    /**
     * Adds a filter which only allows calls made by users in the Super User
     * role to be processed.
     */
    protected onlyAllowSuperUsers() {
       // addBeforeFilter({ 
         //   if (!user.isSuperUser()) {
           //     render(inline: "Unauthorized")
             //   return true
            //}
            //return false
        //})
        return false
    }
    
    /**
     * Get the current time
     */
    protected long now() {
        System.currentTimeMillis()
    }
}
