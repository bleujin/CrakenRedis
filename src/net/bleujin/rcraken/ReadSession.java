package net.bleujin.rcraken;

import java.util.concurrent.Future;

import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;

import net.ion.framework.parse.gson.JsonObject;

public class ReadSession {

	private static ExceptionHandler ehandler = ExceptionHandler.PRINT ;

	private Workspace wspace;
	private RLocalCachedMap<String, String> cmap;
	private RedissonClient rclient;

	ReadSession(Workspace wspace, RedissonClient rclient) {
		this.wspace = wspace ;
		this.rclient = rclient ;
		this.cmap = rclient.getLocalCachedMap(wspace.name(), wspace.mapOption()) ; 
	}

	public ReadNode pathBy(String path) {
		Fqn fqn = Fqn.from(path) ;
		return new ReadNode(this, fqn, readDataBy(fqn));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path) ;
		return fqn.isRoot() || cmap.containsKey(fqn.absPath());
	}

	public <T> Future<T> tran(TransactionJob<T> tjob) {
		
		WriteSession wsession = wspace.writeSession(this) ;

		return wspace.tran(wsession, tjob, ehandler) ;
	}

	private JsonObject readDataBy(Fqn fqn) {
		String jsonString = cmap.get(fqn.absPath()) ;
		return JsonObject.fromString(jsonString) ;
	}

	public Workspace workspace() {
		return wspace;
	}
	
	void reload(){
		
		// this.cmap = rclient.getMap(wspace.name()) ; 
	}

}
