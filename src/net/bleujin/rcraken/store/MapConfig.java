package net.bleujin.rcraken.store;

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
		return new MapConfig(DBMaker.memoryDB());
	}

	
	public Maker maker() {
		return maker ;
	}
	
	@Override
	public Craken build() {
		return new MapCraken(maker);
	}

}
