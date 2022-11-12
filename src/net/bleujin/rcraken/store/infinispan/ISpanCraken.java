package net.bleujin.rcraken.store.infinispan;

import java.util.Map;

import org.infinispan.manager.DefaultCacheManager;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.util.MapUtil;

public class ISpanCraken extends Craken {

	private CrakenNode cnode;
	private Map<String, ISpanWorkspace> wss = MapUtil.newMap() ;
	private ISpanNode mnode;
	private Map<String, Integer> workers;
	
	private DefaultCacheManager dcm;

	public ISpanCraken(DefaultCacheManager dcm, Map<String, Integer> workers) {
		this.dcm = dcm ;
		this.workers = workers ;
	}

	public Craken start() {
		return start(true);
	}

	public ISpanCraken start(boolean doStartNodeService) {
		dcm.start();
		if (doStartNodeService) this.mnode = new ISpanNode(dcm, workers).start();
		return this;
	}

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected synchronized ISpanWorkspace findWorkspace(String wname) {
		if (wname.startsWith("_"))
			throw new IllegalAccessError("illegal worksapce name");
		
		wss.putIfAbsent(wname, (ISpanWorkspace)new ISpanWorkspace(node(), wname, mnode, dcm.getCache(wname)).init());
		return wss.get(wname);
	}

	public void shutdown() {
		if (cnode != null) cnode.shutdown();
		
		dcm.stop();
	}
	
	@Deprecated // test only
	public void removeAll() {
		
	}
	
	@Deprecated
	public DefaultCacheManager dcm() {
		return dcm ;
	}

	public ISpanNode node() {
		if (this.dcm == null || this.mnode == null ) throw new IllegalStateException("craken node not started");
		return mnode ;
	}
	
}
