package net.bleujin.rcraken.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;

import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;
import net.bleujin.searcher.search.SearchConfig;
import net.bleujin.searcher.search.filter.FilterUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

public class Filters {

	public static Query eq(String field, Object value) {
		return new TermQuery(new Term(field, ObjectUtil.toString(value))) ;
	}

	public static Query ne(String field, String value) {
		final BooleanQuery inner = new BooleanQuery.Builder().add(new BooleanClause(new TermQuery(new Term(field, ObjectUtil.toString(value))), Occur.MUST_NOT)).build() ;

		return inner;
	}

	public static Query gte(String field, long min) {
		return NumericDocValuesField.newSlowRangeQuery(field, min, Long.MAX_VALUE) ;
	}

	public static Query gte(String field, String lowerTerm) {
		return new TermRangeQuery(field, new BytesRef(lowerTerm), null, true, false) ;
	}

	public static Query gt(String field, long min) {
		return NumericDocValuesField.newSlowRangeQuery(field, min+1, Long.MAX_VALUE) ;
	}

	public static Query wildcard(String field, Object value) {
		return new WildcardQuery(new Term(field, ObjectUtil.toString(value))) ;
	}

	public static Query query(QueryParser parser, String query) throws ParseException {
		return parser.parse(query) ;
	}

	public static Query lte(String field, long max) {
		return NumericDocValuesField.newSlowRangeQuery(field, Long.MIN_VALUE, max) ;
	}

	public static Query gt(String field, String lowerTerm) {
		return new TermRangeQuery(field, new BytesRef(lowerTerm), null, false, false) ;
	}

	public static Query lte(String field, String higherTerm) {
		return new TermRangeQuery(field, null, new BytesRef(higherTerm), false, true) ;
	}

	public static Query in(String field, String[] values) {
		List<BytesRef> vref = ListUtil.newList() ;
		for (String value : values) {
			vref.add(new BytesRef(value)) ;
		}
		
		return new TermInSetQuery(field, vref);
	}

	public static Query between(String field, long min, long max) {
		return NumericDocValuesField.newSlowRangeQuery(field, min, max) ;
	}

	public static Query lt(String field, long max) {
		return NumericDocValuesField.newSlowRangeQuery(field, Long.MIN_VALUE, max-1) ;
	}

	public static Query lt(String field, String higherTerm) {
		return new TermRangeQuery(field, null, new BytesRef(higherTerm), false, false) ;
	}

	public static Query between(String field, String minTerm, String maxTerm) {
		return new TermRangeQuery(field, new BytesRef(minTerm), new BytesRef(maxTerm), true, true) ;
	}
	
	public static Query exists(String field){
		return new DocValuesFieldExistsQuery(field) ;
	}


	public static Query where(String fnString) {
		if (StringUtil.isBlank(fnString)) return new MatchAllDocsQuery() ;
		
		Parser<Expression> parser = ExpressionParser.expression();
		Expression result = TerminalParser.parse(parser, fnString);
		
		try {
			return (Query) MethodUtils.invokeMethod(result, "filter", new Object[0]) ;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("can't make filter : " + fnString) ;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("can't make filter : " + fnString) ;
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("can't make filter : " + fnString) ;
		}
	}

	
	public static Query not(Query filter){
		BooleanQuery result = new BooleanQuery.Builder().add(new BooleanClause(filter, Occur.MUST_NOT)).build() ;
		return result ;
	}
	
	
	public static Query and(Query... filters){
		return FilterUtil.and(filters) ;
	}

	public static Query or(Query... filters){
		return FilterUtil.or(filters) ;
	}
	

//	public ChildQueryRequest lte(String field, double max) {
//	filter(NumericRangeFilter.newDoubleRange(field, Double.MIN_VALUE, max, true, true));
//	return this;
//}

//public ChildQueryRequest gt(String field, double min) {
//	filter(NumericRangeFilter.newDoubleRange(field, min, Double.MAX_VALUE, false, true));
//	return this;
//}

//	public ChildQueryRequest gte(String field, double min) {
//	filter(NumericRangeFilter.newDoubleRange(field, min, Double.MAX_VALUE, true, true));
//	return this;
//}

//	public ChildQueryRequest between(String field, double min, double max) {
//		filter(NumericRangeFilter.newDoubleRange(field, min, max, true, true));
//		return this;
//	}
//	public ChildQueryRequest lt(String field, double max) {
//		filter(NumericRangeFilter.newDoubleRange(field, Double.MIN_VALUE, max, true, false));
//		return this;
//	}


}
