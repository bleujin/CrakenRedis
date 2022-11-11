package net.bleujin.rcraken.backup;

import java.io.File;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;

public class CopyCrakenTest extends TestBaseRCraken {

	
	@Test
	public void copyCraken() {
		Craken fromCraken = c;
		Craken toCraken = CrakenConfig.mapFile(new File("./resource/backup.db")).build().start() ;
		
		fromCraken.login("testworkspace").root().walkDepth(true, 100).debugPrint() ;
		
		toCraken.login("testworkspace").tran(wsession ->{
			fromCraken.login("testworkspace").root().walkDepth(true, 100).stream().forEach(rnode -> wsession.readFrom(rnode.toJson()).merge()) ;
		}) ;
		
		Debug.line() ;
		toCraken.login("testworkspace").root().walkDepth(true, 100).debugPrint() ;
	}
	
	
	
	
	@Test
	public void corfirmFromCraken() {
		Craken fromCraken = c;
		
		fromCraken.login("testworkspace").root().walkDepth(true, 100).debugPrint() ;
	}
	
	
	@Test
	public void corfirmToCraken() {
		Craken toCraken = CrakenConfig.mapFile(new File("./resource/backup/backup.db")).build().start() ;
		
		toCraken.login("testworkspace").root().walkDepth(true, 100).debugPrint() ;
		toCraken.shutdown() ;
	}
	
}
