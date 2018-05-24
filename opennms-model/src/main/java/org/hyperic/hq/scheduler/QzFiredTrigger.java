/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
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

package org.hyperic.hq.scheduler;

public class QzFiredTrigger  implements java.io.Serializable {

    // Fields
    private String _entryId;
    private String _triggerName;
    private String _triggerGroup;
    private String _instanceName;
    private long _firedTime;
    private String _state;
    private boolean _isVolatile;
    private String _jobName;
    private String _jobGroup;
    private boolean _isStateful;
    private boolean _requestsRecovery;
    private int     _priority;

    // Constructors
    public QzFiredTrigger() {
    }

    // Property accessors
    public String getEntryId() {
        return _entryId;
    }
    
    public void setEntryId(String entryId) {
        _entryId = entryId;
    }

    public String getTriggerName() {
        return _triggerName;
    }
    
    public void setTriggerName(String triggerName) {
        _triggerName = triggerName;
    }

    public String getTriggerGroup() {
        return _triggerGroup;
    }
    
    public void setTriggerGroup(String triggerGroup) {
        _triggerGroup = triggerGroup;
    }

    public String getInstanceName() {
        return _instanceName;
    }
    
    public void setInstanceName(String instanceName) {
        _instanceName = instanceName;
    }

    public long getFiredTime() {
        return _firedTime;
    }
    
    public void setFiredTime(long firedTime) {
        _firedTime = firedTime;
    }

    public String getState() {
        return _state;
    }
    
    public void setState(String state) {
        _state = state;
    }

    public boolean isIsVolatile() {
        return _isVolatile;
    }
    
    public void setIsVolatile(boolean isVolatile) {
        _isVolatile = isVolatile;
    }

    public String getJobName() {
        return _jobName;
    }
    
    public void setJobName(String jobName) {
        _jobName = jobName;
    }

    public String getJobGroup() {
        return _jobGroup;
    }
    
    public void setJobGroup(String jobGroup) {
        _jobGroup = jobGroup;
    }

    public boolean isIsStateful() {
        return _isStateful;
    }
    
    public void setIsStateful(boolean isStateful) {
        _isStateful = isStateful;
    }

    public boolean isRequestsRecovery() {
        return _requestsRecovery;
    }
    
    public void setRequestsRecovery(boolean requestsRecovery) {
        _requestsRecovery = requestsRecovery;
    }

    public int getPriority() {
        return _priority;
    }

    public void setPriority(int priority) {
        _priority = priority;
    }

}
