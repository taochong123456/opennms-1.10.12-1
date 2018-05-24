package org.opennms.netmgt.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="protocolprop",uniqueConstraints = {@UniqueConstraint(columnNames={"nodeid", "serviceid"})})
public class ProtocolProperty {
	private int id;
	private int nodeId;
	private int serviceid;
	private String key;
	private String value;
	
	@Id
    @Column(nullable=false)
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
	public int getId() {
		return id;
	}
	
	@Column(name="nodeid")
	public int getNodeId() {
		return nodeId;
	}
	
	@Column(name="key")
	public String getKey() {
		return key;
	}
	
	@Column(name="serviceid")
	public int getServiceid() {
		return serviceid;
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
