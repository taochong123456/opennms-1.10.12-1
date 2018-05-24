/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2008], Hyperic, Inc.
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

package org.hyperic.hq.appdef.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hyperic.hq.appdef.server.session.AppdefResourceType;
import org.hyperic.hq.appdef.server.session.Application;
import org.hyperic.hq.appdef.server.session.ApplicationType;
import org.hyperic.hq.appdef.server.session.Platform;
import org.hyperic.hq.appdef.server.session.PlatformType;
import org.hyperic.hq.appdef.server.session.Server;
import org.hyperic.hq.appdef.server.session.ServerType;
import org.hyperic.hq.appdef.server.session.Service;
import org.hyperic.hq.appdef.server.session.ServiceType;
import org.hyperic.hq.authz.server.session.AuthzSubject;
import org.hyperic.hq.authz.server.session.Resource;
import org.hyperic.hq.authz.server.session.ResourceGroup;
import org.hyperic.hq.authz.shared.AuthzConstants;


/**
 * An abstract class which all appdef value objects inherit from
 * 
 * The accessors provided in this class represent what the UI model labels
 * "General Properties". Any other attribute is assumed to be specific
 * to the resource type.
 *
 *
 */
public abstract class AppdefResourceValue
    implements Serializable, Comparable
{

    // they all have id's
    public abstract Integer getId();
    public abstract void setId(Integer id);

    // they all have names
    public abstract String getName();
    public abstract void setName(String name);

    // they all have owners;
    public abstract String getOwner();
    public abstract void setOwner(String owner);

    // they all have modifiers
    public abstract String getModifiedBy();
    public abstract void setModifiedBy(String modifiedBy);

    // they all have descriptions
    public abstract String getDescription();
    public abstract void setDescription(String desc);

    // they all have ctime 
    public abstract Long getCTime();
    // they all have mtime
    public abstract Long getMTime();

    // they all have location
    public abstract String getLocation();
    public abstract void setLocation(String loc);

    // Storage for host name
    private String hostName = null;    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    /**
     * get an entity ID for the object
     */
    public abstract AppdefEntityID getEntityId();



    
    
    public int compareTo(Object arg0) {
        if (!(arg0 instanceof AppdefResourceValue))
            return -1;
            
        return this.getName().compareTo(((AppdefResourceValue) arg0).getName());
    }
    
    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof AppdefResourceValue) {
            AppdefEntityID aeid = (AppdefEntityID) rhs;
            return getEntityId().equals(aeid);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getEntityId().hashCode();
    }
	public static AppdefResourceType getAppdefResourceType(
			AuthzSubject subject, ResourceGroup g) {
		// TODO Auto-generated method stub
		return null;
	}

    
}
