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

package org.hyperic.hq.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarProxy;

/**
 * The Humidor provides access to a single Sigar instance.  Sigar has a 
 * high-cost setup/teardown and has a high amount of caching, used to determine
 * things like the % of CPU used in-between calls.  
 * 
 * The Humidor also synchronizes access to all Sigar methods, so all
 * calls will be serialized.
 */
public class Humidor {
    private static final Humidor INSTANCE = new Humidor();
    private Object LOCK = new Object();
    private InvocationHandler _handler;
    private SigarProxy _sigar;
    
    private Humidor() {}

    private static class MyHandler implements InvocationHandler {
        private SigarProxy _sigar;
        private Object _lock = new Object();
        
        public MyHandler(SigarProxy sigar) {
            _sigar = sigar;
        }
        
        public Object invoke(Object proxy, Method meth, Object[] args) 
            throws Throwable  
        {
            if (meth.getName().equals("close")) {
                throw new IllegalArgumentException("Can't close a " + 
                                                   "Humidored Sigar!");
            }
            
            try {
                synchronized (_lock) {
                    return meth.invoke(_sigar, args);
                }
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
    
    public SigarProxy getSigar() {
        synchronized(LOCK) {
            if (_sigar == null) {
                Sigar s  = new Sigar();
                _handler = new MyHandler(s);
                _sigar = (SigarProxy)
                    Proxy.newProxyInstance(Humidor.class.getClassLoader(),
                                           new Class[] { SigarProxy.class },
                                           _handler);
            }
            return _sigar;
        }
    }
    
    public static Humidor getInstance() {
        return INSTANCE;
    }
}
