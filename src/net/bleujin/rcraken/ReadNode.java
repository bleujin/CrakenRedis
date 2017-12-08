package net.bleujin.rcraken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections.list.SetUniqueList;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.SetUtil;

public class ReadNode {

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
		Iterator<String> keyIter = keys().iterator() ;
		return StreamSupport.stream(Spliterators.spliterator(new Iterator<Property>() {
			@Override
			public boolean hasNext() {
				return keyIter.hasNext();
			}

			@Override
			public Property next() {
				return property(keyIter.next());
			}
		}, keySize(), 0), false);
	}


	public boolean hasChild(String name) {
		return data.has(name);
	};

	public ReadNode child(String name) {
		return rsession.pathBy(Fqn.from(fqn, name));
	};

	public ReadChildren children() {
		return new ReadChildren(rsession, fqn, childrenNames());
	}
	
	public WalkReadChildren walkBreadth() {
		List<String> fqns = ListUtil.newList() ;
		rsession.descentantBreadth(this.fqn, fqns);
		return new WalkReadChildren(rsession, this.fqn, fqns);
	}

	public WalkReadChildren walkDepth() {
		List<String> fqns = ListUtil.newList() ;
		rsession.descentantDepth(this.fqn, fqns);
		return new WalkReadChildren(rsession, this.fqn, fqns);
	}

	public WalkReadChildren refChildren(String relName, int limit) {
		List<String> fqns = ListUtil.newList() ;
		rsession.walkRef(this, relName, limit, fqns);
		return new WalkReadChildren(rsession, this.fqn, fqns);
	}

	
	public WalkReadChildren refs(String relName) {
		Set<String> relFqns = SetUtil.create(property(relName).asStrings()) ;
		for (String relFqn : relFqns.toArray(new String[0])) {
			if (! rsession.exist(relFqn)) relFqns.remove(relFqn) ;
		}
				
		return new WalkReadChildren(rsession, this.fqn, relFqns);
	}
	
	public ReadNode ref(String refName) {
		return rsession.pathBy(asString(refName));
	};

	// public boolean hasRef(String refName, Fqn fqn) {
	//
	// };


	public void debugPrint() {
		Debug.line(this);
	}

	public String toString() {
		return "ReadNode:[fqn:" + fqn + ", props:" + data + "]";
	}



}
