package net.bleujin.rcraken.store.rdb;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.store.cache.CacheMap;
import net.ion.framework.db.DBController;

public class PGCraken extends Craken {

	private DBController dc;
	private Map<String, Integer> workers;
	private CacheMap<String, PGWorkspace> wss = new CacheMap<String, PGWorkspace>(100) ;
	private PGNode pgnode;
	private PGConfig config;

	public PGCraken(DBController dc, PGConfig config, Map<String, Integer> workers) {
		this.dc = dc;
		this.config = config ;
		this.workers = workers ;
	}

	@Override
	public Craken start(boolean doStartNodeService) {

		try {
			if (doStartNodeService)
				dc.initSelf();
			this.pgnode = new PGNode(workers).start();
			
		} catch (SQLException ex) {
			throw new IllegalStateException(ex);
		}
		return this;
	}

	@Override
	protected PGWorkspace findWorkspace(String wname) {
		File wrootDir = new File(config.lobRootDir(), wname) ;
		if (! wrootDir.exists()) {
			boolean created = wrootDir.mkdirs() ;
			if (! created) throw new IllegalStateException("workpace's rootDir cant created") ;
		}
		
		return wss.get(wname, () -> {
			return new PGWorkspace(dc, config, wrootDir, pgnode, wname).init() ;
		}) ;
	}

	@Override
	public void shutdown() {
		dc.destroySelf(); 
	}

	@Override
	public void removeAll() {
		
	}

	public CrakenNode wnode() {
		return pgnode ;
	}

	

	public DBController dc() {
		return dc ;
	}

	public Craken cached(int cachedSize) {
		return new CacheCraken(this, cachedSize);
	}
	
	public boolean storedLOBProperty() {
		return false ;
	}

}
