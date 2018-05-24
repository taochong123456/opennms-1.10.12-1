/**
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 *  "derived work".
 *
 *  Copyright (C) [2004-2010], VMware, Inc.
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
package org.hyperic.hq.control.server.session;

import org.hyperic.hibernate.PersistedObject;
import org.hyperic.hq.appdef.shared.AppdefEntityID;

public class ControlHistory extends PersistedObject
{
    // Fields

    private Integer groupId;
    private Integer batchId;
    private Integer entityType;
    private Integer entityId;
    private String entityName;
    private String subject;
    private boolean scheduled;
    private long dateScheduled;
    private long startTime;
    private String status;
    private long endTime;
    private String description;
    private String message;
    private String action;
    private String args;

    // Constructors

    /**
     * default constructor
     */
    public ControlHistory()
    {
    }

    public Integer getGroupId()
    {
        return this.groupId;
    }

    public void setGroupId(Integer groupId)
    {
        this.groupId = groupId;
    }

    public Integer getBatchId()
    {
        return this.batchId;
    }

    public void setBatchId(Integer batchId)
    {
        this.batchId = batchId;
    }

    public Integer getEntityType()
    {
        return this.entityType;
    }

    public void setEntityType(Integer entityType)
    {
        this.entityType = entityType;
    }

    public Integer getEntityId()
    {
        return this.entityId;
    }

    public void setEntityId(Integer entityId)
    {
        this.entityId = entityId;
    }

    public String getSubject()
    {
        return this.subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public boolean isScheduled()
    {
        return this.scheduled;
    }

    public void setScheduled(boolean scheduled)
    {
        this.scheduled = scheduled;
    }

    public Boolean getScheduled()
    {
        return new Boolean(isScheduled());
    }

    public void setScheduled(Boolean scheduled)
    {
        setScheduled(scheduled.booleanValue());
    }

    public long getDateScheduled()
    {
        return this.dateScheduled;
    }

    public void setDateScheduled(long dateScheduled)
    {
        this.dateScheduled = dateScheduled;
    }

    public long getStartTime()
    {
        return this.startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public String getStatus()
    {
        return this.status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public long getEndTime()
    {
        return this.endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getDuration()
    {return 100;}

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getAction()
    {
        return this.action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getArgs()
    {
        return this.args;
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    private String getEntityIdString()
    {
        AppdefEntityID id = new AppdefEntityID(getEntityType().intValue(),
                                               getEntityId());
        return id.getAppdefKey();
    }

    public String getEntityName() {
        if (entityName == null)
            return getEntityIdString();
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean equals(Object obj)
    {
        return (obj instanceof ControlHistory) && super.equals(obj);
    }
}


