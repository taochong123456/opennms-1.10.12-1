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

package org.opennms.netmgt.capsd;

import java.io.File;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OpenNMSIntegrationTestCase;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class CapsdIntegrationTest extends OpenNMSIntegrationTestCase {
    
    private static final int FOREIGN_NODEID = 77;

    private Capsd m_capsd;
    private MockSnmpAgent m_agent;

   protected String[] getConfigLocations() {
       try {
        setupOpenNMSHomeDir();
    } catch (Throwable e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
                "classpath:META-INF/opennms/applicationContext-capsd.xml",
                "classpath:META-INF/opennms/smallEventConfDao.xml",
        };
    }

    public void setCapsd(Capsd capsd) {
        m_capsd = capsd;
    }
    
    @Override
    protected MockNetwork createMockNetwork() {
        MockNetwork network = super.createMockNetwork();
        
        network.addNode(FOREIGN_NODEID, "ForeignNode");
        network.addInterface("172.20.1.201");
        network.addService("ICMP");
        network.addService("SNMP");
        
        return network;
    }

    
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), InetAddressUtils.getLocalHostAddressAsString()+"/9161");
    }
    
    

    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
    }

    @Override
    protected String preprocessConfigContents(File srcFile, String contents) {
        if (srcFile.getName().matches("snmp-config.xml")) {
            return getSnmpConfig();
        } else if (srcFile.getName().matches("capsd-configuration.xml")) {
            String updatedContents = contents.replaceAll("initial-sleep-time=\"30000\"", "initial-sleep-time=\"300\"");
            updatedContents = updatedContents.replaceAll("scan=\"on\"", "scan=\"off\"");
            updatedContents = updatedContents.replaceAll("SnmpPlugin\" scan=\"off\"", "SnmpPlugin\" scan=\"on\"");
            return updatedContents;
        } else {
            return contents;
        }
    }

    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " version=\"v1\">\n" +
                "   <definition version=\"v2c\" port=\"9161\" read-community=\"public\" proxy-host=\""+InetAddressUtils.getLocalHostAddressAsString()+"\">\n" + 
                "      <specific>172.20.1.201</specific>\n" +
                "      <specific>172.20.1.204</specific>\n" +
                "   </definition>\n" + 
                "</snmp-config>\n";
    }
 
    public final void testRescan() throws Exception {/*
        
        assertEquals("Initially only 1 interface", 1, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.start();
        
        m_capsd.rescanInterfaceParent(77);
        
        Thread.sleep(10000);
        
        m_capsd.stop();
        
        assertEquals("after scanning should be 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    */}
    
 
    
    

}
