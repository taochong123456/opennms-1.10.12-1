//#############################################
package org.opennms.netmgt.syslogd.analyze;

/**
 * BYACC/J Semantic Value for parser: Parser This class provides some of the
 * functionality of the yacc/C 'union' directive
 */
public class ParserVal {
	/**
	 * integer value of this 'union'
	 */
	public int ival;

	/**
	 * double value of this 'union'
	 */
	public double dval;

	/**
	 * boolean value of this 'union'
	 */
	public boolean bval;

	/**
	 * string value of this 'union'
	 */
	public String sval;

	/**
	 * object value of this 'union'
	 */
	public Object obj;
	/**
	 * token type of this 'union'
	 */
	public int tokenType = -1;

	public int getTokenType() {
		return tokenType;
	}

	public void setTokenType(int tokenType) {
		this.tokenType = tokenType;
	}

	/**
	 * SyntaxNode value of this 'union'
	 */
	public SyntaxNode refSyntaxNode;

	public SyntaxNode getSyntaxNode() {
		return refSyntaxNode;
	}

	public void setSyntaxNode(SyntaxNode refSyntaxNode) {
		this.refSyntaxNode = refSyntaxNode;
	}

	// #############################################
	// ## M E T H O D S
	// #############################################
	/**
	 * Assign the values
	 */
	public void assign(ParserVal v) {
		ival = v.ival;
		dval = v.dval;
		bval = v.bval;
		sval = v.sval;
		obj = v.obj;
		tokenType = v.tokenType;
		refSyntaxNode = v.refSyntaxNode;
	}

	// #############################################
	// ## C O N S T R U C T O R S
	// #############################################
	/**
	 * Initialize me as an int
	 */
	public ParserVal(int val) {
		ival = val;
	}

	/**
	 * Initialize me as a double
	 */
	public ParserVal(double val) {
		dval = val;
	}

	/**
	 * Initialize me as a string
	 */
	public ParserVal(String val) {
		sval = val;
	}

	/**
	 * Initialize me as a boolean
	 */
	public ParserVal(boolean val) {
		bval = val;
	}

	/**
	 * Initialize me as an Object
	 */
	public ParserVal(Object val) {
		obj = val;
	}
}// end class

// #############################################
// ## E N D O F F I L E
// #############################################
