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

package org.hyperic.hq.authz.server.session;

import java.util.ResourceBundle;

import org.hyperic.hibernate.SortField;
import org.hyperic.util.HypericEnum;

public abstract class ResourceSortField 
    extends HypericEnum
    implements SortField
{
    private static final String BUNDLE = "org.hyperic.hq.authz.Resources";
    
    public static final ResourceSortField NAME = 
        new ResourceSortField(0, "Name", "resource.sortField.name") 
    {
        String getSortString(String resource) {
            return resource + ".name";
        }

        public boolean isSortable() {
            return true;
        }
    };

    private ResourceSortField(int code, String desc, String localeProp) {
        super(ResourceSortField.class, code, desc, localeProp,
              ResourceBundle.getBundle(BUNDLE));
    }
    
    /**
     * Returns HQL which can be used to tack onto an HQL query, to sort
     */
    abstract String getSortString(String resource);
    
    public static ResourceSortField findByCode(int code) {
        return (ResourceSortField)
            HypericEnum.findByCode(ResourceSortField.class, code); 
    }
}
