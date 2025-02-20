package net.bleujin.rcraken.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.tbase.TestBaseRCraken;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class StrategyTest extends TestBaseRCraken{

	
	@Test
	@Disabled("too long")
	public void fullBackup() throws InterruptedException {
		
		c.backupStrategyBuilder("testworkspace")
			.fullBackup(new File("./resource/backup"))
			.schedule(5, TimeUnit.SECONDS)
			.start(DateUtils.addSeconds(new Date(), 5)) ; 
		
		Thread.sleep(1000 * 60) ;
		Debug.debug() ;
	}
	
	
	@Test
	@Disabled("too long")
	public void incrementBackup() throws InterruptedException, ExecutionException, SQLException {

		c.backupStrategyBuilder("testworkspace")
		.incrementBackup(new File("./resource/backup/backup.db"), true)
		.start() ; 
		
		c.login("testworkspace").tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("age", 25).merge() ;
			wsession.pathBy("/emp/jin").property("age", 25).merge() ;
			wsession.pathBy("/dept").removeChild() ;
		}) ;
		
		Thread.sleep(1000) ;
	}

	@Test
	@Disabled("too long")
	public void incrementBackup2() throws InterruptedException, ExecutionException, SQLException {

		c.backupStrategyBuilder("testworkspace")
		.incrementBackup("jdbc:postgresql://127.0.0.1:5432/bleujin", "postgres", "redf", new File("./resource/lob"), true)
		.start() ; 
		
		c.login("testworkspace").tran(wsession ->{
			wsession.pathBy("/emp/bleujin").property("age", 25).property("data2", new FileInputStream(new File("./resource/helloworld.txt"))).merge() ;
			wsession.pathBy("/emp/jin").property("age", 25).merge() ;
			wsession.pathBy("/dept").removeChild() ;
		}) ;
		
		Thread.sleep(1000) ;
	}

	
	
	@Test
	public void corfirmFromCraken() throws IOException {
		Craken fromCraken = c;
		
		ReadSession rsession = fromCraken.login("testworkspace");

		rsession.root().walkDepth(true, 100).debugPrint() ;
 		InputStream input = rsession.pathBy("/emp/bleujin").property("data").asStream() ;
 		IOUtil.copyNClose(input, new FileOutputStream(new File("./resource/temp/helloworld_from.txt")));
	}
	
	@Test
	public void corfirmToCraken() throws FileNotFoundException, IOException {
		Craken toCraken = CrakenConfig.mapFile(new File("./resource/backup/backup.db")).build().start() ;
		
		ReadSession rsession = toCraken.login("testworkspace");
		rsession.root().walkDepth(true, 100).debugPrint() ;
		InputStream input = rsession.pathBy("/emp/bleujin").property("data").asStream() ;
 		IOUtil.copyNClose(input, new FileOutputStream(new File("./resource/temp/helloworld_to.txt")));
		
		toCraken.shutdown() ;
	}
	
	
}
