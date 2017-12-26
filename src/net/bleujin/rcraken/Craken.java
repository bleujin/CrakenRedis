package net.bleujin.rcraken;

public abstract class Craken {

	public Craken start() {
		return start(true);
	}

	public abstract Craken start(boolean doStartNodeService) ;

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected abstract Workspace findWorkspace(String wname) ;
	
	public abstract void shutdownSelf() ;
	
	@Deprecated // test only
	public abstract void removeAll() ;

	public abstract CrakenNode node() ;
	

}
