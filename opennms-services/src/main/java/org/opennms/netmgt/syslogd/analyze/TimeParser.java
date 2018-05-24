//###################################################################################
//### $Id: TimeParser.java,v 1.2 2008/11/06 10:53:11 liunan Exp $
//###################################################################################
//### This file created by BYACC 1.8(/Java extension  1.2)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//### Please send bug reports to rjamison@lincom-asg.com
//###################################################################################

package org.opennms.netmgt.syslogd.analyze;

//#line 2 "timeresouce.y"

import java.lang.Math;
import java.io.*;
import java.util.*;

//#line 26 "Parser.java"

public class TimeParser {

	boolean yydebug; // do I want debug output?
	int yynerrs; // number of errors so far
	int yyerrflag; // was there an error?
	int yychar; // the current working character

	// ########## MESSAGES ##########
	// ###############################################################
	// method: yypdebug
	// ###############################################################
	void yypdebug(String msg) {
		if (yydebug)
			System.out.println(msg);
	}

	// ########## STATE STACK ##########
	final static int YYSTACKSIZE = 500; // maximum stack size. Set by -Jstack=
	int statestk[], stateptr; // state stack
	int stateptrmax; // highest index of stackptr
	int statemax; // state when highest index reached
	// ###############################################################
	// methods: state stack push,pop,drop,peek
	// ###############################################################

	void state_push(int state) {
		try {
			statestk[++stateptr] = state;
		} catch (ArrayIndexOutOfBoundsException e) // overflowed?
		{
			int oldsize = statestk.length;
			int newsize = oldsize * 2;
			int[] newstack = new int[newsize];
			System.arraycopy(statestk, 0, newstack, 0, oldsize);
			statestk = newstack;
			if (yydebug)
				yypdebug(" state stack increased from " + oldsize + " to "
						+ newsize);
			statestk[stateptr] = state;
		}
		if (stateptr > statemax) {
			statemax = state;
			stateptrmax = stateptr;
		}
	}

	int state_pop() {
		if (stateptr < 0) // underflowed?
			return -1;
		return statestk[stateptr--];
	}

	void state_drop(int cnt) {
		int ptr;
		ptr = stateptr - cnt;
		if (ptr < 0)
			return;
		stateptr = ptr;
	}

	int state_peek(int relative) {
		int ptr;
		ptr = stateptr - relative;
		if (ptr < 0)
			return -1;
		return statestk[ptr];
	}

	// ###############################################################
	// method: init_stacks : allocate and prepare stacks
	// ###############################################################
	boolean init_stacks() {
		statestk = new int[YYSTACKSIZE];
		stateptr = -1;
		statemax = -1;
		stateptrmax = -1;
		val_init();
		return true;
	}

	// ###############################################################
	// method: dump_stacks : show n levels of the stacks
	// ###############################################################
	void dump_stacks(int count) {
		int i;
		System.out.println("=index==state====value=     s:" + stateptr + "  v:"
				+ valptr);
		for (i = 0; i < count; i++)
			System.out.println(" " + i + "    " + statestk[i] + "      "
					+ valstk[i]);
		System.out.println("======================");
	}

	// ########## SEMANTIC VALUES ##########
	// public class TimeParser is defined in TimeParser.java

	String yytext;// user variable to return contextual strings
	ParserVal yyval; // used to return semantic vals from action routines
	ParserVal yylval;// the 'lval' (result) I got from yylex()
	ParserVal valstk[];
	int valptr;

	// ###############################################################
	// methods: value stack push,pop,drop,peek.
	// ###############################################################
	void val_init() {
		valstk = new ParserVal[YYSTACKSIZE];
		for (int i = 0; i < YYSTACKSIZE; i++)
			valstk[i] = new ParserVal(0);
		yyval = new ParserVal(0);
		yylval = new ParserVal(0);
		valptr = -1;
	}

	void val_push(ParserVal val) {
		if (valptr >= valstk.length) {
			int oldsize = valstk.length;
			int newsize = oldsize * 2;
			ParserVal[] newstack = new ParserVal[newsize];
			System.arraycopy(valstk, 0, newstack, 0, oldsize);
			for (int i = oldsize; i < newsize; i++)
				newstack[i] = new ParserVal(0);
			valstk = newstack;
		}
		valstk[++valptr].assign(val);
	}

	ParserVal val_pop() {
		if (valptr < 0)
			return new ParserVal(-1);
		return valstk[valptr--];
	}

	void val_drop(int cnt) {
		int ptr;
		ptr = valptr - cnt;
		if (ptr < 0)
			return;
		valptr = ptr;
	}

	ParserVal val_peek(int relative) {
		int ptr;
		ptr = valptr - relative;
		if (ptr < 0)
			return new ParserVal(-1);
		return valstk[ptr];
	}

	// #### end semantic value section ####
	public final static short NUM = 257;
	public final static short WR = 258;
	public final static short NONE = 259;
	public final static short NE = 260;
	public final static short LE = 261;
	public final static short GE = 262;
	public final static short QU = 263;
	public final static short YYERRCODE = 256;
	final static short yylhs[] = { -1, 0, 0, 2, 2, 3, 3, 4, 4, 5, 1, 1, 1, };
	final static short yylen[] = { 2, 1, 2, 25, 29, 1, 2, 3, 1, 3, 1, 1, 2, };
	final static short yydefred[] = { 0, 0, 1, 0, 0, 0, 0, 5, 10, 11, 0, 6, 12,
			0, 8, 0, 2, 0, 0, 9, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 3, 0, 0, 0, 0, 4, };
	final static short yydgoto[] = { 5, 6, 7, 8, 15, 9, };
	final static short yysindex[] = { -10, -49, 0, -38, -32, 0, 2, 0, 0, 0,
			-248, 0, 0, -247, 0, -36, 0, -244, -32, 0, -243, 0, -241, -240,
			-238, -251, -37, -237, -235, -233, -33, -31, -231, -229, -16, -27,
			-226, -224, -223, -9, -221, -220, -222, -218, -217, -216, -214,
			-213, -212, -211, -18, -209, -208, -207, -12, -205, -204, -203, 0,
			-8, -202, -2, -200, 0, };
	final static short yyrindex[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, };
	final static short yygindex[] = { 0, 0, 1, -1, 0, 55, };
	final static int YYTABLESIZE = 248;
	static short yytable[];
	static {
		yytable();
	}

	static void yytable() {
		yytable = new short[] { 2, 13, 4, 14, 11, 19, 26, 27, 18, 10, 17, 1,
				16, 20, 11, 22, 23, 21, 24, 25, 29, 28, 30, 3, 31, 32, 34, 33,
				35, 36, 4, 37, 38, 39, 40, 44, 41, 42, 43, 45, 52, 46, 47, 48,
				49, 50, 56, 51, 53, 54, 60, 55, 57, 58, 59, 61, 62, 63, 12, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, };
	}

	static short yycheck[];
	static {
		yycheck();
	}

	static void yycheck() {
		yycheck = new short[] { 10, 33, 40, 4, 3, 41, 257, 258, 44, 58, 258,
				258, 10, 257, 13, 258, 257, 18, 258, 257, 257, 58, 257, 33,
				257, 58, 257, 58, 257, 45, 40, 58, 258, 257, 257, 257, 45, 258,
				258, 257, 58, 258, 258, 257, 257, 257, 58, 258, 257, 257, 58,
				258, 257, 257, 257, 257, 58, 257, 3, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 258, -1, -1, -1,
				-1, -1, 258, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, 258, };
	}

	final static short YYFINAL = 5;
	final static short YYMAXTOKEN = 263;
	final static String yyname[] = { "end-of-file", null, null, null, null,
			null, null, null, null, null, "'\\n'", null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, "'!'", null, null, null,
			null, null, null, "'('", "')'", "'*'", "'+'", "','", "'-'", null,
			"'/'", null, null, null, null, null, null, null, null, null, null,
			"':'", null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, "'^'", null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null,
			null, "NUM", "WR", "NONE", "NE", "LE", "GE", "QU", };
	public final static String yyrule[] = {
			"$accept : line",
			"line : '\\n'",
			"line : exp '\\n'",
			"time : WR ':' WR NUM WR NUM WR NUM NUM ':' NUM ':' NUM '-' WR NUM WR NUM WR NUM NUM ':' NUM ':' NUM",
			"time : WR ':' WR NUM WR NUM WR NUM WR NUM NUM ':' NUM ':' NUM '-' WR NUM WR NUM WR NUM WR NUM NUM ':' NUM ':' NUM",
			"single : time", "single : '!' time",
			"complex_inline : complex_inline ',' single",
			"complex_inline : single", "complex : '(' complex_inline ')'",
			"exp : single", "exp : complex", "exp : '!' complex", };

	// #line 147 "timeresouce.y"

	private TimeResource result;

	public TimeResource getTimeResource() {
		return result;
	}

	private LexParser aL;

	private int yylex() {
		if (aL == null)
			return -1; // some error occurs

		int ttype;

		try {
			ttype = aL.nextToken(); // fetch next token
		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}

		switch (ttype) {
		case LexParser.NUM:
			yylval = new ParserVal(aL.nval);
			yylval.setTokenType(LexParser.NUM);
			return ttype;
		case LexParser.WR:
			yylval = new ParserVal(aL.sval);
			int wtype = KeywordsDes.getTokenValue(aL.sval);
			if (wtype == -1) { // not key words
				yylval.setTokenType(ttype);
				return ttype;
			} else {
				yylval.setTokenType(wtype);
				return wtype;
			}
		default:
			yylval = new ParserVal(aL.sval);
			yylval.setTokenType(ttype);
			return ttype;
		}

	}

	public void parse(Reader r) {
		aL = new LexParser(r);
		aL.eolIsSignificant(true); // ����'\n'��token
		yyparse();
	}

	public SyntaxNode syntaxTree(Reader r) {
		aL = new LexParser(r);
		aL.eolIsSignificant(true); // ����'\n'��token
		return yytree();
	}

	void yyerror(String s) {
		System.out.println("par:" + s + " at line:" + aL.getLINENO() + " col:"
				+ aL.getCOLNO());
	}

	public static void main(String args[]) {
		TimeParser par = new TimeParser(false);
		try {
			// par.parse(new FileReader("test.txt"));
			// par.parse(new StringReader(" \n"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// #line 347 "TimeParser.java"
	// ###############################################################
	// method: yylexdebug : check lexer state
	// ###############################################################
	void yylexdebug(int state, int ch) {
		String s = null;
		if (ch < 0)
			ch = 0;
		if (ch <= YYMAXTOKEN) // check index bounds
			s = yyname[ch]; // now get it
		if (s == null)
			s = "illegal-symbol";
		yypdebug("state " + state + ", reading " + ch + " (" + s + ")");
	}

	// The following are now global, to aid in error reporting
	int yyn; // next next thing to do
	int yym; //
	int yystate; // current parsing state from state table
	String yys; // current token string

	// ###############################################################
	// method: yyparse : parse input and execute indicated items
	// ###############################################################
	int yyparse() {
		boolean doaction;
		init_stacks(); // set up stacks
		yynerrs = 0; // no errors yet
		yyerrflag = 0; // error level
		yychar = -1; // impossible char forces a read
		yystate = 0; // initial state
		state_push(yystate); // save it
		while (true) // until parsing is done, either correctly, or w/error
		{
			doaction = true;
			if (yydebug)
				yypdebug("loop");

			// #########################
			// # NEXT-ACTION LOOP
			// #########################
			for (yyn = yydefred[yystate]; yyn == 0; yyn = yydefred[yystate]) {
				if (yydebug)
					yypdebug("yyn:" + yyn + "  state:" + yystate + "  yychar:"
							+ yychar);
				if (yychar < 0) // we want a char?
				{
					yychar = yylex(); // get next token
					if (yydebug)
						yypdebug(" next yychar:" + yychar);
					// #### ERROR CHECK ####
					if (yychar < 0) // it it didn't work/error
					{
						yychar = 0; // change it to default string (no -1!)
						if (yydebug)
							yylexdebug(yystate, yychar);
					}
				}// yychar<0

				// ### SHIFT ###
				yyn = yysindex[yystate]; // get amount to shift by (shift index)
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) {
					if (yydebug)
						yypdebug("state " + yystate + ", shifting to state "
								+ yytable[yyn]);
					// #### NEXT STATE ####
					yystate = yytable[yyn];// we are in a new state
					state_push(yystate); // save it
					val_push(yylval); // push our lval as the input for next
										// rule
					yychar = -1; // since we have 'eaten' a token, say we need
									// another
					if (yyerrflag > 0) // have we recovered an error?
						--yyerrflag; // give ourselves credit
					doaction = false; // but don't process yet
					break; // quit the yyn=0 loop
				}

				// ### REDUCE ###
				yyn = yyrindex[yystate]; // reduce
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) { // we reduced!
					if (yydebug)
						yypdebug("reduce");
					yyn = yytable[yyn];
					doaction = true; // get ready to execute
					break; // drop down to actions
				}

				// ### ERROR RECOVERY ###
				if (yyerrflag == 0) {
					yyerror("syntax error");
					yynerrs++;
				}
				if (yyerrflag < 3) // low error count?
				{
					yyerrflag = 3;
					while (true) // do until break
					{
						if (stateptr < 0) // check for under & overflow here
						{
							yyerror("stack underflow. aborting..."); // note
																		// lower
																		// case
																		// 's'
							return 1;
						}
						yyn = yysindex[state_peek(0)];
						if ((yyn != 0) && (yyn += YYERRCODE) >= 0
								&& yyn <= YYTABLESIZE
								&& yycheck[yyn] == YYERRCODE) {
							if (yydebug)
								yypdebug("state " + state_peek(0)
										+ ", error recovery shifting to state "
										+ yytable[yyn] + " ");
							yystate = yytable[yyn];
							state_push(yystate);
							val_push(yylval);
							doaction = false;
							break;
						} else {
							if (yydebug)
								yypdebug("error recovery discarding state "
										+ state_peek(0) + " ");
							if (stateptr < 0) // check for under & overflow here
							{
								yyerror("Stack underflow. aborting..."); // capital
																			// 'S'
								return 1;
							}
							state_pop();
							val_pop();
						}
					}
				} else // discard this token
				{
					if (yychar == 0)
						return 1; // yyabort
					if (yydebug) {
						yys = null;
						if (yychar <= YYMAXTOKEN)
							yys = yyname[yychar];
						if (yys == null)
							yys = "illegal-symbol";
						yypdebug("state " + yystate
								+ ", error recovery discards token " + yychar
								+ " (" + yys + ")");
					}
					yychar = -1; // read another
				}

			}
			// #########################
			// # END NEXT-ACTION LOOP
			// #########################

			if (!doaction) // any reason not to proceed?
				continue; // skip action
			yym = yylen[yyn]; // get count of terminals on rhs
			if (yydebug)
				yypdebug("state " + yystate + ", reducing " + yym + " by rule "
						+ yyn + " (" + yyrule[yyn] + ")");
			if (yym > 0) // if count of rhs not 'nil'
				yyval = val_peek(yym - 1); // get current semantic value
			switch (yyn) {
			// ########## USER-SUPPLIED ACTIONS ##########
			// ######## CASE 2 ########
			case 2:
			// #line 40 "timeresouce.y"
			{
				/* $$ = $1; */
				/* System.out.println(" result>>> " + $1.sval + " "); */
				result = (TimeResource) val_peek(1).obj;
			}
				break;

			// ######## CASE 3 ########
			case 3:
			// #line 47 "timeresouce.y"
			{
				TimeResource pr = new TimeResource();

				pr.setStartYear((int) val_peek(21).dval);
				pr.setStartMonth((int) val_peek(19).dval);
				pr.setStartDay((int) val_peek(17).dval);
				pr.setStartHour((int) val_peek(16).dval);
				pr.setStartMinute((int) val_peek(14).dval);
				pr.setStartSecond((int) val_peek(12).dval);

				pr.setEndYear((int) val_peek(9).dval);
				pr.setEndMonth((int) val_peek(7).dval);
				pr.setEndDay((int) val_peek(5).dval);
				pr.setEndHour((int) val_peek(4).dval);
				pr.setEndMinute((int) val_peek(2).dval);
				pr.setEndSecond((int) val_peek(0).dval);

				yyval.obj = pr;
			}
				break;

			// ######## CASE 4 ########
			case 4:
			// #line 66 "timeresouce.y"
			{
				TimeResource pr = new TimeResource();
				pr.setTimeFlag(1);

				pr.setStartYear((int) val_peek(25).dval);
				pr.setStartMonth((int) val_peek(23).dval);
				pr.setStartWeek((int) val_peek(21).dval);
				pr.setStartDay((int) val_peek(19).dval);
				pr.setStartHour((int) val_peek(18).dval);
				pr.setStartMinute((int) val_peek(16).dval);
				pr.setStartSecond((int) val_peek(14).dval);

				pr.setEndYear((int) val_peek(11).dval);
				pr.setEndMonth((int) val_peek(9).dval);
				pr.setEndWeek((int) val_peek(7).dval);
				pr.setEndDay((int) val_peek(5).dval);
				pr.setEndHour((int) val_peek(4).dval);
				pr.setEndMinute((int) val_peek(2).dval);
				pr.setEndSecond((int) val_peek(0).dval);

				yyval.obj = pr;
			}
				break;

			// ######## CASE 5 ########
			case 5:
			// #line 90 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 6 ########
			case 6:
			// #line 94 "timeresouce.y"
			{
				/* $$.sval = " [*!"+$2.sval+"*] "; */
				((TimeResource) val_peek(0).obj).setLogicFlag(1);
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 7 ########
			case 7:
			// #line 101 "timeresouce.y"
			{
				/* System.out.println($1.sval+",,,"+$3.sval); */
				/* $$.sval=" [*"+$1.sval+" , "+$3.sval+"*] "; */

				TimeResource pr = (TimeResource) val_peek(2).obj;
				if (pr.getSegments() == null) {
					List<TimeResource> segments = new ArrayList<TimeResource>();
					segments.add(pr);
					segments.add((TimeResource) val_peek(0).obj);
					TimeResource newPr = new TimeResource();
					newPr.setSegments(segments);
					yyval.obj = newPr;
				} else {
					pr.getSegments().add((TimeResource) val_peek(0).obj);
					yyval.obj = pr;
				}

			}
				break;

			// ######## CASE 8 ########
			case 8:
			// #line 119 "timeresouce.y"
			{
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 9 ########
			case 9:
			// #line 124 "timeresouce.y"
			{
				/* System.out.println("((("+$2.sval+")))"); */
				/* $$.sval=" [*( "+$2.sval+" )*] "; */
				yyval.obj = val_peek(1).obj;
			}
				break;

			// ######## CASE 10 ########
			case 10:
			// #line 131 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 11 ########
			case 11:
			// #line 135 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 12 ########
			case 12:
			// #line 139 "timeresouce.y"
			{
				/* $$.sval = " [*!"+$2.sval+"*] "; */
				((TimeResource) val_peek(0).obj).setLogicFlag(1);
				yyval.obj = val_peek(0).obj;
			}
				break;

			// #line 665 "TimeParser.java"
			// ########## END OF USER-SUPPLIED ACTIONS ##########
			}// switch
				// #### Now let's reduce... ####
			if (yydebug)
				yypdebug("reduce");
			state_drop(yym); // we just reduced yylen states
			yystate = state_peek(0); // get new state
			val_drop(yym); // corresponding value drop
			yym = yylhs[yyn]; // select next TERMINAL(on lhs)
			if (yystate == 0 && yym == 0)// done? 'rest' state and at first
											// TERMINAL
			{
				yypdebug("After reduction, shifting from state 0 to state "
						+ YYFINAL + "");
				yystate = YYFINAL; // explicitly say we're done
				state_push(YYFINAL); // and save it
				val_push(yyval); // also save the semantic value of parsing
				if (yychar < 0) // we want another character?
				{
					yychar = yylex(); // get next character
					if (yychar < 0)
						yychar = 0; // clean, if necessary
					if (yydebug)
						yylexdebug(yystate, yychar);
				}
				if (yychar == 0) // Good exit (if lex returns 0 ;-)
					break; // quit the loop--all DONE
			}// if yystate
			else // else not done yet
			{ // get next state and push, for next yydefred[]
				yyn = yygindex[yym]; // find out where to go
				if ((yyn != 0) && (yyn += yystate) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yystate)
					yystate = yytable[yyn]; // get new state
				else
					yystate = yydgoto[yym]; // else go to new defred
				yypdebug("after reduction, shifting from state "
						+ state_peek(0) + " to state " + yystate + "");
				state_push(yystate); // going again, so push state & val...
				val_push(yyval); // for next action
			}
		}// main loop
		return 0;// yyaccept!!
	}

	// ## end of method yyparse() ######################################

	// ###############################################################
	// method: yytree : parse input and execute indicated items
	// ###############################################################
	SyntaxNode yytree() {
		SyntaxNode root = null; // return value of syntax tree root node
		boolean doaction;
		init_stacks(); // set up stacks
		yynerrs = 0; // no errors yet
		yyerrflag = 0; // error level
		yychar = -1; // impossible char forces a read
		yystate = 0; // initial state
		state_push(yystate); // save it
		while (true) // until parsing is done, either correctly, or w/error
		{
			doaction = true;
			if (yydebug)
				yypdebug("loop");

			// #########################
			// # NEXT-ACTION LOOP
			// #########################
			for (yyn = yydefred[yystate]; yyn == 0; yyn = yydefred[yystate]) {
				if (yydebug)
					yypdebug("yyn:" + yyn + "  state:" + yystate + "  yychar:"
							+ yychar);
				if (yychar < 0) // we want a char?
				{
					yychar = yylex(); // get next token
					if (yydebug)
						yypdebug(" next yychar:" + yychar);
					// #### ERROR CHECK ####
					if (yychar < 0) // it it didn't work/error
					{
						yychar = 0; // change it to default string (no -1!)
						if (yydebug)
							yylexdebug(yystate, yychar);
					}
				}// yychar<0

				// ### SHIFT ###
				yyn = yysindex[yystate]; // get amount to shift by (shift index)
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) {
					if (yydebug)
						yypdebug("state " + yystate + ", shifting to state "
								+ yytable[yyn]);
					// #### NEXT STATE ####
					yystate = yytable[yyn];// we are in a new state
					state_push(yystate); // save it
					val_push(yylval); // push our lval as the input for next
										// rule
					yychar = -1; // since we have 'eaten' a token, say we need
									// another
					if (yyerrflag > 0) // have we recovered an error?
						--yyerrflag; // give ourselves credit
					doaction = false; // but don't process yet
					break; // quit the yyn=0 loop
				}

				// ### REDUCE ###
				yyn = yyrindex[yystate]; // reduce
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) { // we reduced!
					if (yydebug)
						yypdebug("reduce");
					yyn = yytable[yyn];
					doaction = true; // get ready to execute
					break; // drop down to actions
				}

				// ### ERROR RECOVERY ###
				if (yyerrflag == 0) {
					yyerror("syntax error");
					yynerrs++;
				}
				if (yyerrflag < 3) // low error count?
				{
					yyerrflag = 3;
					while (true) // do until break
					{
						if (stateptr < 0) // check for under & overflow here
						{
							yyerror("stack underflow. aborting..."); // note
																		// lower
																		// case
																		// 's'
							return null;
						}
						yyn = yysindex[state_peek(0)];
						if ((yyn != 0) && (yyn += YYERRCODE) >= 0
								&& yyn <= YYTABLESIZE
								&& yycheck[yyn] == YYERRCODE) {
							if (yydebug)
								yypdebug("state " + state_peek(0)
										+ ", error recovery shifting to state "
										+ yytable[yyn] + " ");
							yystate = yytable[yyn];
							state_push(yystate);
							val_push(yylval);
							doaction = false;
							break;
						} else {
							if (yydebug)
								yypdebug("error recovery discarding state "
										+ state_peek(0) + " ");
							if (stateptr < 0) // check for under & overflow here
							{
								yyerror("Stack underflow. aborting..."); // capital
																			// 'S'
								return null;
							}
							state_pop();
							val_pop();
						}
					}
				} else // discard this token
				{
					if (yychar == 0)
						return null; // yyabort
					if (yydebug) {
						yys = null;
						if (yychar <= YYMAXTOKEN)
							yys = yyname[yychar];
						if (yys == null)
							yys = "illegal-symbol";
						yypdebug("state " + yystate
								+ ", error recovery discards token " + yychar
								+ " (" + yys + ")");
					}
					yychar = -1; // read another
				}

			}
			// #########################
			// # END NEXT-ACTION LOOP
			// #########################

			if (!doaction) // any reason not to proceed?
				continue; // skip action
			yym = yylen[yyn]; // get count of terminals on rhs
			if (yydebug)
				yypdebug("state " + yystate + ", reducing " + yym + " by rule "
						+ yyn + " (" + yyrule[yyn] + ")");
			if (yym > 0) // if count of rhs not 'nil'
				yyval = val_peek(yym - 1); // get current semantic value

			root = new SyntaxNode(); // allocate syntaxnode
			root.setProduction(yyn); // record the rule in the root
			for (int i = yym - 1; i >= 0; i--) {
				ParserVal temp = val_peek(i);
				SyntaxNode childNode = temp.getSyntaxNode();
				if (childNode == null) {
					childNode = new SyntaxNode();
					childNode.setNodeValue(temp);
				}
				root.addChild(childNode);// loop for adding root children
			}
			switch (yyn) {
			// ########## USER-SUPPLIED ACTIONS ##########
			// ######## CASE 2 ########
			case 2:
			// #line 40 "timeresouce.y"
			{
				/* $$ = $1; */
				/* System.out.println(" result>>> " + $1.sval + " "); */
				result = (TimeResource) val_peek(1).obj;
			}
				break;

			// ######## CASE 3 ########
			case 3:
			// #line 47 "timeresouce.y"
			{
				TimeResource pr = new TimeResource();

				pr.setStartYear((int) val_peek(21).dval);
				pr.setStartMonth((int) val_peek(19).dval);
				pr.setStartDay((int) val_peek(17).dval);
				pr.setStartHour((int) val_peek(16).dval);
				pr.setStartMinute((int) val_peek(14).dval);
				pr.setStartSecond((int) val_peek(12).dval);

				pr.setEndYear((int) val_peek(9).dval);
				pr.setEndMonth((int) val_peek(7).dval);
				pr.setEndDay((int) val_peek(5).dval);
				pr.setEndHour((int) val_peek(4).dval);
				pr.setEndMinute((int) val_peek(2).dval);
				pr.setEndSecond((int) val_peek(0).dval);

				yyval.obj = pr;
			}
				break;

			// ######## CASE 4 ########
			case 4:
			// #line 66 "timeresouce.y"
			{
				TimeResource pr = new TimeResource();
				pr.setTimeFlag(1);

				pr.setStartYear((int) val_peek(25).dval);
				pr.setStartMonth((int) val_peek(23).dval);
				pr.setStartWeek((int) val_peek(21).dval);
				pr.setStartDay((int) val_peek(19).dval);
				pr.setStartHour((int) val_peek(18).dval);
				pr.setStartMinute((int) val_peek(16).dval);
				pr.setStartSecond((int) val_peek(14).dval);

				pr.setEndYear((int) val_peek(11).dval);
				pr.setEndMonth((int) val_peek(9).dval);
				pr.setEndWeek((int) val_peek(7).dval);
				pr.setEndDay((int) val_peek(5).dval);
				pr.setEndHour((int) val_peek(4).dval);
				pr.setEndMinute((int) val_peek(2).dval);
				pr.setEndSecond((int) val_peek(0).dval);

				yyval.obj = pr;
			}
				break;

			// ######## CASE 5 ########
			case 5:
			// #line 90 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 6 ########
			case 6:
			// #line 94 "timeresouce.y"
			{
				/* $$.sval = " [*!"+$2.sval+"*] "; */
				((TimeResource) val_peek(0).obj).setLogicFlag(1);
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 7 ########
			case 7:
			// #line 101 "timeresouce.y"
			{
				/* System.out.println($1.sval+",,,"+$3.sval); */
				/* $$.sval=" [*"+$1.sval+" , "+$3.sval+"*] "; */

				TimeResource pr = (TimeResource) val_peek(2).obj;
				if (pr.getSegments() == null) {
					List<TimeResource> segments = new ArrayList<TimeResource>();
					segments.add(pr);
					segments.add((TimeResource) val_peek(0).obj);
					TimeResource newPr = new TimeResource();
					newPr.setSegments(segments);
					yyval.obj = newPr;
				} else {
					pr.getSegments().add((TimeResource) val_peek(0).obj);
					yyval.obj = pr;
				}

			}
				break;

			// ######## CASE 8 ########
			case 8:
			// #line 119 "timeresouce.y"
			{
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 9 ########
			case 9:
			// #line 124 "timeresouce.y"
			{
				/* System.out.println("((("+$2.sval+")))"); */
				/* $$.sval=" [*( "+$2.sval+" )*] "; */
				yyval.obj = val_peek(1).obj;
			}
				break;

			// ######## CASE 10 ########
			case 10:
			// #line 131 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 11 ########
			case 11:
			// #line 135 "timeresouce.y"
			{
				/* $$.sval = $1.sval; */
				yyval.obj = val_peek(0).obj;
			}
				break;

			// ######## CASE 12 ########
			case 12:
			// #line 139 "timeresouce.y"
			{
				/* $$.sval = " [*!"+$2.sval+"*] "; */
				((TimeResource) val_peek(0).obj).setLogicFlag(1);
				yyval.obj = val_peek(0).obj;
			}
				break;

			// #line 1017 "TimeParser.java"
			// ########## END OF USER-SUPPLIED ACTIONS ##########
			}// switch
				// #### Now let's reduce... ####

			if (yym == 0) {
				yyval = new ParserVal(-1);
			}
			root.setNodeValue(yyval); // set left nonterminal node value
			yyval.setSyntaxNode(root); // used for transfering the pointer of
										// root to the next reduction

			if (yydebug)
				yypdebug("reduce");
			state_drop(yym); // we just reduced yylen states
			yystate = state_peek(0); // get new state
			val_drop(yym); // corresponding value drop
			yym = yylhs[yyn]; // select next TERMINAL(on lhs)
			if (yystate == 0 && yym == 0)// done? 'rest' state and at first
											// TERMINAL
			{
				yypdebug("After reduction, shifting from state 0 to state "
						+ YYFINAL + "");
				yystate = YYFINAL; // explicitly say we're done
				state_push(YYFINAL); // and save it
				val_push(yyval); // also save the semantic value of parsing
				if (yychar < 0) // we want another character?
				{
					yychar = yylex(); // get next character
					if (yychar < 0)
						yychar = 0; // clean, if necessary
					if (yydebug)
						yylexdebug(yystate, yychar);
				}
				if (yychar == 0) // Good exit (if lex returns 0 ;-)
					break; // quit the loop--all DONE
			}// if yystate
			else // else not done yet
			{ // get next state and push, for next yydefred[]
				yyn = yygindex[yym]; // find out where to go
				if ((yyn != 0) && (yyn += yystate) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yystate)
					yystate = yytable[yyn]; // get new state
				else
					yystate = yydgoto[yym]; // else go to new defred
				yypdebug("after reduction, shifting from state "
						+ state_peek(0) + " to state " + yystate + "");
				state_push(yystate); // going again, so push state & val...
				val_push(yyval); // for next action
			}
		}// main loop
		return root;// yyaccept!!
	}

	// ## end of method yytree() ######################################

	// ## run() --- for Thread #######################################
	/**
	 * A default run method, used for operating this TimeParser object in the
	 * background. It is intended for extending Thread or implementing Runnable.
	 * Turn off with -Jnorun .
	 */
	public void run() {
		yyparse();
	}

	// ## end of method run() ########################################

	// ## Constructors ###############################################
	/**
	 * Default constructor. Turn off with -Jnoconstruct .
	 */
	public TimeParser() {
		// nothing to do
	}

	/**
	 * Create a TimeParser, setting the debug to true or false.
	 * 
	 * @param debugMe
	 *            true for debugging, false for no debug.
	 */
	public TimeParser(boolean debugMe) {
		yydebug = debugMe;
	}
	// ###############################################################

}

// ###################################################################################
// ### E N D O F F I L E
// ###################################################################################
