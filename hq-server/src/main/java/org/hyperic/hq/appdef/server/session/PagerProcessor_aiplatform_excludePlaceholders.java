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

package org.hyperic.hq.appdef.server.session;


import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AIQueueConstants;

public class PagerProcessor_aiplatform_excludePlaceholders 
    extends PagerProcessor_aiplatform {

    public PagerProcessor_aiplatform_excludePlaceholders () {
        super();
    }

    public Object processElement ( Object o ) {

        Object processed = super.processElement(o);
        if ( processed == null ) return null;
        if ( processed instanceof AIPlatformValue ) {
            AIPlatformValue pv = (AIPlatformValue) processed;
            if (pv.getQueueStatus() == AIQueueConstants.Q_STATUS_PLACEHOLDER) {
                // System.err.println("----->>> status for '" + pv.getFqdn() + "' was placeholder, returning NULL");
                return null;
            }
            // System.err.println("----->>> status for '" + pv.getFqdn() + "' was NOT placeholder, returning value");
            return processed;
        }
        return null;
    }
}
