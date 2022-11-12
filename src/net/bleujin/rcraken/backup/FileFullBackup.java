package net.bleujin.rcraken.backup;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.ion.framework.promise.Deferred;
import net.ion.framework.util.DateUtil;

public class FileFullBackup {

	private final Craken fromCraken;
	private final File backupDir;
	private final Deferred<String, String, String> deferred ;
	private final String[] wsNames ;
	private final ScheduledExecutorService exePool;
	private ScheduleInfo sinfo;
	private int maxFileCount = 3 ;

	FileFullBackup(Craken fromCraken, ScheduledExecutorService exePool, File backupDir, Deferred<String, String, String> deferred, String[] wsNames) {
		this.fromCraken = fromCraken ;
		this.exePool = exePool ;
		this.backupDir = backupDir ;
		this.deferred = deferred ;
		this.wsNames = wsNames ;
	}
	
	
	public Deferred<String, String, String> start(Date startDate) {
		exePool.schedule(()-> backup(), (startDate.getTime() - new Date().getTime()) / 1000, TimeUnit.SECONDS);
		return deferred ;
	}
	
	public FileFullBackup maxFileCount(int maxFileCount) {
		this.maxFileCount = Math.max(2, maxFileCount) ;
		return this ;
	}

	public FileFullBackup schedule(int unitCount, TimeUnit tunit) {
		this.sinfo = new ScheduleInfo(unitCount, tunit);
		return this;
	}


	
	public void backup() {
		
		Craken toCraken = CrakenConfig.mapFile(new File(backupDir, "backup-" + DateUtil.currentDateString() + ".db")).build().start();
		
		try {
			for (String wsName : wsNames) {
				deferred.notify(wsName + " backup started");

				toCraken.login(wsName).tran(new WriteJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						fromCraken.login(wsName).root().walkDepth(true, 100).stream().forEach(rnode -> wsession.readFrom(rnode.toJson()).merge());
						deferred.notify(wsName + " backuped");
						return null;
					}
				}) ;
			}
			
			// delete oldest backup-file 
			File[] bfiles = backupDir.listFiles( (dir, name) -> {return name.startsWith("backup-") && name.endsWith("db") ;}) ;
			if (bfiles.length > maxFileCount) {
				File oldestFile = Arrays.stream(bfiles).sorted( (f1, f2) ->   (int)(f1.lastModified() - f2.lastModified()) )
					.findFirst().get();
				oldestFile.delete() ;
				
				deferred.notify(oldestFile.getName() + " removed") ;
			}

		} catch(Throwable e) {
			deferred.notify(e.getMessage()) ;
		} finally {
			toCraken.shutdown();
			if (this.sinfo != null) {
				exePool.schedule(() -> backup(), sinfo.unitCount(), sinfo.timeUnit());
			}
		}
		
		deferred.notify("============ full backup completed at " + DateUtil.currentDateString());
	}

}
