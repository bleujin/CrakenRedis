package net.bleujin.rcraken;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import net.bleujin.rcraken.convert.FieldDefinition;
import net.bleujin.rcraken.convert.ToBeanStrategy;
import net.bleujin.rcraken.def.Defined;
import net.bleujin.rcraken.extend.ChildQueryRequest;
import net.bleujin.rcraken.template.PropertyId;
import net.bleujin.rcraken.template.PropertyValue;
import net.bleujin.searcher.SearchController;
import net.ion.framework.db.Rows;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.StringUtil;

public class ReadNode implements CommonNode, Comparable<ReadNode> {

	private ReadSession rsession;
	private Fqn fqn;
	private JsonObject data;

	ReadNode(ReadSession rsession, Fqn fqn, JsonObject data) {
		this.rsession = rsession;
		this.fqn = fqn;
		this.data = data;
	}

	public boolean hasProperty(String name) {
		return data.has(name);
	}

	public Property property(String name) {
		return Property.create(rsession, fqn, name, data.asJsonObject(name));
	}

	public String asString(String name) {
		return property(name).asString();
	}
	
	public Object asValue(String name) {
		return property(name).value() ;
	}
	
	public <T> T defaultValue(String name, T dftValue) {
		return (T) ObjectUtil.coalesce(asValue(name), dftValue) ;
	}

	public Fqn fqn() {
		return fqn;
	}

	public ReadNode parent() {
		return rsession.pathBy(fqn.getParent());
	}

	public boolean isRoot() {
		return fqn.isRoot();
	}
	
	public boolean exist() {
		return rsession.exist(fqn.absPath()) ;
	}

	public Set<String> childrenNames() {
		return rsession.readStruBy(fqn);
	}

	public ReadSession session() {
		return rsession;
	}

	public int keySize() {
		return data.keySet().size();
	};

	public Set<String> keys() { // all
		return data.keySet();
	}
	
	public Stream<Property> properties() {
		return keys().stream().map(pid -> property(pid)) ;
	}


	public boolean hasChild(String name) {
		return child(name).exist();
	};

	public ReadNode child(String name) {
		return rsession.pathBy(Fqn.from(fqn, name));
	};

	public ReadChildren children() {
		return new ReadChildren(rsession, fqn, childrenNames());
	}
	
	public ReadWalk walkBreadth() {
		return walkBreadth(false, 10) ;
	}

	public ReadWalk walkDepth() {
		return walkDepth(false, 10) ;
	}

	public ReadWalk walkBreadth(boolean includeSelf, int maxlevel) {
		List<String> fqns = ListUtil.newList() ;
		if (includeSelf) fqns.add(fqn().absPath()) ;
		
		rsession.descentantBreadth(this.fqn, fqns, maxlevel);
		return new ReadWalk(rsession, this.fqn, fqns);
	}

	public ReadWalk walkDepth(boolean includeSelf, int maxlevel) {
		List<String> fqns = ListUtil.newList() ;
		if (includeSelf) fqns.add(fqn().absPath()) ;
		
		rsession.descentantDepth(this.fqn, fqns, maxlevel);
		return new ReadWalk(rsession, this.fqn, fqns);
	}

	
	
	public ReadWalk refChildren(String relName, int limit) {
		List<String> fqns = ListUtil.newList() ;
		rsession.walkRef(this, relName, limit, fqns);
		return new ReadWalk(rsession, this.fqn, fqns);
	}

	
	public ReadWalk refs(String relName) {
		Set<String> relFqns = SetUtil.create(property(relName).asStrings()) ;
		for (String relFqn : relFqns) {
			if (! rsession.exist(relFqn)) relFqns.remove(relFqn) ;
		}
				
		return new ReadWalk(rsession, this.fqn, relFqns);
	}
	
	public ReadNode ref(String refName) {
		return rsession.pathBy(asString(refName));
	};

	public boolean hasRef(String refName) {
		return ref(refName).exist();
	};

	public boolean hasRef(String refName, Fqn target) {
		ReadNode refNode = ref(refName);
		return refNode.exist() && refNode.fqn().equals(target);
	};
	
	public boolean isMatch(String key, String value) {
		return this.property(key).defaultValue("").equals(session().encrypt(value)) ;
	}

	public void debugPrint() {
		Debug.line(this);
	}

	public String toString() {
		return "ReadNode:[path:" + fqn + ", property:" + data + "]";
	}

	
	public ChildQueryRequest childQuery(String query) throws IOException {
		if (StringUtil.isBlank(query))
			return childQuery(new TermQuery(new Term(Defined.Index.PARENT, this.fqn().toString())));

		SearchController central = rsession.workspace().central();
		Analyzer analyzer = central.sconfig().defaultAnalyzer();
		try {
			final ChildQueryRequest result = ChildQueryRequest.create(rsession, rsession.newSearcher(), central.sconfig().defaultParser().parse(query));
			result.filter(new TermQuery(new Term(Defined.Index.PARENT, this.fqn().toString())));

			return result;
		} catch (ParseException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public ChildQueryRequest childTermQuery(String name, String value, boolean includeDecentTree) throws IOException {
		if (StringUtil.isBlank(name) || StringUtil.isBlank(value)) throw new IllegalStateException(String.format("not defined name or value[%s:%s]", name, value)) ;
		
		final ChildQueryRequest result = ChildQueryRequest.create(rsession, rsession.newSearcher(), new TermQuery(new Term(name, value)));
		if (includeDecentTree){
			result.filter(this.fqn().childrenQuery());
		} else {
			result.filter(new TermQuery(new Term(Defined.Index.PARENT, this.fqn().toString())));
		}
		return result;
	}

	public ChildQueryRequest childQuery(Query query) throws IOException {
		return ChildQueryRequest.create(rsession, rsession.newSearcher(), query);
	}

	public ChildQueryRequest childQuery(Query query, boolean includeDecentTree) throws IOException {
		if (!includeDecentTree)
			return childQuery(query);

		Analyzer analyzer = session().workspace().central().sconfig().defaultAnalyzer() ;
		final ChildQueryRequest result = ChildQueryRequest.create(rsession, rsession.newSearcher(), query);
		result.filter(this.fqn().childrenQuery());

		return result;
	}

	public ChildQueryRequest childQuery(String query, boolean includeDecentTree) throws IOException {
		if (!includeDecentTree)
			return childQuery(query);

		if (StringUtil.isBlank(query))
			return childQuery(this.fqn().childrenQuery());

		try {
			SearchController central = rsession.workspace().central();
			Analyzer analyzer = central.sconfig().defaultAnalyzer();
			final ChildQueryRequest result = ChildQueryRequest.create(rsession, central.newSearcher(), central.sconfig().defaultParser().parse(query));
			result.filter(this.fqn().childrenQuery());
			return result;
		} catch (ParseException ex) {
			throw new IllegalStateException(ex) ;
		}

	}

	public Rows toRows(String expr, FieldDefinition... fds) {
		try {
			return new ReadStream(rsession, Arrays.asList(this).stream()).toRows(expr, fds) ;
		} catch (SQLException ex) {
			throw new IllegalStateException(ex) ;
		} 
	}

	public <T> T toBean(Class<T> clz) {
		return ToBeanStrategy.ProxyByCGLib.toBean(this, clz) ;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject() ;
		json.put("path", fqn.absPath()) ;
		json.put("property", this.data) ;
		return json ;
	}
	
	public JsonObject toFlatJson() {
		JsonObject json = new JsonObject() ;
		this.data.entrySet().forEach(entry ->{
			json.add(entry.getKey(), entry.getValue().getAsJsonObject().has("values") ? entry.getValue().getAsJsonObject().get("values") : entry.getValue().getAsJsonObject().get("value")) ;
		});
		return json ;
	}

	@Override
	public int compareTo(ReadNode o) {
		return fqn.absPath().compareTo(o.fqn.absPath());
	}

	public <F> F transformer(Function<ReadNode, F> fn) {
		return fn.apply(this);
	}
	
	public Map<PropertyId, PropertyValue> toMap() {
		return data.toMap().entrySet().stream().collect(Collectors.toMap(
				entry-> PropertyId.create(entry),  
				entry -> PropertyValue.create(entry.getValue()))) ;
	}
}
