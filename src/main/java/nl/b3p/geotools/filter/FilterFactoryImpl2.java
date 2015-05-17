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
 *
 *
 * Created on 24 October 2002, 16:16
 */


import org.geotools.factory.Hints;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.FunctionFinder;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;



/**
 * Implementation of the FilterFactory, generates the filter implementations in
 * defaultcore.
 *
 * @author Ian Turton, CCG
 * @source $URL: http://svn.geotools.org/tags/2.5.2/modules/library/main/src/main/java/org/geotools/filter/FilterFactoryImpl.java $
 * @version $Id: FilterFactoryImpl.java 30746 2008-06-17 03:58:48Z jgarnett $
 */
public class FilterFactoryImpl2 extends FilterFactoryImpl {
    
    private FunctionFinder functionFinder;

    /**
     * Creates a new instance of FilterFactoryImpl
     */
    public FilterFactoryImpl2() {
        this( null );
    }
    public FilterFactoryImpl2( Hints hints ){
        super(hints);
    }  

    public PropertyIsLike like(Expression expr1, Expression expr2) {
        return like(expr1,expr2,true);
    }
    public PropertyIsLike like(Expression expr1, Expression expr2, boolean matchCase) {
        return like(expr1,expr2,"*","?","\\",matchCase);
    }
    public PropertyIsLike like(Expression expr1, Expression expr2,
            String wildcard, String singleChar, String escape) {
        return like(expr1,expr2,wildcard,singleChar,escape,true);
    }
    public PropertyIsLike like(Expression expr1, Expression expr2,
            String wildcard, String singleChar, String escape, boolean matchCase) {

        LikeFilterImpl2 filter = new LikeFilterImpl2(this,expr1,expr2,matchCase);
        filter.setSingleChar(singleChar);
        filter.setEscape(escape);
        filter.setWildCard(wildcard);

        return filter;
    }

    @Override
    public PropertyIsLike like(Expression expr, String pattern) {
        return like(expr,pattern,"*","?","\\");
    }

    @Override
    public PropertyIsLike like(Expression expr, String pattern,
            String wildcard, String singleChar, String escape) {
        return like(expr,this.literal(pattern),wildcard,singleChar,escape);
    }
}
