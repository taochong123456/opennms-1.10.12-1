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

package org.hyperic.hq.zevents;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.util.thread.ThreadGroupFactory;

class BufferedListener<T extends Zevent>
    extends ThreadPoolExecutor
    implements ZeventListener<T>
{
    private static Log _log = LogFactory.getLog(BufferedListener.class);

    private final ZeventListener<T> _target;
    
    BufferedListener(ZeventListener<T> target, ThreadGroupFactory fact) {
        super(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue(), fact);
        ZeventManager.getInstance().registerBuffer(getQueue(), target); 
        _target = target;
    }

    private static class BufferedEventRunnable
        implements Runnable 
    {
        private final List           _events;
        private final ZeventListener _target;
        
        BufferedEventRunnable(List events, ZeventListener target) {
            _events = events;
            _target = target;
        }
        
        public void run() {
            _target.processEvents(_events);
        }
    }

    public void processEvents(List events) {
        try {
            execute(new BufferedEventRunnable(events, _target));
        } catch (Throwable e) {
            _log.error(e,e);
        }
        int size = getQueue().size();
        if (_log.isDebugEnabled() && size != 0 && (size % 100) == 0) {
            _log.debug("obj=" + this.toString() + ", queue size=" + size);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return _target.equals(obj);
    }

    @Override
    public int hashCode() {
        return _target.hashCode();
    }
    
    @Override
    public String toString() {
        return _target.toString();
    }
    
    public ZeventListener getTarget() {
        return _target;
    }
}
