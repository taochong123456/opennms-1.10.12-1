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
package org.hyperic.hq.common.server.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.UUIDGenerator;

class GUIDGenerator {
    private static final Log _log = LogFactory.getLog(GUIDGenerator.class);

    static String createGUID() {
        Sigar sigar = null;
        
        try {
            EthernetAddress eAddr;
            sigar = new Sigar();
            String[] ifaces = sigar.getNetInterfaceList();
            String hwaddr = null;
            
            for (int i=0; i<ifaces.length; i++) {
                NetInterfaceConfig cfg = 
                    sigar.getNetInterfaceConfig(ifaces[i]);

                if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress()) ||
                    (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0 ||
                    NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) 
                {
                    continue;
                }
                
                hwaddr = cfg.getHwaddr();
                break;
            }
            
            if (hwaddr == null) {
                _log.warn("Unable to get MAC hardware address -- none found");
                return null;
            }

            _log.debug("Obtained HW MAC: " + hwaddr);
            eAddr = new EthernetAddress(hwaddr);
            return UUIDGenerator.getInstance()
                                .generateTimeBasedUUID(eAddr)
                                .toString();
        } catch(Exception e) {
            _log.warn("Error while creating GUID", e);
            return null;
        } finally {
            if (sigar != null) 
                sigar.close();
        }
    }
}
