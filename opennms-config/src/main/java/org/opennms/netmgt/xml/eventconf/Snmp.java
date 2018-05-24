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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The SNMP information from the trap
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="snmp")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Snmp implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The SNMP enterprise ID
     */
	@XmlElement(name="id", required=true)
    private String m_id;

    /**
     * The SNMP enterprise ID text
     */
	@XmlElement(name="idtext", required=false)
    private String m_idtext;

    /**
     * The SNMP version
     */
	@XmlElement(name="version", required=true)
    private String m_version;

    /**
     * The specific trap number
     */
	@XmlElement(name="specific", required=false)
    private Integer m_specific;

    /**
     * The generic trap number
     */
	@XmlElement(name="generic", required=false)
    private Integer m_generic;

    /**
     * The community name
     */
	@XmlElement(name="community", required=false)
    private String m_community;


      //----------------/
     //- Constructors -/
    //----------------/

    public Snmp() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteGeneric(
    ) {
        m_generic = null;
    }

    /**
     */
    public void deleteSpecific(
    ) {
        m_specific = null;
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
        
        if (obj instanceof Snmp) {
        
            Snmp temp = (Snmp)obj;
            if (this.m_id != null) {
                if (temp.m_id == null) return false;
                else if (!(this.m_id.equals(temp.m_id))) 
                    return false;
            }
            else if (temp.m_id != null)
                return false;
            if (this.m_idtext != null) {
                if (temp.m_idtext == null) return false;
                else if (!(this.m_idtext.equals(temp.m_idtext))) 
                    return false;
            }
            else if (temp.m_idtext != null)
                return false;
            if (this.m_version != null) {
                if (temp.m_version == null) return false;
                else if (!(this.m_version.equals(temp.m_version))) 
                    return false;
            }
            else if (temp.m_version != null)
                return false;
            if (this.m_specific != temp.m_specific)
                return false;
            if (this.m_generic != temp.m_generic)
                return false;
            if (this.m_community != null) {
                if (temp.m_community == null) return false;
                else if (!(this.m_community.equals(temp.m_community))) 
                    return false;
            }
            else if (temp.m_community != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'community'. The field
     * 'community' has the following description: The community
     * name
     * 
     * @return the value of field 'Community'.
     */
    public String getCommunity(
    ) {
        return this.m_community;
    }

    /**
     * Returns the value of field 'generic'. The field 'generic'
     * has the following description: The generic trap number
     * 
     * @return the value of field 'Generic'.
     */
    public Integer getGeneric(
    ) {
        return this.m_generic;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the
     * following description: The SNMP enterprise ID
     * 
     * @return the value of field 'Id'.
     */
    public String getId(
    ) {
        return this.m_id;
    }

    /**
     * Returns the value of field 'idtext'. The field 'idtext' has
     * the following description: The SNMP enterprise ID text
     * 
     * @return the value of field 'Idtext'.
     */
    public String getIdtext(
    ) {
        return this.m_idtext;
    }

    /**
     * Returns the value of field 'specific'. The field 'specific'
     * has the following description: The specific trap number
     * 
     * @return the value of field 'Specific'.
     */
    public Integer getSpecific(
    ) {
        return this.m_specific;
    }

    /**
     * Returns the value of field 'version'. The field 'version'
     * has the following description: The SNMP version
     * 
     * @return the value of field 'Version'.
     */
    public String getVersion(
    ) {
        return this.m_version;
    }

    /**
     * Method hasGeneric.
     * 
     * @return true if at least one Generic has been added
     */
    public boolean hasGeneric(
    ) {
        return m_generic != null;
    }

    /**
     * Method hasSpecific.
     * 
     * @return true if at least one Specific has been added
     */
    public boolean hasSpecific(
    ) {
        return m_specific != null;
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
        return new HashCodeBuilder(17,37).append(getGeneric()).append(getSpecific())
        	.append(getCommunity()).append(getId()).append(getIdtext()).append(getVersion()).toHashCode();
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
     * Sets the value of field 'community'. The field 'community'
     * has the following description: The community name
     * 
     * @param community the value of field 'community'.
     */
    public void setCommunity(
            final String community) {
        this.m_community = community;
    }

    /**
     * Sets the value of field 'generic'. The field 'generic' has
     * the following description: The generic trap number
     * 
     * @param generic the value of field 'generic'.
     */
    public void setGeneric(
            final int generic) {
        this.m_generic = generic;
    }

    /**
     * Sets the value of field 'id'. The field 'id' has the
     * following description: The SNMP enterprise id
     * 
     * @param id the value of field 'id'.
     */
    public void setId(
            final String id) {
        this.m_id = id;
    }

    /**
     * Sets the value of field 'idtext'. The field 'idtext' has the
     * following description: The SNMP enterprise id text
     * 
     * @param idtext the value of field 'idtext'.
     */
    public void setIdtext(
            final String idtext) {
        this.m_idtext = idtext;
    }

    /**
     * Sets the value of field 'specific'. The field 'specific' has
     * the following description: The specific trap number
     * 
     * @param specific the value of field 'specific'.
     */
    public void setSpecific(
            final int specific) {
        this.m_specific = specific;
    }

    /**
     * Sets the value of field 'version'. The field 'version' has
     * the following description: The SNMP version
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(
            final String version) {
        this.m_version = version;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.eventconf.Snmp
     */
    public static org.opennms.netmgt.xml.eventconf.Snmp unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Snmp) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Snmp.class, reader);
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
