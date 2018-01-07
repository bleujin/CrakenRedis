package net.bleujin.rcraken.store;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;

public class MapConfig implements CrakenConfig {

	
	private Maker maker;

	public MapConfig(Maker maker) {
		this.maker = maker ;
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
		return new MapCraken(maker, Collections.singletonMap(DFT_WORKER_NAME, 3));
	}

	@Override
	public Craken build(Map<String, Integer> workers) {
		return new MapCraken(maker, workers);
	}
}
