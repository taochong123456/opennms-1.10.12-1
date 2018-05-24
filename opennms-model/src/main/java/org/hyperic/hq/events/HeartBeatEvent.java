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

package org.hyperic.hq.events;

import java.util.Date;

import org.hyperic.hq.zevents.HeartBeatZevent;

public class HeartBeatEvent extends AbstractEvent
    implements java.io.Serializable {
    
    private static final long serialVersionUID = -5753333005138238760L;
    
    /** Holds value of property beat. */
    private Date beat;
    
    /** Creates a new instance of HeartBeatEvent */
    public HeartBeatEvent(Date beat) {
        setBeat(beat);
    }
    
    public String toString() {
        if (this.beat != null)
            return this.beat.toString();
        else
            return super.toString();
    }    
    
    /** Getter for property beat.
     * @return Value of property beat.
     *
     */
    public Date getBeat() {
        return this.beat;
    }
    
    /** Setter for property beat.
     * @param beat New value of property beat.
     *
     */
    public void setBeat(Date beat) {
        this.beat = beat;
        setTimestamp(beat.getTime());
    }
    
    /**
     * Convert this heart beat event into an equivalent zevent.
     * 
     * @return A heart beat zevent.
     */
    public HeartBeatZevent toZevent() {
        return new HeartBeatZevent(this.getBeat());
    }
}
