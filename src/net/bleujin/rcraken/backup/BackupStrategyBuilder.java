package net.bleujin.rcraken.backup;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.util.NamedThreadFactory;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;
import net.ion.framework.promise.Deferred;
import net.ion.framework.promise.impl.DeferredObject;
import net.ion.framework.util.Debug;

public class BackupStrategyBuilder {

	private Craken fromCraken;
	private String[] wsNames;
	private ScheduledExecutorService exePool;
	private ScheduleInfo sinfo;
	
	public BackupStrategyBuilder(Craken craken, String[] wsNames) {
		this(craken, wsNames, Executors.newScheduledThreadPool(3, new NamedThreadFactory("backup-pool"))) ;
	}

	public BackupStrategyBuilder(Craken craken, String[] wsNames, ScheduledExecutorService exePool) {
		this.fromCraken = craken;
		this.wsNames = wsNames;
		this.exePool = exePool ;
	}

	public BackupStrategyBuilder schedule(int unitCount, TimeUnit tunit) {
		this.sinfo = new ScheduleInfo(unitCount, tunit);
		return this;
	}

	public FileFullBackup fullBackup(File backupDir) {
		if (!backupDir.exists())
			backupDir.mkdirs();
		
		FileFullBackup bstrategy = new FileFullBackup(fromCraken, exePool, backupDir, newDeferred(), wsNames, sinfo) ;
		
		return bstrategy ;
	}

	public IncrementBackup incrementBackup(File backupFile) {
		if (!backupFile.getParentFile().exists())
			backupFile.getParentFile().mkdirs();
		if (backupFile.exists()) backupFile.delete() ; // remove
		
		Craken toCraken = CrakenConfig.mapFile(backupFile).build().start();

		IncrementBackup bstrategy = new IncrementBackup(fromCraken, exePool, newDeferred(), wsNames, toCraken) ;
		return bstrategy;
	}

	public IncrementBackup incrementBackup(String jdbcURL, String userId, String userPwd, File lobRootDir) {
		Craken toCraken = CrakenConfig.pgDB(jdbcURL, userId, userPwd, lobRootDir).build() ;

		IncrementBackup bstrategy = new IncrementBackup(fromCraken, exePool, newDeferred(), wsNames, toCraken) ;
		return bstrategy;
	}

	
	private Deferred<String, String, String> newDeferred() {
		final Deferred<String, String, String> deferred = new DeferredObject<>();
		deferred.promise()
			.progress(Debug::println) // singleton deferred
			.fail(res -> Debug.error(res))
			.always((state, res, emessage) -> { Debug.line(state, res, emessage == null ? "" : emessage); }) ;
		return deferred;
	}
	

}

class ScheduleInfo {

	private long unitCount;
	private TimeUnit timeUnit;

	public ScheduleInfo(long unitCount, TimeUnit timeUnit) {
		this.unitCount = unitCount;
		this.timeUnit = timeUnit;
	}

	public long unitCount() {
		return unitCount;
	}

	public TimeUnit timeUnit() {
		return timeUnit;
	}
}