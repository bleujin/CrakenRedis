package net.bleujin.rcraken;

import net.bleujin.rcraken.backup.BackupStrategyBuilder;

public abstract class Craken {

	public Craken start() {
		return start(true);
	}

	public abstract Craken start(boolean doStartNodeService) ;

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected abstract Workspace findWorkspace(String wname) ;
	
	public abstract void shutdown() ;
	
	@Deprecated // test only
	public abstract void removeAll() ;

	public BackupStrategyBuilder backupStrategyBuilder(String... wsNames) {
		return new BackupStrategyBuilder(this, wsNames) ;
	}

}
