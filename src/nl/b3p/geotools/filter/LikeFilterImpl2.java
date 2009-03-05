package nl.b3p.geotools.filter;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.filter.CompareFilterImpl;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.LikeFilterImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.filter.expression.ThisPropertyAccessorFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;


/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @source $URL: http://svn.geotools.org/tags/2.5.2/modules/library/main/src/main/java/org/geotools/filter/LikeFilterImpl.java $
 * @version $Id: LikeFilterImpl.java 30648 2008-06-12 19:22:35Z acuster $
 */
public class LikeFilterImpl2 extends CompareFilterImpl implements PropertyIsLike{

    /** The attribute value, which must be an attribute expression. */
    //private Expression attribute = null;

    /** The (limited) REGEXP pattern. */
    //private String pattern = null;

    /** The single wildcard for the REGEXP pattern. */
    private String wildcardSingle = ".?";

    /** The multiple wildcard for the REGEXP pattern. */
    private String wildcardMulti = ".*";

    /** The escape sequence for the REGEXP pattern. */
    private String escape = "\\";

    /** the pattern compiled into a java regex */
    private Pattern compPattern = null;

    /** The matcher to match patterns with. */
    private Matcher match = null;

    public String getExpressionLiteral(Expression expr){
        if (expr instanceof LiteralExpressionImpl){
            String s= (String) ((LiteralExpressionImpl)expr).getValue();
            return s;
        }
        return null;
    }
    public Expression getExpression2(){
        return expression2;
    }
	public void setWildCard(String wildCard) {
		this.wildcardMulti = wildCard;
		match = null;
	}

	public void setSingleChar(String singleChar) {
		this.wildcardSingle = singleChar;
		match = null;
	}

	public void setEscape(String escape) {
		this.escape = escape;
		match = null;
	}


    private Matcher getMatcher(){
        if(match == null){
            // protect the vars as this is moved code

            String pattern1 = new String(getExpressionLiteral(expression2));
            String wildcardMulti1 = new String(this.wildcardMulti);
            String wildcardSingle1 = new String(this.wildcardSingle);
            String escape1 = new String(this.escape);

//          The following things happen for both wildcards:
            //  (1) If a user-defined wildcard exists, replace with Java wildcard
            //  (2) If a user-defined escape exists, Java wildcard + user-escape
            //  Then, test for matching pattern and return result.
            char esc = escape1.charAt(0);
            //LOGGER.finer("wildcard " + wildcardMulti1 + " single " + wildcardSingle1);
            //LOGGER.finer("escape " + escape1 + " esc " + esc + " esc == \\ "
               // + (esc == '\\'));

            String escapedWildcardMulti = fixSpecials(wildcardMulti1);
            String escapedWildcardSingle = fixSpecials(wildcardSingle1);

            // escape any special chars which are not our wildcards
            StringBuffer tmp = new StringBuffer("");

            boolean escapedMode = false;

            for (int i = 0; i < pattern1.length(); i++) {
                char chr = pattern1.charAt(i);
               // LOGGER.finer("tmp = " + tmp + " looking at " + chr);

                if (pattern1.regionMatches(false, i, escape1, 0, escape1.length())) {
                    // skip the escape string
                  //  LOGGER.finer("escape ");
                    escapedMode = true;

                    i += escape1.length();
                    chr = pattern1.charAt(i);
                }

                if (pattern1.regionMatches(false, i, wildcardMulti1, 0,
                            wildcardMulti1.length())) { // replace with java wildcard
                    //LOGGER.finer("multi wildcard");

                    if (escapedMode) {
                        //LOGGER.finer("escaped ");
                        tmp.append(escapedWildcardMulti);
                    } else {
                        tmp.append(".*");
                    }

                    i += (wildcardMulti1.length() - 1);
                    escapedMode = false;

                    continue;
                }

                if (pattern1.regionMatches(false, i, wildcardSingle1, 0,
                            wildcardSingle1.length())) {
                    // replace with java single wild card
                    //LOGGER.finer("single wildcard");

                    if (escapedMode) {
                        //LOGGER.finer("escaped ");
                        tmp.append(escapedWildcardSingle);
                    } else {
                        // From the OpenGIS filter encoding spec,
                        // "the single singleChar character matches exactly one character"
                        tmp.append(".{1}");
                    }

                    i += (wildcardSingle1.length() - 1);
                    escapedMode = false;

                    continue;
                }

                if (isSpecial(chr)) {
                    //LOGGER.finer("special");
                    tmp.append(this.escape + chr);
                    escapedMode = false;

                    continue;
                }

                tmp.append(chr);
                escapedMode = false;
            }

            pattern1 = tmp.toString();
            //LOGGER.finer("final pattern " + pattern1);
            compPattern = java.util.regex.Pattern.compile(pattern1);
            match = compPattern.matcher("");
        }
        return match;
    }

    /**
     * Constructor which flags the operator as like.
     */
    protected LikeFilterImpl2(FilterFactory factory) {
        this(factory, null, null);
    }

    protected LikeFilterImpl2(FilterFactory factory, Expression expression1, Expression expression2) {
        this(factory, expression1, expression2, true);
    }

    protected LikeFilterImpl2(FilterFactory factory, Expression expression1, Expression expression2,
            boolean matchCase) {
        super(factory, expression1, expression2, matchCase);

        // backwards compat with old type system
        this.filterType = LIKE;
    }

    /**
     * Gets the expression for hte filter.
     * <p>
     * This method calls th deprecated {@link #getValue()} for backwards
     * compatability with subclasses.
     * </p>
     */
    public org.opengis.filter.expression.Expression getExpression() {
    	return expression1;
    }

    public void setExpression(org.opengis.filter.expression.Expression e) {
    	Expression attribute = (Expression)e;
    		this.expression1 = attribute;

    }
    /**
     * Returns the pattern.
     */
    public String getLiteral() {
        return getExpressionLiteral(expression2);
    }

    /**
     * Sets the pattern.
     */

    /**
      * Determines whether or not a given feature matches this pattern.
      *
      * @param feature Specified feature to examine.
      *
      * @return Flag confirming whether or not this feature is inside the
      *         filter.
      *
      * @task REVISIT: could the pattern be null such that a null = null?
      */
    public boolean evaluate(Object feature) {
    	//Checks to ensure that the attribute has been set
        if (expression1 == null) {
            return false;
        }
            // Note that this converts the attribute to a string
            //  for comparison.  Unlike the math or geometry filters, which
            //  require specific types to function correctly, this filter
            //  using the mandatory string representation in Java
            // Of course, this does not guarantee a meaningful result, but it
            //  does guarantee a valid result.
            //LOGGER.finest("pattern: " + pattern);
            //LOGGER.finest("string: " + attribute.getValue(feature));
            //return attribute.getValue(feature).toString().matches(pattern);
            Object value = expression1.evaluate(feature);

            if (null == value) {
                return false;
            }

            Matcher matcher = getMatcher();
            matcher.reset(expression1.evaluate(feature).toString());

            return matcher.matches();
    }

    /**
     * Return this filter as a string.
     *
     * @return String representation of this like filter.
     */
    @Override
    public String toString() {
        return "[ " + expression1.toString() + " is like " + expression2.toString() + " ]";
    }

    /**
     * Getter for property escape.
     *
     * @return Value of property escape.
     */
    public java.lang.String getEscape() {
        return escape;
    }

     /**
     * Getter for property wildcardMulti.
     *
     * @return Value of property wildcardMulti.
     *
     * @deprecated use {@link #getWildCard()}.
     *
     */
    public final String getWildcardMulti() {
        return wildcardMulti;
    }

    /**
     * <p>
     * THis method calls {@link #getWildcardMulti()} for subclass backwards
     * compatability.
     * </p>
     *
     * @see org.opengis.filter.PropertyIsLike#getWildCard().
     */
    public String getWildCard() {
    	return getWildcardMulti();
    }

    /**
     * Getter for property wildcardSingle.
     *
     * @return Value of property wildcardSingle.
     *
     * @deprecated use {@link #getSingleChar()}
     */
    public final String getWildcardSingle() {
        return wildcardSingle;
    }

    /**
     * <p>
     * THis method calls {@link #getWildcardSingle()()} for subclass backwards
     * compatability.
     * </p>
     *
     * @see org.opengis.filter.PropertyIsLike#getSingleChar()().
     */
    public String getSingleChar() {
    	return getWildcardSingle();
    }

    /**
     * convienience method to determine if a character is special to the regex
     * system.
     *
     * @param chr the character to test
     *
     * @return is the character a special character.
     */
    private boolean isSpecial(final char chr) {
        return ((chr == '.') || (chr == '?') || (chr == '*') || (chr == '^')
        || (chr == '$') || (chr == '+') || (chr == '[') || (chr == ']')
        || (chr == '(') || (chr == ')') || (chr == '|') || (chr == '\\')
        || (chr == '&'));
    }

    /**
     * convienience method to escape any character that is special to the regex
     * system.
     *
     * @param inString the string to fix
     *
     * @return the fixed string
     */
    private String fixSpecials(final String inString) {
        StringBuffer tmp = new StringBuffer("");

        for (int i = 0; i < inString.length(); i++) {
            char chr = inString.charAt(i);

            if (isSpecial(chr)) {
                tmp.append(this.escape + chr);
            } else {
                tmp.append(chr);
            }
        }

        return tmp.toString();
    }

    /**
     * Compares this filter to the specified object.  Returns true  if the
     * passed in object is the same as this filter.  Checks  to make sure the
     * filter types, the value, and the pattern are the same. &
     *
     * @param obj - the object to compare this LikeFilter against.
     *
     * @return true if specified object is equal to this filter; false
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof LikeFilterImpl) {
            LikeFilterImpl lFilter = (LikeFilterImpl) obj;

            //REVISIT: check for nulls.
            return ((lFilter.getFilterType() == this.filterType)
            && lFilter.getValue().equals(expression1)
            && lFilter.getPattern().equals(expression2));
        }
        return false;
    }

    /**
     * Override of hashCode method.
     *
     * @return the hash code for this like filter implementation.
     */
    public int hashCode() {
        int result = 17;
        result = (37 * result)
            + ((expression1 == null) ? 0 : expression1.hashCode());
        result = (37 * result) + ((expression2 == null) ? 0 : expression2.hashCode());

        return result;
    }

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
     public Object accept(FilterVisitor visitor, Object extraData) {
    	return visitor.visit(this,extraData);
    }
}