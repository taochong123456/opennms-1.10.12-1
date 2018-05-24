/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006, 2007], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.ui;

/**
 *
 * Attribute Constants
 */
public interface AttrConstants {
    /**
     * the portal that contains the useres dashboard settings.
     * this is placed in the session for scalability reasons.
     * 
     */
    public static final String USERS_SES_PORTAL= "myPortal";    
    
    //---------------------------------------session attributes

    /**
     * The session scope attribute under which the User object
     * for the currently logged in user is stored.
     */
    public static final String WEBUSER_SES_ATTR = "webUser";
    
    public static final String STATEFUL_HTTP_SESSION_HDR_ATTR = "stateful_session" ; 

    /**
     * The session scope attribute under which an LDAP user's password
     * is stored during registration.
     */
    public static final String PASSWORD_SES_ATTR = "password";
    public static final String NEEDS_REGISTRATION = "registerUser";

    /**
     * The session scope attribute under which the <code>List</code>
     * of beans containing data for rendering a chart is stored.
     */
    public static final String CHART_DATA_SES_ATTR = "chartData";
    
    /**
     * The session scope attribute under which the default user preferences live
     */
    public static final String DEF_USER_PREFS = "userPrefs";

    /**
     * The session scope attribute under which the default role dashboard preferences live 
     */
    public static final String DEF_ROLE_DASH_PREFS = "roleDashPrefs";

    /**
     * The session scope attribute under which the defualt user dashboard preferences live
     */
    public static final String DEF_USER_DASH_PREFS = "userDashPrefs";
    
    /**
     * The request scope attribute for toggle dashboard display for default dashboard change
     */
    public static final String IS_DASHBOARD_REMOVED = "isDashRemoved";
    /**
     * The session scope attribute under which the ids of users
     * pending association with a role are stored.
     */
    public static final String PENDING_USERS_SES_ATTR = "pendingUsers";

    /**
     * The session scope attribute under which the ids of roles
     * pending association with a user are stored.
     */
    public static final String PENDING_ROLES_SES_ATTR = "PendingSessionRoles";

    /**
     * The session scope attribute under which the ids of groups
     * pending association with a role are stored.
     */
    public static final String PENDING_RESGRPS_SES_ATTR = "pendingResGrps";

    /**
     * The session scope attribute under which the ids of services pending
     * association with a application are stored.
     */    
    public static final String PENDING_APPSVCS_SES_ATTR = "pendingSessionAppSvcs";
    public static final String PENDING_APPSVCS_REQ_ATTR = "reqPendingAppSvcs";
    public static final String NUM_PENDING_APPSVCS_REQ_ATTR = "reqNumPendingAppSvcs";
    public static final String AVAIL_APPSVCS_REQ_ATTR = "reqAvailableAppSvcs";
    public static final String NUM_AVAIL_APPSVCS_REQ_ATTR = "reqNumAvailableAppSvcs";

    public static final String PENDING_SVCDEPS_SES_ATTR = "pendingSessionSvcDeps";
    public static final String PENDING_SVCDEPS_REQ_ATTR = "reqPendingSvcDeps";
    public static final String NUM_PENDING_SVCDEPS_REQ_ATTR = "reqNumPendingSvcDeps";
    public static final String AVAIL_SVCDEPS_REQ_ATTR = "reqAvailableSvcDeps";
    public static final String NUM_AVAIL_SVCDEPS_REQ_ATTR = "reqNumAvailableSvcDeps";

    public static final String APPSVC_CURRENT_ATTR = "appSvcCurrent";
    public static final String APPSVC_DEPENDEES_ATTR = "appSvcDependees";
    public static final String APPSVC_DEPENDERS_ATTR = "appSvcDependers";
    public static final String NUM_APPSVC_DEPENDEES_ATTR = "numAppSvcDependees";
    public static final String NUM_APPSVC_DEPENDERS_ATTR = "numAppSvcDependers";
    
    /**
     * The session scope attribute under which the return path for a
     * sub-workflow is stored.
     */
    public static final String RETURN_LOC_SES_ATTR = "returnLoc";

    /**
     * The sesion scope attribute under which the global workflow
     * mapping is stored.
     */
    public static final String WORKFLOW_SES_ATTR = "workflowSesAttr";
    
     /**
     * The session scope attribute under which the flag for whether or not
     * the return path is ignored is stored.
     *
     * This is for the case where you want to ignore the return path for "ok,"
     * but want to use it for cancel. vewwy twicky.
     */
    public static final String RETURN_IGNORED_FOR_OK_ATTR = "returnLocIgnored";
    
    /** 
     * The session scope attribute used to store the most recent browse url
     * used in the breadcrumb navigation
     */
    public static final String ROOT_BREADCRUMB_URL_ATTR_NAME = "rootBreadcrumbUrl";
    public final static String BREADCRUMB_SESSION_BACKUP_ATTR_NAME = "breadcrumbListBackup";
    public final static String BREADCRUMB_SESSION_ATTR_NAME = "breadcrumbList";
    
    //---------------------------------------request attributes

    /**
     * The request scope attribute under which the total list size 
     * is stored.
     */
    public static final String LIST_SIZE_ATTR = "listSize";
    
    /**
     * The request scope attribute under which the Portal object
     * is stored.
     */
    public static final String PORTAL_KEY = "portal";    

    /**
     * The request scope attribute under which actions store the
     * (optional) substitution parameter for the page title.
     */
    public static final String TITLE_PARAM_ATTR = "TitleParam";

    /**
     * The request scope attribute under which actions store the
     * (optional) second substitution parameter for the page title.
     */
    public static final String TITLE_PARAM2_ATTR = "TitleParam2";

    /**
     * The session scope attribute under which the ids of resources
     * pending association with a group are stored.
     */
    public static final String PENDING_RESOURCES_SES_ATTR = "PendingSessionResources";

    /**
     * The request scope attribute under which actions store the
     * number of <code>AppdefResourceValue</code> objects in the associated
     * <code>List</code>.
     */
    public static final String NUM_RESOURCES_ATTR = "NumResources";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of available <code>AppdefResourceValue</code> objects.
     */
    public static final String AVAIL_RESOURCES_ATTR = "AvailableResources";

    /** 
     * The request scope attribute under which actions store the
     * number of available <code>AppdefResourceValue</code> objects for a 
     * AuthzSubjectValue in the associated <code>List</code>.
     */
    public static final String NUM_AVAIL_RESOURCES_ATTR = "NumAvailableResources";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of pending <code>AppdefResourceValue</code> objects.
     */
    public static final String PENDING_RESOURCES_ATTR = "PendingResources";

    /**
     * The request scope attribute under which actions store the
     * number of available <code>AppdefResourceValue</code> objects for a 
     * AuthzSubjectValue in the associated <code>List</code>.
     */
    public static final String NUM_PENDING_RESOURCES_ATTR = "NumPendingResources";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>EventValue</code> objects.
     */
    public static final String ALL_EVENTS_ATTR = "AllEvents";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ResourceTypeValue</code>
     * objects.
     */
    public static final String ALL_RESTYPES_ATTR = "AllResTypes";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ResourceTypeValue</code>
     * objects.
     */
    public static final String ALL_PERMS_ATTR = "AllPerms";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ResourceTypeValue</code>
     * objects.
     */
    public static final String ALL_OPS_ATTR = "AllOps";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>AuthzSubjectValue</code> objects
     * for the requested role.
     */
    public static final String ROLE_USERS_ATTR = "RoleUsers";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>ResourceGroupValue</code> objects
     * for the requested role.
     */
    public static final String ROLE_RESGRPS_ATTR = "RoleResGrps";

    /**
     * The request scope attribute under which actions store the
     * number of <code>AuthzSubjectValue</code> objects in the
     * associated <code>List</code>.
     */
    public static final String NUM_USERS_ATTR = "NumUsers";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>AuthzSubjectValue</code>
     * objects.
     */
    public static final String ALL_USERS_ATTR = "AllUsers";

    /**
     * The request scope attribute under which actions store the
     * full <code>HashMap</code> of principals
     */
    public static final String ALL_PRINCIPALS_ATTR = "AllPrincipals";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ServiceValue</code>
     * objects (in the inventory context) or 
     * <code>ResourceDisplaySummary</code> beans (in the
     * monitoring context).
     */
    public static final String SERVICES_ATTR = "Services";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ServerValue</code>
     * objects (in the inventory context) or 
     * <code>ResourceDisplaySummary</code> beans (in the
     * monitoring context).
     */
    public static final String SERVERS_ATTR = "Servers";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ServiceValue</code>
     * objects.
     */
    public static final String APPDEF_ENTRIES_ATTR = "AppdefEntries";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ApplicationValue</code>
     * objects.
     */
    public static final String APPLICATIONS_ATTR = "Applications";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>AuthzSubjectValue</code>
     * objects representing those users not associated with a
     * particular role.
     */
    public static final String AVAIL_USERS_ATTR = "AvailableUsers";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>AuthzSubjectValue</code>
     * objects representing those users pending association with a
     * particular role.
     */
    public static final String PENDING_USERS_ATTR = "PendingUsers";

    /**
     * The request scope attribute under which actions store the
     * number of <code>ResourceGroupValue</code> objects in the
     * associated <code>List</code>.
     */
    public static final String NUM_RESGRPS_ATTR = "NumResGrps";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>ResourceGroupValue</code>
     * objects.
     */
    public static final String ALL_RESGRPS_ATTR = "AllResGrps";

    /**
     * The request scope attribute under which actions store the
     * number of <code>ResourceGroupValue</code> objects in the
     * associated <code>List</code> of groups available for
     * association with a role.
     */
    public static final String NUM_AVAIL_RESGRPS_ATTR = "NumAvailableResGrps";

    /**
     * the request scope attribute under which actions store the
     * <code>list</code> of <code>resourcegroupsvalue</code>
     * objects representing those resource groups not associated with
     * a particular role.
     */
    public static final String AVAIL_RESGRPS_ATTR = "AvailableResGrps";

    /**
     * The request scope attribute under which actions store the
     * number of <code>ResourceGroupValue</code> objects in the
     * associated <code>List</code> of groups pending association with
     * a role.
     */
    public static final String NUM_PENDING_RESGRPS_ATTR = "NumPendingResGrps";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>ResourceGroupValue</code>
     * objects representing those groups pending association with a
     * particular role.
     */
    public static final String PENDING_RESGRPS_ATTR = "PendingResGrps";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AuthzSubjectValue</code> object.
     */
    public static final String USER_ATTR = "User";

    public static final String RESOURCE_REMOVE_CHILDREN_FORM_ATTR =
        "RemoveResourceChildrenForm";

    public static final String RESOURCE_REMOVE_FORM_ATTR =
        "RemoveResourceForm";

    public static final String RESOURCE_REMOVE_SERVERS_FORM_ATTR =
        "RemoveServersForm";

    public static final String RESOURCE_REMOVE_SERVICES_FORM_ATTR =
        "RemoveServicesForm";

    public static final String RESOURCE_REMOVE_APPSERVICES_FORM_ATTR =
        "RemoveAppServicesForm";

    public static final String RESOURCE_REMOVE_GROUPS_MEMBERS_FORM_ATTR =
        "RemoveResourceGroupsForm";
    
    public static final String GROUP_REMOVE_MEMBERS_FORM_ATTR =
        "RemoveGroupResourcesForm";

    public static final String GROUP_FORM_ATTR = "GroupForm";
    
    /**
     * The context scope attribute under which the service locator is
     * stored.
     */
    public static final String SERVICE_LOCATOR_CTX_ATTR = "service-locator";


    /**
     * The context scope attribute under which the list of permissions
     * defined by the application is stored.
     */
    public static final String PERMISSIONS_CTX_ATTR = "permissions";

    /**
     * The context scope attribute under which the list of operations
     * defined by the application is stored.
     */
    public static final String OPERATIONS_CTX_ATTR = "operations";

    /**
     * The request attribute under which the list of operations
     * allowed for the given user is stored.
     */
    public static final String USER_OPERATIONS_ATTR = "useroperations";

    /**
     * The context scope attribute under which the list of resource
     * types defined by the application is stored.
     */
    public static final String RESTYPES_CTX_ATTR = "resource-types";

    /**
     * The context scope attribute under which the list of resource
     * locations defined by general configuration is stored.
     */
    public static final String RESOURCE_LOCATIONS_CTX_ATTR =
    "resource-locations";

    /**
     * The context scope attribute under which the list of platform
     * machine types defined by general configuration is stored.
     */
    public static final String PLATFORM_MACHINE_TYPES_CTX_ATTR =
    "platform-machine-types";
    
    /**
     * The context scope attribute under which the list of server
     * machine types defined by general configuration is stored.
     */
    public static final String SERVER_APPLICATION_TYPES_CTX_ATTR =
    "server-application-types";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AppdefResourceValue</code> object.
     */
    public static final String RESOURCE_ATTR = "Resource";

    // AppdefResourceTypeValue object
    public static final String RESOURCE_TYPE_ATTR = "ResourceType";
    
    public static final String CAN_TAKE_ACTION_ON_ALERT_ATTR = "canTakeAction";
    public static final String CAN_MODIFY_ALERT_ATTR = "canModify";
    public static final String CAN_VIEW_RESOURCE_TYPE_ALERT_TEMPLATE_ATTR = "canViewResourceTypeAlertTemplate";
    
    /**
     * The request or session scope attribute under which actions
     * store the <code>Array</code> of requested
     * <code>AppdefEntityID</code> objects.
     */
    public static final String ENTITY_IDS_ATTR = "EntityIds";

    /**
     * The request or session scope attribute under which actions
     * store the requested url.
     */
    public static final String URL_ATTR = "Url";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AIPlatformValue</code> object.
     */
    public static final String AIPLATFORM_ATTR = "AIPlatform";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AIScheduleValue</code> object.
     */
    public static final String AISCHEDULE_ATTR = "AISchedule";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>LastAIError</code> object.
     */
    public static final String LAST_AI_ERROR_ATTR = "LastAIError";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>AppdefResourceValue</code>
     * objects.
     */
    public static final String ALL_RESOURCES_ATTR = "AllResources";

    /**
     * The request scope attribute under which actions store the
     * <code>List</code> of <code>AppdefResourceValue</code> objects upon
     * which the web user can execute control actions.
     */
    public static final String ALL_RESOURCES_CONTROLLABLE =
    "AllResourcesControllable";

    /**
     * The request scope attribute under which actions store the
     * <code>AppdefSummary</code> object.
     */
    public static final String RESOURCE_SUMMARY_ATTR = "ResourceSummary";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>AIScheduleValue</code>
     * objects.
     */
    public static final String ALL_SCHEDULES_ATTR = "AllSchedules";

    /**
     * The request scope attribute under which actions store the
     * <code>AuthzSubjectValue</code> representing the user who most
     * recently modified the resource.
     */
    public static final String RESOURCE_OWNER_ATTR = "ResourceOwner";

    /**
     * The request scope attribute under which actions store the
     * <code>AuthzSubjectValue</code> representing the user who most
     * recently modified the resource.
     */
    public static final String GROUP_TYPE_LABEL = "GroupTypeLabel";
    public static final String GROUP_TYPE_ATTR = "GroupType";

    /**
     * The request scope attribute under which actions store the
     * <code>AuthzSubjectValue</code> representing the user who most
     * recently modified the resource.
     */
    public static final String RESOURCE_MODIFIER_ATTR = "ResourceModifier";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>PlatformValue</code> object.
     */
    public static final String PARENT_RESOURCE_ATTR = "ParentResource";
    
    /**
     * Indicate that this is a platform service we're dealing with
     */
    public static final String PLATFORM_SERVICE_ATTR = "IsPlatformService";
    
    /**
     * The request scope attribute under which actions store the
     * <code>Collection</code> of child resources of an appdef resource.
     */
    public static final String CHILD_RESOURCES_ATTR = "ChildResources";

    /**
     * The request scope attribute under which actions store the
     * number of child resources of an appdef resource.
     */
    public static final String NUM_CHILD_RESOURCES_ATTR = "NumChildResources";

    /**
     * The request scope attribute under which actions store the
     * <code>AppdefResourceTypeValue</code> of the child resources of
     * an appdef resource.
     */
    public static final String CHILD_RESOURCE_TYPE_ATTR = "ChildResourceType";

    /**
     * The request scope attribute under which actions store the
     * <code>Collection</code> with the number of child resources for each
     * child resource type of an appdef resource.
     */
    public static final String RESOURCE_TYPES_ATTR = "ResourceTypes";

    public static final String RESOURCE_OTHER_TYPES_ATTR = "OtherResourceTypes";

    /**
     * The request scope attribute under which actions store the
     * <code>Map</code> with the number of child resources for each
     * child resource type of an appdef resource.
     */
    public static final String RESOURCE_TYPE_MAP_ATTR = "ResourceTypeMap";

    /**
     * The request scope attribute under which actions store the
     * full <code>List</code> of <code>PlatformValue</code>
     * objects.
     */
    public static final String ALL_PLATFORMS_ATTR = "AllPlatforms";
    
    /**
     * The request or session scope attribute under which actions
     * store the requested <code>ScanState</code> object for 
     * autodiscovery.
     */
    public static final String SCAN_STATE_ATTR = "ScanState";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>ImportError</code> object for 
     * autodiscovery import.
     */
    public static final String IMPORT_ERROR_ATTR = "ImportError";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>IgnoreError</code> object for
     * autodiscover import.
     */
    public static final String IMPORT_IGNORE_ERROR_ATTR = "IgnoreError";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AIServerValue</code> object for 
     * autodiscovery.
     */
    public static final String AI_SERVERS = "AIServers";

    /**
     * The request or session scope attribute under which actions
     * store the requested <code>AIIpValue</code> object for 
     * autodiscovery.
     */
    public static final String AI_IPS = "AIIps";
    
    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>ResourceDisplaySummary</code>
     * objects representing the currently viewed resource's deployed
     * child resources.
     */
    public static final String DEPLOYED_HEALTH_SUMMARIES_ATTR =
        "DeployedHealthSummaries";

    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>ResourceDisplaySummary</code>
     * objects representing the currently viewed resource's internal
     * child resources.
     */
    public static final String INTERNAL_HEALTH_SUMMARIES_ATTR =
    "InternalHealthSummaries";

    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>ResourceDisplaySummary</code>
     * objects representing the currently viewed resource's host
     * resources.
     */
    public static final String HOST_HEALTH_SUMMARIES_ATTR =
    "HostHealthSummaries";
    
    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>ResourceDisplaySummary</code>
     * objects representing the currently viewed compatible group's members
     */
    public static final String GROUP_MEMBER_HEALTH_SUMMARIES_ATTR =
    "GroupMemberHealthSummaries";
    
    /**
     * The request scope attribute under which actions store a
     * <code>Map</code> of <code>MetricDisplaySummary</code>
     * objects keyed by metric category.
     */
    public static final String METRIC_SUMMARIES_ATTR = "MetricSummaries";

    /**
     * The request scope attribute under which actions store a
     * <code>PageList</code> of <code>PerformanceDisplaySummary</code>
     * objects.
     */
    public static final String PERF_SUMMARIES_ATTR = "PerfSummaries";

    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>MiniTab</code> objects representing
     * the minitabs to be displayed on the screen.
     */
    public static final String MINI_TABS_ATTR = "MiniTabs";

    /**
     * The request scope attribute under which actions store a
     * <code>List</code> of <code>MiniTab</code> objects representing
     * the minitabs to be displayed on the screen.
     */
    public static final String SUB_MINI_TABS_ATTR = "SubMiniTabs";

    public static final String TIME_INTERVALS_ATTR = "timeIntervals";
    
    public static final String ALL_PLATFORM_TYPES_ATTR = "platformTypes";
    public static final String ALL_SERVER_TYPES_ATTR = "serverTypes";
    public static final String ALL_PLATFORM_SERVICE_TYPES_ATTR = "platformServiceTypes";
    public static final String ALL_WINDOWS_SERVICE_TYPES_ATTR = "windowsServiceTypes";
    
    /**
     * Attribute name to store the value of the above flag
     */
    public static final String SERVER_BASED_AUTO_INVENTORY_VALUE =
        "serverBasedAutoInventoryValue";

    /**
     * Attribute name to store the status of the sbai flag
     */
    public static final String AUTO_INVENTORY = "autoInventory";

    /**
     * Attribute name to allow/disallow edit of Config Properties
     */
    public static final String EDIT_CONFIG = "editConfig";
    
    /**
     * Attribute name to represent the agents of a platform
     */
    public static final String AGENT = "agent";
    
    /**
     * Attribute name to represent the agents of a platform
     */
    public static final String AGENTS_COUNT = "agentsCount";
    
    /**
     * Attribute name to represent the whether rt is supported for this service or not
     */
    public static final String RT_SUPPORTED = "rtSupported";
    
    /**
     * Attribute name to represent the whether end user rt is supported for this
     * service or not
     */
    public static final String EU_RT_SUPPORTED = "euRtSupported";
    
    /**
     * Attribute name to represent serviceResponseTime
     */
    public static final String SERVICE_RT = "serviceRT";
    
    /**
     * Attribute name to represent endUserResponseTime
     */
    public static final String EU_RT = "euRT";

    /*
     * Attribute for metrics pending for addition to a resource.
     */
    public static final String PENDING_METRICS_ATTR = "pendingMetricsAttr";
    
    /*
     * Attribute for number of metrics pending for addtion to a resource.
     */
    public static final String NUM_PENDING_METRICS_ATTR = "numPendingMetricsAttr";
    
    /*
     * Attribute for metrics pending for addition to a resource.
     */
    public static final String AVAIL_METRICS_ATTR = "availMetricsAttr";

    /**
     * Attribute for value object holding current status of last control
     * action. 
     * @see org.hyperic.hq.ui.c
     */
    public static final String CONTROL_CURRENT_STATUS_ATTR = "controlCurrentStatus";
    
     /**
     * A value for a request attribute. <br>
     * which is a list of a history of control items.
     */
    public static final String CONTROL_HST_DETAIL_ATTR = "hstDetailAttr";
    
    /**
     * current configSchema for validation checks
     */
    public static final String CURR_CONFIG_SCHEMA = "configSchema";
    
    /**
     * old configResponse for validation checks
     */
    public static final String OLD_CONFIG_RESPONSE = "configResponse";

    /**
     * Request attribute of whether or not control is enabled for this
     * resource.
     */
    public static final String CONTROL_ENABLED_ATTR = "controlEnabledAttr";

    /** is performance enabled for this resource?
     */
    public static final String PERFORMANCE_SUPPORTED_ATTR = "perfSupported";
    
    /**
     * Request attribute of the list of scheduled control actions.
     */
    public static final String CONTROL_ACTIONS_SERVER_ATTR = "ctrlActionsSrvAttr";
  
    /*
     * Request attributes for different metric categories.
     */
    public static final String CAT_AVAILABILITY_METRICS_ATTR = "availabilityMetrics";
    public static final String CAT_PERFORMANCE_METRICS_ATTR = "performanceMetrics";
    public static final String CAT_THROUGHPUT_METRICS_ATTR = "throughputMetrics";
    public static final String CAT_UTILIZATION_METRICS_ATTR = "utilizationMetrics";

    public static final String ALERTS_ATTR="Alerts";
    public static final String ALERT_DEFS_ATTR="Definitions";
    public static final String ALERT_DEFINITION_ATTR="alertDef";

    /**
     * Attribute name to represent the shared configOptions of a resource
     */
    public static final String PRODUCT_CONFIG_OPTIONS = "productConfigOptions";

    /**
     * Attribute name to represent the monitor configOptions of a resource
     */
    public static final String MONITOR_CONFIG_OPTIONS = "monitorConfigOptions";

    /**
     * Request attribute indicating whether monitoring is enabled for a
     * resource.
     */
    public static final String MONITOR_ENABLED_ATTR = "monitorEnabledAttr";

    /**
     * Request attribute to hold monitoring help text
     */
    public static final String MONITOR_HELP = "monitorHelp";
    
    /**
     * Attribute name to represent the monitor configOptions of a resource
     */
    public static final String RT_CONFIG_OPTIONS = "rtConfigOptions";
    
    /**
     * Attribute name to represent the control configOptions of a resource
     */
    public static final String EFFECTIVE_POLICY = "effectivePolicy";

    /**
     * Attribute name to represent the control configOptions of a resource
     */
    public static final String CONTROL_CONFIG_OPTIONS = "controlConfigOptions";

    /**
     * Attribute name to represent the shared configOptions count of a resource
     */
    public static final String PRODUCT_CONFIG_OPTIONS_COUNT = "productConfigOptionsCount";

    /**
     * Attribute name to represent the monitor configOptions count of a resource
     */
    public static final String MONITOR_CONFIG_OPTIONS_COUNT = "monitorConfigOptionsCount";

    /**
     * Attribute name to represent the control configOptions count of a resource
     */
    public static final String CONTROL_CONFIG_OPTIONS_COUNT = "controlConfigOptionsCount";

    /**
     * Attribute name to represent the serverBasedAutoInventory flag from the UI
     */
    public static final String SERVER_BASED_AUTO_INVENTORY = "serverBasedAutoInventory";

    /**
     * Attribute to look up the chart data key.
     */
    public static final String CHART_DATA_KEYS = "chartDataKeys";

    /**
     * Attribute to look up the chart data key size.
     */
    public static final String CHART_DATA_KEYS_SIZE = "chartDataKeysSize";

    /**
     * Attribute name to represent whether or not control is enabled.
     */
    public static final String CONTROL_ENABLED = "controlEnabled";

    /**
     * Attribute name to represent whether or not custom properties is available
     */
    public static final String CUSTPROPS_AVAIL = "custPropsAvail";

    /**
     * Attribute to represent whether OS Type is editable in the Platform
     * EditTypeNetworkProperties form
     */
    public static final String PLATFORM_OS_EDITABLE = "platformOSEditable";

    /**
     * keyword identifier for the current resource location
     */
    public static final String CURR_RES_LOCATION_MODE = "currResourceMode";
    public static final String CURR_RES_LOCATION_TYPE = "currResourceType";
    public static final String CURR_RES_LOCATION_TAG = "currResourceUrl";
    
    /**
     * name of the table properties file that is stored in the servlet context
     *
     */
    public static final String PROPS_TAGLIB_NAME = "display";

    /**
     * Used in generating the message next to the "Auto-Discover {0}?" checkbox
     */
    public static final String AI_SAMPLE_SERVICETYPE_LIST = "autodiscoveryMessageServiceList";

    /**
     * Tiles context attribute used to denote internal services
     */
    public static final String CTX_INTERNAL = "internal";

    /**
     * Tiles context attribute used to store resource health summaries
     */
    public static final String CTX_SUMMARIES = "summaries";
    
    /**
     *  Attribute which holds the current inv hierarchy for Res Hub
     */
    public static final String INVENTORY_HIERARCHY_ATTR = "navHierarchy";
    
    /**
     * The workflow name for comparing metrics
     */
    public static final String WORKFLOW_COMPARE_METRICS_NAME =
        "visibility/MetricDisplayRange";

    public static final String RESOURCE_HEALTH_LIST = "resourceHealthList";
    
    /**
     * AJAX attributes
     */
    public static final String AJAX_TYPE = "ajaxType";
    public static final String AJAX_ID   = "ajaxId";
    public static final String AJAX_HTML = "ajaxHTML";
    
    public static final String AJAX_JSON = "ajaxJson";

    /**
     * XLib present
     */
    public static final String XLIB_INSTALLED = "xlib";
    
    /**
    * Attribute name for the SNMP local engine ID
    */
    public static final String SNMP_LOCAL_ENGINE_ID = "snmpLocalEngineID";
    
}
