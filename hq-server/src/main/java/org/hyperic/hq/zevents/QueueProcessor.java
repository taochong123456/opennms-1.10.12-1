/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2009], Hyperic, Inc.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class acts as the thread pulling data off the event queue and 
 * dispatching it to listeners.
 */
class QueueProcessor 
    implements Runnable
{
    private final Log _log = LogFactory.getLog(ZeventManager.class);

    private final BlockingQueue _eventQueue;
    private final ZeventManager _manager;
    private final int           _batchSize;
    private final Object        DATA_LOCK = new Object();        
    
    QueueProcessor(ZeventManager manager, BlockingQueue eventQueue,
                   int batchSize)
    {
        _manager    = manager;
        _eventQueue = eventQueue;
        _batchSize  = batchSize;
    }

    public void run() {
        while (true) {
            processBatch();
        }
    }
    
    private void processBatch() {
        try {
            // We use take() to get the first element, since this will block
            // until the queue has data.  We then use drainTo(), since it
            // is not blocking but will allow us to batch.
            List batch = new ArrayList(_batchSize);
            batch.add(_eventQueue.take());
            
            // Replacing drainTo() due to the undefined behavior if the operation in progress and the collection is modified
            // Removes available events from this queue and adds them to the batch
            // The number of removed events is the minimum  between all available events (queue size) and batch size
            int availSize=_eventQueue.size();
            int drainSize=Math.min(availSize, _batchSize - 1);
            if (_log.isDebugEnabled()) {
                _log.debug("available events=[" + availSize + "], batch size=[" + _batchSize +"], drainSize=[" + drainSize + "]");
            }
            for (int index = 0; index < drainSize; index++) {
                Object obj =_eventQueue.poll();
                // in case that other threads poll the queue
                if (obj == null) {
                    break;
                }
                batch.add(obj);
            }

            for (Iterator i=batch.iterator(); i.hasNext(); ) {
                Zevent e = (Zevent)i.next();
                e.leaveQueue();
            }
            
            _manager.dispatchEvents(batch);

            // TODO The thread is not actually interrupted since the run continue the process batch
        } catch(InterruptedException exc) {
            _log.warn("Thread interrupted.  I'm dying");
            return;
        } catch(Exception exc) {
            _log.warn("Unable to dequeue and dispatch", exc);
        }
    }
}
