//#############################################
package org.opennms.netmgt.syslogd.analyze;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * BYACC/J Syntax tree node for parser: SyntaxNode
 */
public class SyntaxNode {
	public SyntaxNode() {
	}

	/**
	 * production code ,index of array yyrule in parser
	 */
	public int production = -1;
	/**
	 * node value
	 */
	public ParserVal nodeValue = new ParserVal(-1);
	/**
	 * parent node
	 */
	public SyntaxNode parent;
	/**
	 * children nodes��arranged from left to rigth
	 */
	public ArrayList childrenNodes = new ArrayList();

	public void addChild(SyntaxNode child) {
		childrenNodes.add(child);
	}

	public ParserVal getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(ParserVal nodeValue) {
		this.nodeValue.assign(nodeValue);
	}

	public SyntaxNode getParent() {
		return parent;
	}

	public void setParent(SyntaxNode parent) {
		this.parent = parent;
	}

	public ArrayList getChildrenNodes() {
		return childrenNodes;
	}

	public int getProduction() {
		return production;
	}

	public void setProduction(int production) {
		this.production = production;
	}
}// end class

// #############################################
// ## E N D O F F I L E
// #############################################
