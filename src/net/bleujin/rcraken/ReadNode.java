package net.bleujin.rcraken;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.list.SetUniqueList;

import com.google.common.base.Function;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
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
		return Property.create(fqn, name, data.asJsonObject(name));
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

	public Set<String> childrenNames() {
		return rsession.readStruBy(fqn);
	}

	public ReadSession session() {
		return rsession;
	}

	public int keySize() {
		return data.keySet().size() ;
	};

	public Set<PropertyId> keys(){ // all 
		Set<PropertyId> result = SetUtil.newSet();
		for(String pid :data.keySet()) {
			result.add(PropertyId.fromIdString(pid)) ;
		}
		return result ;
	}

	
	public boolean hasChild(String name) {
		return data.has(name) ;
	};

	public ReadNode child(String name) {
		return rsession.pathBy(Fqn.fromRelativeElements(fqn, name)) ;
	};

//	public Iterable<ReadNode> children(){
//		
//	}
//
//	public Map<PropertyId, Property> propertyMap(){
//		
//	}
//
//	public ReadNode ref(String refName) {
//		
//	};
//
//	public IteratorList<ReadNode> refs(String refName){
//		
//	}
//
//	public boolean hasRef(String refName) {
//		
//	};
//
//	public boolean hasRef(String refName, Fqn fqn) {
//		
//	};
//
//	public <R> R transformer(Function<ReadNode, R> transformer) {
//		
//	};

	public void debugPrint() {
		Debug.line(data.toString());
	};

}
