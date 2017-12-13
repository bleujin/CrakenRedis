package net.bleujin.rcraken;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.nsearcher.search.Searcher;

public class ReadSession {

	private static ExceptionHandler ehandler = ExceptionHandler.PRINT;

	private Workspace wspace;
	private RedissonClient rclient;

	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	ReadSession(Workspace wspace, RedissonClient rclient) {
		this.wspace = wspace;
		this.rclient = rclient;
		this.dataMap = rclient.getMapCache(wspace.nodeMapName(), wspace.mapOption());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	public ReadNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);
		return pathBy(fqn);
	}

	public ReadNode pathBy(Fqn fqn) {
		return new ReadNode(this, fqn, readDataBy(fqn));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}

	public <T> Future<T> tran(WriteJob<T> tjob) {
		return tran(tjob, ehandler);
	}

	public <T> Future<T> tran(WriteJob<T> tjob, ExceptionHandler ehandler) {
		WriteSession wsession = wspace.writeSession(this);
		return wspace.tran(wsession, tjob, ehandler);
	}

	public void batch(BatchJob bjob) {
		batch(bjob, ehandler);
	}

	public void batch(BatchJob bjob, ExceptionHandler ehandler) {
		BatchSession bsession = wspace.batchSession(this);
		wspace.batch(bsession, bjob, ehandler);
	}

	
	
	
	private JsonObject readDataBy(Fqn fqn) {
		String jsonString = dataMap.get(fqn.absPath());
		return JsonObject.fromString(jsonString);
	}

	Set<String> readStruBy(Fqn fqn) {
		return struMap.getAll(fqn.absPath());
	}

	void descentantBreadth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
			descentantBreadth(child, fqns);
		}
	}

	void descentantDepth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
		}

		for(String childName : readStruBy(fqn)) {
			descentantDepth(Fqn.from(fqn, childName), fqns);
		}
	}

	public void walkRef(ReadNode source, String relName, int limit, List<String> fqns) {
		if (limit == 0) return ; 
		for(String relPath : source.property(relName).asStrings()) {
			Fqn rel = Fqn.from(relPath);
			if (! source.session().exist(rel.absPath())) continue ;
			fqns.add(rel.absPath()) ;
			walkRef(source.session().pathBy(rel), relName, --limit, fqns);
		}
	}


	public Workspace workspace() {
		return wspace;
	}

	void reload() {
		// this.dataMap = rclient.getMapCache(wspace.name(), wspace.mapOption()) ;
	}

	@Deprecated
	RMap<String, String> dataMap() {
		return dataMap;
	}

	public Searcher newSearcher() throws IOException {
		return workspace().central().newSearcher();
	}

	
	


}
