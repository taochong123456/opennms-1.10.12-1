/*
 * @(#)ExtendedStreamTokenizer.java	1.37 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * @author zhuzhen   [extended version]
 */

package org.opennms.netmgt.syslogd.analyze;

import java.io.Reader;
import java.io.IOException;
import java.io.*;

/**
 * The <code>LexParser</code> class takes an input stream and
 * parses it into "tokens", allowing the tokens to be
 * read one at a time. The parsing process is controlled by a table
 * and a number of flags that can be set to various states. The
 * stream tokenizer can recognize identifiers, numbers, quoted
 * strings, and various comment styles.
 * <p>
 * Each byte read from the input stream is regarded as a character
 * in the range <code>'&#92;u0000'</code> through <code>'&#92;u00FF'</code>.
 * The character value is used to look up five possible attributes of
 * the character: <i>white space</i>, <i>alphabetic</i>,
 * <i>numeric</i>, <i>string quote</i>, and <i>comment character</i>.
 * Each character can have zero or more of these attributes.
 * <p>
 * In addition, an instance has four flags. These flags indicate:
 * <ul>
 * <li>Whether line terminators are to be returned as tokens or treated
 *     as white space that merely separates tokens.
 * <li>Whether C-style comments are to be recognized and skipped.
 * <li>Whether C++-style comments are to be recognized and skipped.
 * <li>Whether the characters of identifiers are converted to lowercase.
 * </ul>
 * <p>
 * A typical application first constructs an instance of this class,
 * sets up the syntax tables, and then repeatedly loops calling the
 * <code>nextToken</code> method in each iteration of the loop until
 * it returns the value <code>TT_EOF</code>.
 *
 * @author  James Gosling , < extended veriosn, including scientific counting grammars > zhu zhen
 * @version 1.37, 12/03/01
 * @see     org.GreatWall.parser.LexParser#nextToken()
 * @see     org.GreatWall.parser.LexParser#TT_EOF

 */

public class LexParser {

  private Reader reader = null;

  private char buf[] = new char[32]; //modified,original is 20

  /**
   * The next character to be considered by the nextToken method.  May also
   * be NEED_CHAR to indicate that a new character should be read, or SKIP_LF
   * to indicate that a new character should be read and, if it is a '\n'
   * character, it should be discarded and a second new character should be
   * read.
   */
  private int peekc = NEED_CHAR; //to make a decision  whether read() be called when calling nextToken()

  private static final int NEED_CHAR = Integer.MAX_VALUE;
  private static final int SKIP_LF = Integer.MAX_VALUE - 1;

  private boolean pushedBack;
  private boolean forceLower;
  private boolean forceUpper;

  /** The line number of the last token read */
  private int LINENO = 1;
  public int getLINENO() {
    return LINENO;
  }

  /** The column number of current reading char */
  private int COLNO = 0;
  public int getCOLNO() {
    return COLNO;
  }

  private boolean eolIsSignificantP = false;
  private boolean slashSlashCommentsP = false;
  private boolean slashStarCommentsP = false;

  private byte ctype[] = new byte[256]; //bit_map index
  private static final byte CT_WHITESPACE = 1; //00000001
  private static final byte CT_DIGIT = 2; //00000010
  private static final byte CT_ALPHA = 4; //00000100
  private static final byte CT_QUOTE = 8; //00001000
  private static final byte CT_COMMENT = 16; //00010000

  /**
   * After a call to the <code>nextToken</code> method, this field
   * contains the type of the token just read. For a single character
   * token, its value is the single character, converted to an integer.
   * For a quoted string token (see , its value is the quote character.
   * Otherwise, its value is one of the following:
   * <ul>
   * <li><code>TT_WORD</code> indicates that the token is a word.
   * <li><code>TT_NUMBER</code> indicates that the token is a number.
   * <li><code>TT_EOL</code> indicates that the end of line has been read.
   *     The field can only have this value if the
   *     <code>eolIsSignificant</code> method has been called with the
   *     argument <code>true</code>.
   * <li><code>TT_EOF</code> indicates that the end of the input stream
   *     has been reached.
   * </ul>
   * <p>
   * The initial value of this field is -4.
   *
   * @see     org.GreatWall.parser.LexParser#eolIsSignificant(boolean)
   * @see     org.GreatWall.parser.LexParser#nextToken()
   * @see     org.GreatWall.parser.LexParser#quoteChar(int)
   * @see     org.GreatWall.parser.LexParser#TT_EOF
   * @see     org.GreatWall.parser.LexParser#TT_EOL
   * @see     org.GreatWall.parser.LexParser#TT_NUMBER
   * @see     org.GreatWall.parser.LexParser#TT_WORD
   */
  public int ttype = NONE;

  /**
   * A constant indicating that the end of the stream has been read.
   */
  public static final int EOF = 0;

  /**
   * A constant indicating that the end of the line has been read.
   */
  public static final int EOL = '\n';

  //modified >>>>begin
  /**
   * EOF,EOL,ERR,NUM,WR,NONE,NE,LE,GE,QU   Ϊ���ʷ�������֧�ֵ�token,ȡֵС��300
   */
  /**
   * Wrong state,such as not matched quote etc. At the time ,
   * sval is the description of that error message.
   */
  public static final int ERR = -1;

  /**
   * A constant indicating that a number token has been read.
   */
  public static final int NUM = 257;

  /**
   * A constant indicating that a word token has been read.
   */
  public static final int WR = 258;

  /* A constant indicating that no token has been read, used for
   * initializing ttype.  FIXME This could be made public and
   * made available as the part of the API in a future release.
   */
  public static final int NONE = 259;

  public static final int NE = 260; // '!='  OR '<>'
  public static final int LE = 261; // '<='
  public static final int GE = 262; // '>='
  public static final int QU = 263; //quote value

  //modified <<<<end


  /**
   * If the current token is a word token, this field contains a
   * string giving the characters of the word token. When the current
   * token is a quoted string token, this field contains the body of
   * the string.
   * <p>
   * The current token is a word when the value of the
   * <code>ttype</code> field is <code>WR</code>. The current token is
   * a quoted string token when the value of the <code>ttype</code> field is
   * a quote character.
   * <p>
   * The initial value of this field is null.
   *
   * @see     org.GreatWall.parser.LexParser#quoteChar(int)
   * @see     org.GreatWall.parser.LexParser#WR
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public String sval;

  /**
   * If the current token is a number, this field contains the value
   * of that number. The current token is a number when the value of
   * the <code>ttype</code> field is <code>NUM</code>.
   * <p>
   * The initial value of this field is 0.0.
   *
   * @see     org.GreatWall.parser.LexParser#NUM
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public double nval;

  /** Private constructor that initializes everything except the streams. */
  private LexParser() {

    wordChars('a', 'z');
    wordChars('A', 'Z');
    wordChars('_', '_');
    wordChars(128 + 32, 255);

    whitespaceChars(0, ' ');
    //commentChar('/');

    quoteChar('"');
    quoteChar('\'');

    parseNumbers();

    //eolIsSignificant(true);

    //ignore upper or lower case.
    lowerCaseMode(true);

  }

  /**
   * Create a tokenizer that parses the given character stream.
   *
   * @param r  a Reader object providing the input stream.
   * @since   JDK1.1
   */
  public LexParser(Reader r) {
    this();
    if (r == null) {
      throw new NullPointerException();
    }
    reader = r;
  }

  /**
   * Resets this tokenizer's syntax table so that all characters are
   * "ordinary." See the <code>ordinaryChar</code> method
   * for more information on a character being ordinary.
   *
   * @see     org.GreatWall.parser.LexParser#ordinaryChar(int)
   */
  public void resetSyntax() {
    for (int i = ctype.length; --i >= 0; ) {
      ctype[i] = 0;
    }
  }

  /**
   * Specifies that all characters <i>c</i> in the range
   * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
   * are word constituents. A word token consists of a word constituent
   * followed by zero or more word constituents or number constituents.
   *
   * @param   low   the low end of the range.
   * @param   hi    the high end of the range.
   */
  public void wordChars(int low, int hi) {
    if (low < 0) {
      low = 0;
    }
    if (hi >= ctype.length) {
      hi = ctype.length - 1;
    }
    while (low <= hi) {
      ctype[low++] |= CT_ALPHA;
    }
  }

  /**
   * Specifies that all characters <i>c</i> in the range
   * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
   * are white space characters. White space characters serve only to
   * separate tokens in the input stream.
   *
   * @param   low   the low end of the range.
   * @param   hi    the high end of the range.
   */
  public void whitespaceChars(int low, int hi) {
    if (low < 0) {
      low = 0;
    }
    if (hi >= ctype.length) {
      hi = ctype.length - 1;
    }
    while (low <= hi) {
      ctype[low++] = CT_WHITESPACE;
    }
  }

  /**
   * Specifies that all characters <i>c</i> in the range
   * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
   * are "ordinary" in this tokenizer. See the
   * <code>ordinaryChar</code> method for more information on a
   * character being ordinary.
   *
   * @param   low   the low end of the range.
   * @param   hi    the high end of the range.
   * @see     org.GreatWall.parser.LexParser#ordinaryChar(int)
   */
  public void ordinaryChars(int low, int hi) {
    if (low < 0) {
      low = 0;
    }
    if (hi >= ctype.length) {
      hi = ctype.length - 1;
    }
    while (low <= hi) {
      ctype[low++] = 0;
    }
  }

  /**
   * Specifies that the character argument is "ordinary"
   * in this tokenizer. It removes any special significance the
   * character has as a comment character, word component, string
   * delimiter, white space, or number character. When such a character
   * is encountered by the parser, the parser treates it as a
   * single-character token and sets <code>ttype</code> field to the
   * character value.
   *
   * @param   ch   the character.
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public void ordinaryChar(int ch) {
    if (ch >= 0 && ch < ctype.length) {
      ctype[ch] = 0;
    }
  }

  /**
   * Specified that the character argument starts a single-line
   * comment. All characters from the comment character to the end of
   * the line are ignored by this stream tokenizer.
   *
   * @param   ch   the character.
   */
  public void commentChar(int ch) {
    if (ch >= 0 && ch < ctype.length) {
      ctype[ch] = CT_COMMENT;
    }
  }

  /**
   * Specifies that matching pairs of this character delimit string
   * constants in this tokenizer.
   * <p>
   * When the <code>nextToken</code> method encounters a string
   * constant, the <code>ttype</code> field is set to the string
   * delimiter and the <code>sval</code> field is set to the body of
   * the string.
   * <p>
   * If a string quote character is encountered, then a string is
   * recognized, consisting of all characters after (but not including)
   * the string quote character, up to (but not including) the next
   * occurrence of that same string quote character, or a line
   * terminator, or end of file. The usual escape sequences such as
   * <code>"&#92;n"</code> and <code>"&#92;t"</code> are recognized and
   * converted to single characters as the string is parsed.
   *
   * @param   ch   the character.
   * @see     org.GreatWall.parser.LexParser#nextToken()
   * @see     org.GreatWall.parser.LexParser#sval
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public void quoteChar(int ch) {
    if (ch >= 0 && ch < ctype.length) {
      ctype[ch] = CT_QUOTE;
    }
  }

  /**
   * Specifies that numbers should be parsed by this tokenizer. The
   * syntax table of this tokenizer is modified so that each of the twelve
   * characters:
   * <blockquote><pre>
   *      0 1 2 3 4 5 6 7 8 9 . -
   * </pre></blockquote>
   * <p>
   * has the "numeric" attribute.
   * <p>
   * When the parser encounters a word token that has the format of a
   * double precision floating-point number, it treats the token as a
   * number rather than a word, by setting the the <code>ttype</code>
   * field to the value <code>NUM</code> and putting the numeric
   * value of the token into the <code>nval</code> field.
   *
   * @see     org.GreatWall.parser.LexParser#nval
   * @see     org.GreatWall.parser.LexParser#NUM
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public void parseNumbers() {
    for (int i = '0'; i <= '9'; i++) {
      ctype[i] |= CT_DIGIT;
    }
    ctype['.'] |= CT_DIGIT;
    ctype['-'] |= CT_DIGIT;
  }

  /**
   * Determines whether or not ends of line are treated as tokens.
   * If the flag argument is true, this tokenizer treats end of lines
   * as tokens; the <code>nextToken</code> method returns
   * <code>EOL</code> and also sets the <code>ttype</code> field to
   * this value when an end of line is read.
   * <p>
   * A line is a sequence of characters ending with either a
   * carriage-return character (<code>'&#92;r'</code>) or a newline
   * character (<code>'&#92;n'</code>). In addition, a carriage-return
   * character followed immediately by a newline character is treated
   * as a single end-of-line token.
   * <p>
   * If the <code>flag</code> is false, end-of-line characters are
   * treated as white space and serve only to separate tokens.
   *
   * @param   flag   <code>true</code> indicates that end-of-line characters
   *                 are separate tokens; <code>false</code> indicates that
   *                 end-of-line characters are white space.
   * @see     org.GreatWall.parser.LexParser#nextToken()
   * @see     org.GreatWall.parser.LexParser#ttype
   * @see     org.GreatWall.parser.LexParser#EOL
   */
  public void eolIsSignificant(boolean flag) {
    eolIsSignificantP = flag;
  }

  /**
   * Determines whether or not the tokenizer recognizes C-style comments.
   * If the flag argument is <code>true</code>, this stream tokenizer
   * recognizes C-style comments. All text between successive
   * occurrences of <code>/*</code> and <code>*&#47;</code> are discarded.
    * <p>
    * If the flag argument is <code>false</code>, then C-style comments
    * are not treated specially.
    *
    * @param   flag   <code>true</code> indicates to recognize and ignore
    *                 C-style comments.
    */
   public void slashStarComments(boolean flag) {
     slashStarCommentsP = flag;
   }

  /**
   * Determines whether or not the tokenizer recognizes C++-style comments.
   * If the flag argument is <code>true</code>, this stream tokenizer
   * recognizes C++-style comments. Any occurrence of two consecutive
   * slash characters (<code>'/'</code>) is treated as the beginning of
   * a comment that extends to the end of the line.
   * <p>
   * If the flag argument is <code>false</code>, then C++-style
   * comments are not treated specially.
   *
   * @param   flag   <code>true</code> indicates to recognize and ignore
   *                 C++-style comments.
   */
  public void slashSlashComments(boolean flag) {
    slashSlashCommentsP = flag;
  }

  /**
   * Determines whether or not word token are automatically lowercased.
   * If the flag argument is <code>true</code>, then the value in the
   * <code>sval</code> field is lowercased whenever a word token is
   * returned (the <code>ttype</code> field has the
   * value <code>WR</code> by the <code>nextToken</code> method
   * of this tokenizer.
   * <p>
   * If the flag argument is <code>false</code>, then the
   * <code>sval</code> field is not modified.
   *
   * @param   fl   <code>true</code> indicates that all word tokens should
   *               be lowercased.
   * @see     org.GreatWall.parser.LexParser#nextToken()
   * @see     org.GreatWall.parser.LexParser#ttype
   * @see     org.GreatWall.parser.LexParser#WR
   */
  public void lowerCaseMode(boolean fl) {
    forceLower = fl;
  }

  public void upperCaseMode(boolean fl) {
    forceUpper = fl;
  }

  /** Read the next character */
  private int read() throws IOException {
    if (reader != null) {
      COLNO++;
      return reader.read();
    }
    else {
      throw new IllegalStateException();
    }
  }

  /**
   * Parses the next token from the input stream of this tokenizer.
   * The type of the next token is returned in the <code>ttype</code>
   * field. Additional information about the token may be in the
   * <code>nval</code> field or the <code>sval</code> field of this
   * tokenizer.
   * <p>
   * Typical clients of this
   * class first set up the syntax tables and then sit in a loop
   * calling nextToken to parse successive tokens until EOF
   * is returned.
   *
   * @return     the value of the <code>ttype</code> field.
   * @exception  IOException  if an I/O error occurs.
   * @see        org.GreatWall.parser.LexParser#nval
   * @see        org.GreatWall.parser.LexParser#sval
   * @see        org.GreatWall.parser.LexParser#ttype
   */
  public int nextToken() throws IOException {
    if (pushedBack) {
      pushedBack = false;
      return ttype;
    }
    byte ct[] = ctype;
    sval = null;
    nval = 0;

    int c = peekc;
    if (c < 0) {
      c = NEED_CHAR;
    }
    if (c == SKIP_LF) {
      c = read();
      if (c < 0) {
        return ttype = EOF;
      }
      if (c == '\n') {
        c = NEED_CHAR;

      }
    }
    if (c == NEED_CHAR) {
      c = read();
      if (c < 0) {
        return ttype = EOF;
      }
    }
    ttype = c; /* Just to be safe */

    /* Set peekc so that the next invocation of nextToken will read
     * another character unless peekc is reset in this invocation
     */
    peekc = NEED_CHAR;

    int ctype = c < 256 ? ct[c] : CT_ALPHA;
    while ( (ctype & CT_WHITESPACE) != 0) {
      if (c == '\r') {
        LINENO++;
        COLNO = 0;
        if (eolIsSignificantP) {
          peekc = SKIP_LF;
          return ttype = EOL;
        }
        c = read();
        if (c == '\n') {
          c = read();
        }
      }
      else {
        if (c == '\n') {
          LINENO++;
          COLNO = 0;
          if (eolIsSignificantP) {
            return ttype = EOL;
          }
        }
        c = read();
      }
      if (c < 0) {
        return ttype = EOF;
      }
      ctype = c < 256 ? ct[c] : CT_ALPHA;
    }

    if ( (ctype & CT_DIGIT) != 0) {
      boolean neg = false;
      if (c == '-') {
        //c = read();                              //ע�͵����
        //if (c != '.' && (c < '0' || c > '9')) {  //ע�͵����
        //  peekc = c;                             //ע�͵����

          //modified >>>>begin

          sval = "-";
          return ttype = '-';
          //modified <<<<end

       // }                                         //ע�͵����
       // neg = true;                               //ע�͵����  ���������qε�����ж�
      }
      double v = 0;
      int decexp = 0;
      int seendot = 0;
      //modified >>>>begin
      boolean scUsed = false; //whether using scientific counting.
      double scexp = 1; //scientific counting exponent result.
      //modified <<<<end

      while (true) {
        if (c == '.' && seendot == 0) {
          seendot = 1;
        }
        else if ('0' <= c && c <= '9') {
          v = v * 10 + (c - '0');
          decexp += seendot;
        }
        //modified >>>>begin
        else if (c == 'e' || c == 'E') {
          scUsed = true;
          boolean positiveExp = true;
          double v2 = 0;
          int c2 = read();
          if (c2 == '+') {
          }
          else if (c2 == '-') {
            positiveExp = false;
          }
          else if ('0' <= c2 && c2 <= '9') {
            v2 = v2 * 10 + (c2 - '0');
          }
          else {
            peekc = c2;
            sval = "invalid number exp format at line,column:[" + LINENO + "," +
                COLNO + "]";
            return ttype = ERR;
          }
          int c3 = read();
          while ('0' <= c3 && c3 <= '9') {
            v2 = v2 * 10 + (c3 - '0');
            c3 = read();
          }

          peekc = c3;
          if (c3 == '.') {
            sval = "invalid  exp format line,column:[" + LINENO + "," + COLNO +
                "]";
            return ttype = ERR;
          }
          else {
            double exp = 1;
            for (int i = 0; i < v2; i++) {
              exp *= 10;
            }
            scexp = positiveExp ? exp : 1 / exp;
          }

          break;
          //modified <<<<end
        }
        else {
          break;
        }
        c = read();
      }

      if (scUsed == false) { //modified
        peekc = c;

      }
      if (decexp != 0) {
        double denom = 10;
        decexp--;
        while (decexp > 0) {
          denom *= 10;
          decexp--;
        }
        /* Do one division of a likely-to-be-more-accurate number */
        v = v / denom;
      }
      nval = neg ? -v : v;
      nval *= scexp; //modified
      //modified >>>>begin
      if (c == '.') {

        sval = "invalid number format! at line,column:[" + LINENO + "," + COLNO +
            "]";
        return ttype = ERR;
      }
      //modified <<<<end
      return ttype = NUM;
    }

    if ( (ctype & CT_ALPHA) != 0) {
      int i = 0;
      do {
        if (i >= buf.length) {
          char nb[] = new char[buf.length * 2];
          System.arraycopy(buf, 0, nb, 0, buf.length);
          buf = nb;
        }
        buf[i++] = (char) c;
        c = read();
        ctype = c < 0 ? CT_WHITESPACE : c < 256 ? ct[c] : CT_ALPHA;
      }
      while ( (ctype & (CT_ALPHA | CT_DIGIT)) != 0);
      peekc = c;
      sval = String.copyValueOf(buf, 0, i);
      if (forceLower) {
        sval = sval.toLowerCase();
      }
      if (forceUpper) {
        sval = sval.toUpperCase();

      }
      return ttype = WR;
    }

    if ( (ctype & CT_QUOTE) != 0) {
      ttype = c;
      int i = 0;
      /* Invariants (because \Octal needs a lookahead):
       *   (i)  c contains char value
       *   (ii) d contains the lookahead
       */
      int d = read();
      while (d >= 0 && d != ttype && d != '\n' && d != '\r') {
        if (d == '\\') {
          c = read();
          int first = c; /* To allow \377, but not \477 */
          if (c >= '0' && c <= '7') {
            c = c - '0';
            int c2 = read();
            if ('0' <= c2 && c2 <= '7') {
              c = (c << 3) + (c2 - '0');
              c2 = read();
              if ('0' <= c2 && c2 <= '7' && first <= '3') {
                c = (c << 3) + (c2 - '0');
                d = read();
              }
              else {
                d = c2;
              }
            }
            else {
              d = c2;
            }
          }
          else {
            switch (c) {
              case 'a':
                c = 0x7;
                break;
              case 'b':
                c = '\b';
                break;
              case 'f':
                c = 0xC;
                break;
              case 'n':
                c = '\n';
                break;
              case 'r':
                c = '\r';
                break;
              case 't':
                c = '\t';
                break;
              case 'v':
                c = 0xB;
                break;
            }
            d = read();
          }
        }
        else {
          c = d;
          d = read();
        }
        if (i >= buf.length) {
          char nb[] = new char[buf.length * 2];
          System.arraycopy(buf, 0, nb, 0, buf.length);
          buf = nb;
        }
        buf[i++] = (char) c;
      }

      /* If we broke out of the loop because we found a matching quote
       * character then arrange to read a new character next time
       * around; otherwise, save the character.
       */


      //peekc = (d == ttype) ? NEED_CHAR : d;
      //sval = String.copyValueOf(buf, 0, i);
      //return ttype;

      if (d == ttype) {
        peekc = NEED_CHAR;
        sval = String.copyValueOf(buf, 0, i);
        //modified >>>>begin
        ttype = QU;
        //modified <<<<end
        return ttype;
      }
      else {
        peekc = d;
        sval = "quote is not matched at line,column:[" + LINENO + "," + COLNO +
            "]";
        ttype = ERR;
        return ttype;
      }
    }

    if (c == '/' && (slashSlashCommentsP || slashStarCommentsP)) {
      c = read();
      if (c == '*' && slashStarCommentsP) {
        int prevc = 0;
        while ( (c = read()) != '/' || prevc != '*') {
          if (c == '\r') {
            LINENO++;
            c = read();
            if (c == '\n') {
              c = read();
            }
          }
          else {
            if (c == '\n') {
              LINENO++;
              c = read();
            }
          }
          if (c < 0) {
            return ttype = EOF;
          }
          prevc = c;
        }
        return nextToken();
      }
      else if (c == '/' && slashSlashCommentsP) {
        while ( (c = read()) != '\n' && c != '\r' && c >= 0) {
          ;
        }
        peekc = c;
        return nextToken();
      }
      else {
        /* Now see if it is still a single line comment */
        if ( (ct['/'] & CT_COMMENT) != 0) {
          while ( (c = read()) != '\n' && c != '\r' && c >= 0) {
            ;
          }
          peekc = c;
          return nextToken();
        }
        else {
          peekc = c;
          //modified >>>>begin
          //return ttype = '/';
          sval = "/";
          return ttype = '/';
          //modified <<<<end

        }
      }
    }

    if ( (ctype & CT_COMMENT) != 0) {
      while ( (c = read()) != '\n' && c != '\r' && c >= 0) {
        ;
      }
      peekc = c;
      return nextToken();
    }
    //modified >>>>begin
    //other tokens that consist of non-word symbols.
    switch (c) {
      case '<':
        c = read();
        if (c == '=') {
          sval = "<=";
          return ttype = LE;
        }
        if (c == '>') {
          sval = "<>";
          return ttype = NE;
        }

        sval = "<";
        peekc = c;
        return ttype = '<';

      case '>':
        c = read();
        if (c == '=') {
          sval = ">=";
          return ttype = GE;
        }
        else {
          sval = ">";
          peekc = c;
          return ttype = '>';
        }
      case '!':
        c = read();
        if (c == '=') {
          sval = "!=";
          return ttype = NE;
        }
        else {
          peekc = c;
          sval = "!";
          return ttype = '!';
        }
        /*
            case '':
         sval="";
         return ttype=;
            case '':
         sval="";
         return ttype=;
            case '':
         sval="";
         return ttype=;
         */
    }

    sval = String.valueOf( (char) c);
    //modified <<<<end

    return ttype = c;
  }

  /**
   * Causes the next call to the <code>nextToken</code> method of this
   * tokenizer to return the current value in the <code>ttype</code>
   * field, and not to modify the value in the <code>nval</code> or
   * <code>sval</code> field.
   *
   * @see     org.GreatWall.parser.LexParser#nextToken()
   * @see     org.GreatWall.parser.LexParser#nval
   * @see     org.GreatWall.parser.LexParser#sval
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public void pushBack() {
    if (ttype != NONE) /* No-op if nextToken() not called */
        {
      pushedBack = true;
    }
  }

  /**
   * Return the current line number.
   *
   * @return  the current line number of this stream tokenizer.
   */
  public int lineno() {
    return LINENO;
  }

  /**
   * Returns the string representation of the current stream token.
   *
   * @return  a string representation of the token specified by the
   *          <code>ttype</code>, <code>nval</code>, and <code>sval</code>
   *          fields.
   * @see     org.GreatWall.parser.LexParser#nval
   * @see     org.GreatWall.parser.LexParser#sval
   * @see     org.GreatWall.parser.LexParser#ttype
   */
  public String toString() {
    String ret;
    switch (ttype) {
      case EOF:
        ret = "EOF";
        break;
      case EOL:
        ret = "EOL";
        break;
      case WR:
        ret = sval;
        break;
      case NUM:
        ret = "n=" + nval;
        break;
      case NONE:
        ret = "NOTHING";
        break;
      default: {
        /*
         * ttype is the first character of either a quoted string or
         * is an ordinary character. ttype can definitely not be less
         * than 0, since those are reserved values used in the previous
         * case statements
         */
        if (ttype < 256 &&
            ( (ctype[ttype] & CT_QUOTE) != 0)) {
          ret = sval;
          break;
        }

        char s[] = new char[3];
        s[0] = s[2] = '\'';
        s[1] = (char) ttype;
        ret = new String(s);
        break;
      }
    }
    return "Token[" + ret + "], line " + LINENO;
  }

  public static void main(String[] args) {
    try { //ʹ��ʾ��
      //LexParser myLP = new LexParser(new FileReader(new File("test.txt"))); //���������
      LexParser myLP = new LexParser(new StringReader(" \"2 + 4\",,,")); //���������
      int tType = -1;
      while ( (tType = myLP.nextToken()) != LexParser.EOF) { //ȡ��token
        System.out.print("tType:" + tType + "   ");
        switch (tType) {
          case WR:
            System.out.println(myLP.sval);
            break;
          case NUM:
            System.out.println(myLP.nval);
            break;
          case NE:
            System.out.println(myLP.sval);
            break;
          case LE:
            System.out.println(myLP.sval);
            break;
          case GE:
            System.out.println(myLP.sval);
            break;
          case QU:
            System.out.println("�������:" + myLP.sval);
            break;
          case EOL:
            System.out.println("���з�");
            break;
          case EOF:
            System.out.println("��ֹ��");
            break;
          case ERR:
            System.out.println("�������");
            break;
          case NONE:
            System.out.println("None");
            break;
          default: //����ַ�
            System.out.println( (char) tType);
            break;
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

}
