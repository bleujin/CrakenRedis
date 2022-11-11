package net.bleujin.rcraken.backup;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.CalendarUtils;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.Debug;

public class StrategyTest extends TestBaseRCraken{

	
	@Test
	public void fullBackup() throws InterruptedException {
		// c.login("testworkspace").workspace().executor(Executors.newCachedThreadPool(new NamedThreadFactory("testworkspace"))) ;
		
		c.backupStrategyBuilder("testworkspace")
			.schedule(5, TimeUnit.SECONDS)
			.fullBackup(new File("./resource/backup"))
//			.fullBackup(dc, "bworkspace")
			.start(DateUtils.addSeconds(new Date(), 5)) ; 
		
		Thread.sleep(1000 * 60) ;
		Debug.debug() ;
	}
	
	
	@Test
	public void incrementBackup() throws InterruptedException, ExecutionException {
		
		c.backupStrategyBuilder("testworkspace")
		.incrementBackup(new File("./resource/backup/backup.db"))
//		.incrementBackup(dc, "bworkspace")
		.start() ; 
		
		c.login("testworkspace").tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("age", 25).merge() ;
			wsession.pathBy("/emp/jin").property("age", 25).merge() ;
			wsession.pathBy("/dept").removeChild() ;
		}) ;
		
		Thread.sleep(1000) ;
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
