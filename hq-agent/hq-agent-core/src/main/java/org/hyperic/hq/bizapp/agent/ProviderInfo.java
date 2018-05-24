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

package org.hyperic.hq.bizapp.agent;

public class ProviderInfo {
    private String providerAddress;    // CAM connection string
    private String agentToken;         // Token the agent should use to connect
    private boolean isNewTransport;
    private boolean unidirectional;
    private int unidirectionalPort;

    public ProviderInfo(String providerAddress, String agentToken){
        this.providerAddress = providerAddress;
        this.agentToken      = agentToken;

        if(providerAddress == null ||
           agentToken == null)
        {
            throw new IllegalArgumentException("No arguments can be null");
        }
        
        unidirectionalPort = -1;
    }
    
    public void setNewTransport(boolean unidirectional, int unidirectionalPort) {
        this.isNewTransport = true;
        this.unidirectional = unidirectional;
        this.unidirectionalPort = unidirectionalPort;
    }

    public String getProviderAddress(){
        return this.providerAddress;
    }

    public String getAgentToken(){
        return this.agentToken;
    }
    
    public boolean isNewTransport() {
        return this.isNewTransport;
    }
    
    public boolean isUnidirectional() {
        if (this.unidirectional && !isNewTransport()) {
            throw new IllegalStateException("not a new transport");
        }
        
        return this.unidirectional;
    }
    
    public int getUnidirectionalPort() {
        if (!isNewTransport()) {
            throw new IllegalStateException("not a new transport");
        }
        
        return this.unidirectionalPort;
    }

    public String toString(){
        return this.providerAddress;
    }

}
