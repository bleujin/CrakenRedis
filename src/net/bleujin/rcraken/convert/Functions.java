package net.bleujin.rcraken.convert;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.bleujin.rcraken.Property;
import net.bleujin.rcraken.Property.PType;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.SelectProjection;
import net.bleujin.rosetta.Parser;
import net.ion.framework.db.Rows;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonParser;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class Functions {

	
	private static Parser<SelectProjection> parser = ExpressionParser.selectProjection();
	public final static Function<ReadNode, Rows> rowsFunction(final ReadSession session, final String expr, final FieldDefinition... fieldDefinitons){
		return new Function<ReadNode, Rows>(){
			@Override
			public Rows apply(ReadNode node) {
				return node.toRows(expr, fieldDefinitons) ;
			}
		} ;
	}
	
	public final static <T> Function<ReadNode, T> beanCGIFunction(final Class<T> clz){
		return new Function<ReadNode, T>(){
			@Override
			public T apply(ReadNode node) {
				return ToBeanStrategy.ProxyByCGLib.toBean(node, clz) ;
			}
		} ;
	}

	public final static <T> Function<ReadNode, T> beanReflectionFunction(final Class<T> clz){
		return new Function<ReadNode, T>(){
			@Override
			public T apply(ReadNode node) {
				return ToBeanStrategy.EasyByJson.toBean(node, clz) ;
			}
		} ;
	}
	
	public static Function<ReadNode, Map> toPropertyValueMap(){
		return new Function<ReadNode, Map>(){
			@Override
			public Map apply(ReadNode node) {
				Map<String, Object> properties = MapUtil.newMap() ;
				node.properties().filter(p -> p.type() != PType.Ref).forEach(p -> properties.put(p.name(), p.asSet().size() == 1 ? p.value() : p.asSet()));
				return properties ;
			}
		} ;
	}
	
	public static final Function<ReadNode, Map<String, Object>> READ_TOFLATMAP = new Function<ReadNode, Map<String, Object>>(){
		@Override
		public Map<String, Object> apply(ReadNode target) {
			Map<String, Object> result = MapUtil.newMap() ;
			target.properties().filter(p -> p.type() != PType.Ref).forEach(p -> result.put(p.name(), p.value()));
			return result;
		}
	} ;
	

	public static final Function<WriteNode, Map<String, Object>> WRITE_TOFLATMAP =  new Function<WriteNode, Map<String, Object>>(){
		@Override
		public Map<String, Object> apply(WriteNode target) {
			Map<String, Object> result = MapUtil.newMap() ;
			target.properties().filter(p -> p.type() != PType.Ref).forEach(p -> result.put(p.name(), p.value()));
			return result;
		}
	} ;


	public static Function<ReadNode, JsonObject> toJson() {
		return new Function<ReadNode, JsonObject>(){
			@Override
			public JsonObject apply(ReadNode node) {
				JsonObject result = new JsonObject() ;
				
				Map<String, Object> properties = MapUtil.newMap() ;
				Map<String, Set> refs = MapUtil.newMap() ;
				node.properties().forEach(p -> {
					if (p.type() != Property.PType.Ref){
						properties.put(p.name(), p.asSet());
					} else {
						refs.put(p.name(), p.asSet()) ;
					}
				}) ;
				result.add("properties", JsonObject.fromObject(properties)) ;
				result.add("references", JsonObject.fromObject(refs)) ;
				result.add("children", JsonParser.fromObject(node.childrenNames())) ;
				
				return result ;
			}
		} ;
	}
	
	public static Function<ReadNode, JsonObject> toJsonExpression() {
		return new Function<ReadNode, JsonObject>(){
			@Override
			public JsonObject apply(ReadNode node) {
				JsonObject result = new JsonObject() ;
				
				Map<String, Object> properties = MapUtil.newMap() ;
				Map<String, Set> refs = MapUtil.newMap() ;
				node.properties().forEach(p -> {
					if (p.type() != Property.PType.Ref){
						properties.put(p.name(), p.asSet());
					} else {
						refs.put(p.name(), p.asSet()) ;
					}
				}) ;
				result.add("properties", JsonObject.fromObject(properties)) ;
				result.add("references", JsonObject.fromObject(refs)) ;
				
				return result ;
			}
		} ;
	}
	
	public final static Function<ReadNode, Void> READ_DEBUGPRINT = new Function<ReadNode, Void>() {
		@Override
		public Void apply(ReadNode target) {
			StringBuilder sb = new StringBuilder() ;
			sb.append(target).append(", [") ;
			boolean notfirst = false ;
			for (String pid :  target.keys()) {
				if (notfirst) sb.append(",") ;
				sb.append(pid).append(":").append(target.property(pid).asString()) ;
				notfirst = true ;
			}
			sb.append("]") ;
			Debug.debug(sb);;
			return null ;
		}
	};


	public static final Function<Iterable<ReadNode>, JsonObject> CHILDLIST = new Function<Iterable<ReadNode>, JsonObject>(){
		@Override
		public JsonObject apply(Iterable<ReadNode> iter) {
			JsonObject result = new JsonObject() ;
			
			for(ReadNode next : iter) {
				result.add(next.fqn().name(), next.toJson());
			}
			return result;
		}
		
	};

	public static final Function<ReadNode, JsonObject> DECENT = new Function<ReadNode, JsonObject>(){
		@Override
		public JsonObject apply(ReadNode target) {
			final JsonObject result = new JsonObject() ;
		
			target.walkBreadth(true, 10).stream().forEach(node -> {
				result.add(node.fqn().toString(), node.toJson());
			}) ;
			
			return result;
		}
	};
	
}
