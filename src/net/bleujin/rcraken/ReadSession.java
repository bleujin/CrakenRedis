package net.bleujin.rcraken;

import java.util.Set;
import java.util.concurrent.Future;

import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RedissonClient;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;

public class ReadSession {

	private static ExceptionHandler ehandler = ExceptionHandler.PRINT ;

	private Workspace wspace;
	private RedissonClient rclient;

	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	ReadSession(Workspace wspace, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rclient = rclient ;
		this.dataMap = rclient.getLocalCachedMap(wspace.name(), wspace.mapOption()) ;
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName()) ;
	}

	public ReadNode pathBy(String path) {
		Fqn fqn = Fqn.from(path) ;
		return pathBy(fqn) ;
	}

	public ReadNode pathBy(Fqn fqn) {
		return new ReadNode(this, fqn, readDataBy(fqn));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path) ;
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}

	public <T> Future<T> tran(TransactionJob<T> tjob) {
		
		WriteSession wsession = wspace.writeSession(this) ;

		return wspace.tran(wsession, tjob, ehandler) ;
	}

	private JsonObject readDataBy(Fqn fqn) {
		String jsonString = dataMap.get(fqn.absPath()) ;
		return JsonObject.fromString(jsonString) ;
	}
	
	Set<String> readStruBy(Fqn fqn){
		return struMap.getAll(fqn.absPath()) ;
	}
	

	public Workspace workspace() {
		return wspace;
	}
	
	void reload(){
//		this.cmap.destroy();
//		this.cmap = rclient.getLocalCachedMap(wspace.name(), wspace.mapOption()) ; 
	}

}
