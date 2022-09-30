package net.bleujin.rcraken.store;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.store.rdb.PGConfig;
import net.ion.framework.util.StringUtil;

public class MapConfig implements CrakenConfig {

	
	private Maker maker;
	private File lobRootDir ;
	

	public MapConfig(Maker maker) {
		this.maker = maker ;
	}

	public MapConfig lobRootDir(File rootDir) {
		if (! rootDir.exists()) {
			rootDir.mkdirs() ;
		}
		this.lobRootDir = rootDir ;
		return this ;
	}
	
	public File lobRootDir() {
		return lobRootDir ;
	}
	
	
	public static MapConfig memory() {
		return fromMaker(DBMaker.memoryDB()) ;
	}

	public static MapConfig file(File file) {
		return fromMaker(DBMaker.fileDB(file).closeOnJvmShutdown().fileMmapEnableIfSupported().transactionEnable()) ;
	}

	public static MapConfig fromMaker(Maker maker) {
		return new MapConfig(maker);
	}

	public Maker maker() {
		return maker ;
	}
	
	@Override
	public Craken build() {
		return build(Collections.singletonMap(DFT_WORKER_NAME, 3));
	}

	@Override
	public Craken build(Map<String, Integer> workers) {
		if (maker == null || lobRootDir == null) throw new IllegalStateException("not setted config info") ;
		return new MapCraken(maker, this, workers);
	}
}
