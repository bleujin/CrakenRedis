package net.bleujin.rcraken.store.rdb;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.store.MapCraken;
import net.ion.framework.db.DBController;
import net.ion.framework.db.manager.DBManager;
import net.ion.framework.db.manager.PostSqlDBManager;
import net.ion.framework.db.procedure.PostgreSqlRepositoryService;
import net.ion.framework.util.StringUtil;

public class PGConfig implements CrakenConfig {

	private String jdbcURL ;
	private String userId ;
	private String userPwd ;
	
	private File lobRootDir ;
	
	public PGConfig jdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL ;
		return this ;
	}
	
	public PGConfig userId(String userId) {
		this.userId = userId ;
		return this ;
	}
	
	public PGConfig userPwd(String userPwd) {
		this.userPwd = userPwd ;
		return this ;
	}
	
	public PGConfig lobRootDir(File rootDir) {
		if (! rootDir.exists()) {
			rootDir.mkdirs() ;
		}
		this.lobRootDir = rootDir ;
		return this ;
	}
	
	public File lobRootDir() {
		return lobRootDir ;
	}

	public PGCraken testBuild() {
		this.jdbcURL = "jdbc:postgresql://127.0.0.1:5432/ics6" ;
		this.userId = "postgres" ;
		this.userPwd = "postgres" ;
		this.lobRootDir = new File("./resource/lob") ;
		
		return this.build() ;
	}
	
	@Override
	public PGCraken build() {
		return build(Collections.singletonMap(DFT_WORKER_NAME, 3)) ;
	}

	@Override
	public PGCraken build(Map<String, Integer> workers) {
		if (StringUtil.isBlank(jdbcURL) || StringUtil.isBlank(userId) || StringUtil.isBlank(userPwd) || lobRootDir == null) throw new IllegalStateException("not setted config info") ;
		
		DBManager dbm = new PostSqlDBManager(jdbcURL, userId, userPwd) ;
		return new PGCraken(new DBController(dbm), this, workers);
	}

}
