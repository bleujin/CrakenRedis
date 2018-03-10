package net.bleujin.rcraken;

import java.util.Map;

import net.ion.framework.util.MapUtil;

public class CrakenSwarm extends Craken{

	private Map<String, CrakenConfig> configchild = MapUtil.newCaseInsensitiveMap() ; 
	private Map<String, Craken> child = MapUtil.newCaseInsensitiveMap() ; 

	private CrakenSwarm() {
	}
	
	public static CrakenSwarm create() {
		return new CrakenSwarm();
	}

	public CrakenSwarm makeSwarm(String wname, CrakenConfig cconfig) {
		configchild.put(wname, cconfig) ;
		return this;
	}

	public CrakenSwarm build() {
		configchild.forEach((key, value) -> {
			child.put(key, value.build()) ;
		});
		
		return this;
	}
	
	@Override
	public CrakenSwarm start(boolean doStartNodeService) {
		child.values().forEach(craken -> craken.start(doStartNodeService));
		return this;
	}

	@Override
	protected Workspace findWorkspace(String wname) {
		Craken craken = child.get(wname);
		if (craken == null) throw new IllegalStateException("not found workspace") ;
		return craken.findWorkspace(wname) ;
	}

	@Override
	public void shutdown() {
		child.values().forEach(craken -> craken.shutdown());
	}

	@Override
	public void removeAll() {
		child.values().forEach(craken -> craken.removeAll());
	}




}
