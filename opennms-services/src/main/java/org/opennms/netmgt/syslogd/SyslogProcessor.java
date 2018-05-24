/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Parm;

/**
 * This class encapsulates the execution context for processing syslog messsages
 * received via UDP from remote agents. This is a separate event context to
 * allow the event receiver to do minimum work to avoid dropping packets from
 * the agents.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */
final class SyslogProcessor implements Runnable {

    @SuppressWarnings("unused")
    private BroadcastEventProcessor m_eventReader;

    /**
     * The UDP receiver thread.
     */
    private Thread m_context;

    /**
     * The stop flag
     */
    private volatile boolean m_stop;

    private boolean m_NewSuspectOnMessage;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    private String m_localAddr;

    /**
     * <p>setSyslogConfig</p>
     *
     * @param syslogdConfig a {@link org.opennms.netmgt.config.SyslogdConfig} object.
     */
    public static void setSyslogConfig(SyslogdConfig syslogdConfig) {
        @SuppressWarnings("unused")
        SyslogdConfig m_syslogdConfig = syslogdConfig;
    }

    SyslogProcessor(boolean newSuspectOnMessage, String forwardingRegexp, int matchingGroupHost,
                    int matchingGroupMessage, UeiList ueiList, HideMessage hideMessages) {
        m_context = null;
        m_stop = false;
        m_NewSuspectOnMessage = newSuspectOnMessage;

        m_logPrefix = Syslogd.LOG4J_CATEGORY;

        m_localAddr = InetAddressUtils.getLocalHostName();
    }

    /**
     * Returns true if the thread is still alive
     */
    boolean isAlive() {
        return (m_context != null && m_context.isAlive());
    }

    /**
     * Stops the current context
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            ThreadCategory log = ThreadCategory.getInstance(getClass());
            if (log.isDebugEnabled())
                log.debug("Stopping and joining thread context " + m_context.getName());

            m_context.interrupt();
            m_context.join();

            log.debug("Thread context stopped and joined");
        }
    }

    /**
     * The event processing execution context.
     */
    public void run() {
        // The runnable context
        m_context = Thread.currentThread();

        // get a logger
        ThreadCategory.setPrefix(m_logPrefix);
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        boolean isTracing = log.isEnabledFor(ThreadCategory.Level.TRACE);

        if (m_stop) {
            if (isTracing)
                log.trace("Stop flag set before thread started, exiting");
            return;
        } else if (isTracing)
            log.trace("Thread context started");

        while (!m_stop) {

            ConvertToEvent o = null;

            o = SyslogHandler.queueManager.getFromQueue();

            if (o != null) {
                try {
                    if (isTracing)  {
                        log.trace("Processing a syslog to event dispatch" + o.toString());
                        String uuid = o.getEvent().getUuid();
                        log.trace("Event {");
                        log.trace("  uuid  = "
                                + (uuid != null && uuid.length() > 0 ? uuid
                                : "<not-set>"));
                        log.trace("  uei   = " + o.getEvent().getUei());
                        log.trace("  src   = " + o.getEvent().getSource());
                        log.trace("  iface = " + o.getEvent().getInterface());
                        log.trace("  time  = " + o.getEvent().getTime());
                        log.trace("  Msg   = "
                                + o.getEvent().getLogmsg().getContent());
                        log.trace("  Dst   = "
                                + o.getEvent().getLogmsg().getDest());
                        List<Parm> parms = (o.getEvent().getParmCollection() == null ? null : o.getEvent().getParmCollection());
                        if (parms != null) {
                            log.trace("  parms {");
                            for (Parm parm : parms) {
                                if ((parm.getParmName() != null)
                                        && (parm.getValue().getContent() != null)) {
                                    log.trace("    ("
                                            + parm.getParmName().trim()
                                            + ", "
                                            + parm.getValue().getContent().trim()
                                            + ")");
                                }
                            }
                            log.trace("  }");
                        }
                        log.trace("}");
                    }

                    EventIpcManagerFactory.getIpcManager().sendNow(o.getEvent());

                    if (m_NewSuspectOnMessage && !o.getEvent().hasNodeid()) {
                        if (isTracing) {
                            log.trace("Syslogd: Found a new suspect " + o.getEvent().getInterface());
                        }
                        sendNewSuspectEvent(o.getEvent().getInterface());
                    }

                } catch (Throwable t) {
                    log.error("Unexpected error processing SyslogMessage - Could not send", t);
                }
            }

        }

    }

    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }

    private void sendNewSuspectEvent(String trapInterface) {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "syslogd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(m_localAddr);
        EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());
    }
}
