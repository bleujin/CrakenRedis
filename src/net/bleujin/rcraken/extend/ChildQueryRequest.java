package net.bleujin.rcraken.extend;

import java.io.IOException;
import java.util.Iterator;

import org.apache.ecs.xml.XML;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;

import com.google.common.base.Splitter;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.convert.Filters;
import net.bleujin.searcher.SearchRequestWrapper;
import net.bleujin.searcher.Searcher;
import net.bleujin.searcher.common.IKeywordField;
import net.bleujin.searcher.search.SearchResponse;
import net.bleujin.searcher.search.filter.FilterUtil;
import net.ion.framework.db.Page;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

public class ChildQueryRequest {

	private ReadSession session;
	private SearchRequestWrapper srequest;

	protected ChildQueryRequest(ReadSession session, Query query, Searcher searcher) {
		this.session = session;
		this.srequest = searcher.createRequest(query).selections(IKeywordField.DocKey);
	}

	public static ChildQueryRequest create(ReadSession session, Searcher searcher, Query query) {
		return new ChildQueryRequest(session, query, searcher);
	}

	public ChildQueryRequest refTo(String refName, Fqn target) {
		filter(new TermQuery(new Term("@" + refName, target.toString())));
		return this;
	}

	public ChildQueryRequest in(String field, String... values) {
		if (values == null || values.length == 0)
			return this;

		filter(Filters.in(field, values));
		return this;
	}

	public ChildQueryRequest between(String field, int min, int max) {
		return between(field, 1L * min, 1L * max);
	}

	public ChildQueryRequest between(String field, long min, long max) {
		filter(Filters.between(field, min, max));
		return this;
	}

	public ChildQueryRequest between(String field, String minTerm, String maxTerm) {
		filter(Filters.between(field, minTerm, maxTerm));
		return this;
	}

	public ChildQueryRequest lt(String field, int max) {
		return lt(field, 1L * max);
	}

	public ChildQueryRequest lt(String field, long max) {
		filter(Filters.lt(field, max));
		return this;
	}

	public ChildQueryRequest lt(String field, String higherTerm) {
		filter(Filters.lt(field, higherTerm));
		return this;
	}

	public ChildQueryRequest lt(String field, Object max) {
		if (max instanceof Long) {
			return lt(field, (Long) max);
		} else {
			return lt(field, ObjectUtil.toString(max));
		}
	}

	public ChildQueryRequest lte(String field, String higherTerm) {
		filter(Filters.lte(field, higherTerm));
		return this;
	}

	public ChildQueryRequest lte(String field, int max) {
		return lte(field, 1L * max);
	}

	public ChildQueryRequest lte(String field, long max) {
		filter(Filters.lte(field, max));
		return this;
	}

	public ChildQueryRequest lte(String field, Object max) {
		if (max instanceof Long) {
			return lte(field, (Long) max);
		} else {
			return lte(field, ObjectUtil.toString(max));
		}
	}

	public ChildQueryRequest gt(String field, int min) {
		return gt(field, 1L * min);
	}

	public ChildQueryRequest gt(String field, long min) {
		filter(Filters.gt(field, min));
		return this;
	}

	public ChildQueryRequest gt(String field, String lowerTerm) {
		filter(Filters.gt(field, lowerTerm));
		return this;
	}

	public ChildQueryRequest gt(String field, Object max) {
		if (max instanceof Long) {
			return gt(field, (Long) max);
		} else {
			return gt(field, ObjectUtil.toString(max));
		}
	}

	public ChildQueryRequest gte(String field, String lowerTerm) {
		filter(Filters.gte(field, lowerTerm));
		return this;
	}

	public ChildQueryRequest gte(String field, int min) {
		return gte(field, 1L * min);
	}

	public ChildQueryRequest gte(String field, long min) {
		filter(Filters.gte(field, min));
		return this;
	}

	public ChildQueryRequest gte(String field, Object max) {
		if (max instanceof Long) {
			return gte(field, (Long) max);
		} else {
			return gte(field, ObjectUtil.toString(max));
		}
	}

	public ChildQueryRequest eq(String field, Object value) {
		filter(Filters.eq(field, value));
		return this;
	}

	public ChildQueryRequest ne(String field, String value) {
		filter(Filters.ne(field, value));
		return this;
	}

	public ChildQueryRequest ne(String field, Object value) {
		filter(Filters.ne(field, ObjectUtil.toString(value)));
		return this;
	}

	public ChildQueryRequest where(String fnString) {
		if (StringUtil.isBlank(fnString))
			return filter(new MatchAllDocsQuery());
		filter(Filters.where(fnString));
		return this;
	}

	public ChildQueryRequest wildcard(String field, Object value) {
		filter(Filters.wildcard(field, value));
		return this;
	}

	public ChildQueryRequest query(String query) throws ParseException {
		filter(Filters.query(session.workspace().central().sconfig().defaultParser(), query));
		return this;
	}

	public ChildQueryRequest skip(int skip) {
		srequest.skip(skip);
		return this;
	}

	public Query query() {
		return srequest.query();
	}

	public ChildQueryRequest page(Page page) {
		this.skip(page.getStartLoc()).offset(page.getListNum());
		return this;
	}

	public ChildQueryRequest offset(int offset) {
		srequest.offset(offset);
		return this;
	}

	public int skip() {
		return srequest.skip();
	}

	public int offset() {
		return srequest.offset();
	}

	public Sort sort() {
		return srequest.sort();
	}

	public int limit() {
		return skip() + offset();
	}

	public ChildQueryRequest ascending(String field) {
		srequest.ascending(field);
		return this;
	}

	public ChildQueryRequest descending(String field) {
		srequest.descending(field);
		return this;
	}

	public ChildQueryRequest ascendingNum(String field) {
		srequest.ascendingNum(field);
		return this;
	}

	public ChildQueryRequest descendingNum(String field) {
		srequest.descendingNum(field);
		return this;
	}

	public void setParam(String key, Object value) {
		srequest.setParam(key, value);
	}

	public Object getParam(String key) {
		return srequest.getParam(key);
	}

	public ChildQueryRequest filter(Query... filters) {
		if (filters == null)
			return this;

		Query compositeFilter = (srequest.filter() == Searcher.MatchAllDocsQuery) ? FilterUtil.and(filters) : FilterUtil.and(srequest.filter(), FilterUtil.and(filters));
		srequest.filter(compositeFilter);
		return this;
	}

	public Query getFilter() {
		return srequest.filter();
	}

	public ChildQueryResponse find() throws IOException {
		// field=asc && field2=desc...
		srequest.selections(IKeywordField.DocKey);

		final SearchResponse response = srequest.find() ;
		return ChildQueryResponse.create(session, response);
	}

	public ReadNode findOne() throws IOException {
		return find().first();
	}

	public SearchRequestWrapper request() {
		return srequest;
	}

	public XML toXML() {
		return srequest.toXML();
	}

	public String toString() {
		return srequest.toString();
	}

	public ChildQueryRequest sort(String sexpression) {
		Iterable<String> sorts = Splitter.on('&').trimResults().omitEmptyStrings().split(sexpression);
		
		Iterator<String> iter = sorts.iterator();
		while (iter.hasNext()) {
			String[] exp = StringUtil.split(iter.next(), " =");
			if (exp.length != 1 && exp.length != 2)
				throw new IllegalArgumentException("illegal sort expression : " + exp.toString());
			if (exp.length == 1) {
				srequest.ascending(exp[0]);
			} else if (exp.length == 2) {
				srequest = ("desc".equalsIgnoreCase(exp[1])) ? srequest.descending(exp[0]) : srequest.ascending(exp[0]);
			}
		}
		
		return this;
	}

}
