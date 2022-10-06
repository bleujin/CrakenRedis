package net.bleujin.rcraken.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.SetUtil;

public class RedisWriteSession extends WriteSession{

	private RedissonClient rclient;
	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	protected RedisWriteSession(RedisWorkspace wspace, ReadSession rsession, RedissonClient rclient) {
		super(wspace, rsession) ;
		this.rclient = rclient ;
		this.dataMap = rclient.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString());
		
		// handle lob
		wnode.properties().filter(p -> PType.Lob.equals(p.type())&& hasAttribute(p.asString())).forEach(p ->{
			if (attrs().get(p.asString()) instanceof InputStream) {
		        try {
		        	InputStream input = (InputStream) attrs().get(p.asString()) ;
		        	OutputStream output = workspace().outputStream(p.asString());
					IOUtil.copyNClose(input, output);
				} catch (IOException ex) {
					attrs().put(p.asString(), ex.getMessage()) ;
				}
			}
		});
		
	}
	
	public RedisWorkspace workspace() {
		return (RedisWorkspace)super.workspace() ;
	}
	
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs); 
		
		dataMap.fastRemove(rs.toArray(new String[0])) ;
		struMap.fastRemove(rs.toArray(new String[0])) ;

		rclient.getKeys().findKeysByPattern(workspace().lobPrefix() + fqn.absPath() + "/*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
	}

	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs);
		rs.add(fqn.absPath()) ;
		
		dataMap.fastRemove(rs.toArray(new String[0])) ;
		struMap.fastRemove(rs.toArray(new String[0])) ;
		Fqn parent = fqn.getParent() ;
		if(! fqn.isRoot()) struMap.get(parent.absPath()).remove(fqn.name()) ;
		
		// remove lob
		rclient.getKeys().findKeysByPattern(workspace().lobPrefix() + fqn.absPath() + "$*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
		rclient.getKeys().findKeysByPattern(workspace().lobPrefix() + fqn.absPath() + "/*").forEach(key ->{
			rclient.getBinaryStream(key).delete() ;
		});
	}

	void decendant(Fqn parent, Set<String> rs) {
		Set<String> children = struMap.getAll(parent.absPath()) ;
		for (String child : children) {
			Fqn childFqn = Fqn.from(parent, child);
			rs.add(childFqn.absPath()) ;
			decendant(childFqn, rs);
		}
	}

	protected Set<String> readStruBy(Fqn fqn) {
		return struMap.getAll(fqn.absPath());
	}

	protected JsonObject readDataBy(Fqn fqn) {
		return JsonObject.fromString(dataMap.get(fqn.absPath()));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}

}
