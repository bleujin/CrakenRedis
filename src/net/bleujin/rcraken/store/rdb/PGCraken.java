package net.bleujin.rcraken.store.rdb;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.store.MapNode;
import net.ion.framework.db.DBController;

public class PGCraken extends Craken {

	private DBController dc;
	private Map<String, Integer> workers;
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
	protected Workspace findWorkspace(String wname) {
		File wrootDir = new File(config.lobRootDir(), wname) ;
		if (! wrootDir.exists()) {
			boolean created = wrootDir.mkdirs() ;
			if (! created) throw new IllegalStateException("workpace's rootDir cant created") ;
		}
		
		return new PGWorkspace(dc, config, wrootDir, pgnode, wname);
	}

	@Override
	public void shutdown() {
		dc.destroySelf(); 
	}

	@Override
	public void removeAll() {
		
	}

}
