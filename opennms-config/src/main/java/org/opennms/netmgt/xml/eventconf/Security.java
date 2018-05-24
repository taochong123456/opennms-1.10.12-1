/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.eventconf;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Security settings for this configuration
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="security")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Security implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Event element whose value cannot be overridden by a
     *  value in an incoming event
     */
	@XmlElement(name="doNotOverride", required=true)
    private List<String> m_doNotOverrideList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Security() {
        super();
        this.m_doNotOverrideList = new java.util.ArrayList<String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vDoNotOverride
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDoNotOverride(
            final String vDoNotOverride)
    throws IndexOutOfBoundsException {
        this.m_doNotOverrideList.add(vDoNotOverride);
    }

    /**
     * 
     * 
     * @param index
     * @param vDoNotOverride
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDoNotOverride(
            final int index,
            final String vDoNotOverride)
    throws IndexOutOfBoundsException {
        this.m_doNotOverrideList.add(index, vDoNotOverride);
    }

    /**
     * Method enumerateDoNotOverride.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<String> enumerateDoNotOverride(
    ) {
        return java.util.Collections.enumeration(this.m_doNotOverrideList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Security) {
        
            Security temp = (Security)obj;
            if (this.m_doNotOverrideList != null) {
                if (temp.m_doNotOverrideList == null) return false;
                else if (!(this.m_doNotOverrideList.equals(temp.m_doNotOverrideList))) 
                    return false;
            }
            else if (temp.m_doNotOverrideList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getDoNotOverride.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getDoNotOverride(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_doNotOverrideList.size()) {
            throw new IndexOutOfBoundsException("getDoNotOverride: Index value '" + index + "' not in range [0.." + (this.m_doNotOverrideList.size() - 1) + "]");
        }
        
        return (String) m_doNotOverrideList.get(index);
    }

    /**
     * Method getDoNotOverride.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getDoNotOverride(
    ) {
        String[] array = new String[0];
        return (String[]) this.m_doNotOverrideList.toArray(array);
    }

    /**
     * Method getDoNotOverrideCollection.Returns a reference to
     * '_doNotOverrideList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getDoNotOverrideCollection(
    ) {
        return this.m_doNotOverrideList;
    }

    /**
     * Method getDoNotOverrideCount.
     * 
     * @return the size of this collection
     */
    public int getDoNotOverrideCount(
    ) {
        return this.m_doNotOverrideList.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        return new HashCodeBuilder(17,37).append(getDoNotOverride()).toHashCode();
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateDoNotOverride.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<String> iterateDoNotOverride(
    ) {
        return this.m_doNotOverrideList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllDoNotOverride(
    ) {
        this.m_doNotOverrideList.clear();
    }

    /**
     * Method removeDoNotOverride.
     * 
     * @param vDoNotOverride
     * @return true if the object was removed from the collection.
     */
    public boolean removeDoNotOverride(
            final String vDoNotOverride) {
        boolean removed = m_doNotOverrideList.remove(vDoNotOverride);
        return removed;
    }

    /**
     * Method removeDoNotOverrideAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeDoNotOverrideAt(
            final int index) {
        Object obj = this.m_doNotOverrideList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDoNotOverride
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDoNotOverride(
            final int index,
            final String vDoNotOverride)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_doNotOverrideList.size()) {
            throw new IndexOutOfBoundsException("setDoNotOverride: Index value '" + index + "' not in range [0.." + (this.m_doNotOverrideList.size() - 1) + "]");
        }
        
        this.m_doNotOverrideList.set(index, vDoNotOverride);
    }

    /**
     * 
     * 
     * @param vDoNotOverrideArray
     */
    public void setDoNotOverride(
            final String[] vDoNotOverrideArray) {
        //-- copy array
        m_doNotOverrideList.clear();
        
        for (int i = 0; i < vDoNotOverrideArray.length; i++) {
                this.m_doNotOverrideList.add(vDoNotOverrideArray[i]);
        }
    }

    /**
     * Sets the value of '_doNotOverrideList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vDoNotOverrideList the Vector to copy.
     */
    public void setDoNotOverride(
            final java.util.List<String> vDoNotOverrideList) {
        // copy vector
        this.m_doNotOverrideList.clear();
        
        this.m_doNotOverrideList.addAll(vDoNotOverrideList);
    }

    /**
     * Sets the value of '_doNotOverrideList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param doNotOverrideList the Vector to set.
     */
    public void setDoNotOverrideCollection(
            final java.util.List<String> doNotOverrideList) {
        this.m_doNotOverrideList = doNotOverrideList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.xml.eventconf.Security
     */
    public static org.opennms.netmgt.xml.eventconf.Security unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Security) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Security.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
