package net.bleujin.rcraken.store.rdb;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.store.cache.CacheMap;

public class CacheCraken extends Craken {

	private PGCraken icraken;
	private CacheMap<String, Workspace> cached = new CacheMap<String, Workspace>(100) ;
	private int perWorkspace;
	public CacheCraken(PGCraken craken) {
		this(craken, 1000) ;
	}
	public CacheCraken(PGCraken craken, int perWorkspace) {
		this.icraken = craken ; 
		this.perWorkspace = perWorkspace;
	}

	@Override
	public Craken start(boolean doStartNodeService) {
		icraken.start(doStartNodeService) ;
		return this;
	}

	@Override
	protected synchronized Workspace findWorkspace(String wname) {
		return cached.get(wname, ()  -> new CacheWorkspace(icraken.findWorkspace(wname), icraken.wnode(), wname, perWorkspace)).init();
	}

	@Override
	public void shutdown() {
		icraken.shutdown();
	}

	@Override
	public void removeAll() {
		icraken.removeAll(); 
	}

}
