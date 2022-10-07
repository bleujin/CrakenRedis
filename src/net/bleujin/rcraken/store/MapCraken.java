package net.bleujin.rcraken.store;

import java.io.File;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class MapCraken extends Craken {

	private CrakenNode cnode;
	private Maker maker;
	private DB db;
	private Map<String, MapWorkspace> wss = MapUtil.newMap() ;
	private MapNode mnode;
	private Map<String, Integer> workers;
	private MapConfig config;

	public MapCraken(Maker maker, MapConfig config, Map<String, Integer> workers) {
		this.maker = maker;
		this.config = config ;
		this.workers = workers ;
	}

	public Craken start() {
		return start(true);
	}

	public MapCraken start(boolean doStartNodeService) {
		this.db = maker.make() ;
		if (doStartNodeService) this.mnode = new MapNode(workers).start();
		return this;
	}

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected synchronized MapWorkspace findWorkspace(String wname) {
		if (wname.startsWith("_"))
			throw new IllegalAccessError("illegal worksapce name");
		
		File wrootDir = new File(config.lobRootDir(), wname) ;
		if (! wrootDir.exists()) {
			boolean created = wrootDir.mkdirs() ;
			if (! created) throw new IllegalStateException("workpace's rootDir can't created") ;
		}
		
		wss.putIfAbsent(wname, (MapWorkspace)new MapWorkspace(node(), wname, wrootDir, mnode, db).init());
		return wss.get(wname);
	}

	public void shutdown() {
		if (cnode != null) cnode.shutdown();
		db.close();
	}
	
	@Deprecated // test only
	public void removeAll() {
		
	}
	
	@Deprecated
	public DB db() {
		return db ;
	}

	public MapNode node() {
		if (this.db == null || this.mnode == null) throw new IllegalStateException("craken node not started");
		return mnode ;
	}
	
}
