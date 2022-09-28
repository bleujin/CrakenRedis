package net.bleujin.rcraken.store.rdb;

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

	public PGCraken(DBController dc, Map<String, Integer> workers) {
		this.dc = dc;
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
		return new PGWorkspace(dc, pgnode, wname);
	}

	@Override
	public void shutdown() {
		dc.destroySelf(); 
	}

	@Override
	public void removeAll() {
		
	}

}
