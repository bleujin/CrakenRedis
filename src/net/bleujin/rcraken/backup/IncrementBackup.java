package net.bleujin.rcraken.backup;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.NodeListener;
import net.bleujin.rcraken.extend.NodeListener.EventType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.promise.Deferred;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.DoubleKeyHashMap;

public class IncrementBackup {

	private final Craken fromCraken;
	private final ScheduledExecutorService exePool;
	private final Deferred<String, String, String> deferred;
	private final String[] wsNames;
	private DoubleKeyHashMap<String, Fqn, BackupEvent> wevents;
	private final Craken toCraken;
	private boolean initFullBackup;

	public IncrementBackup(Craken fromCraken, ScheduledExecutorService exePool, Deferred<String, String, String> deferred, String[] wsNames, Craken toCraken, boolean initFullBackup) {
		this.fromCraken = fromCraken;
		this.exePool = exePool;
		this.deferred = deferred;
		this.wsNames = wsNames;
		this.wevents = new DoubleKeyHashMap<String, Fqn, BackupEvent>();
		this.toCraken = toCraken;
		this.initFullBackup = initFullBackup;
	}

	public Deferred<String, String, String> start() throws InterruptedException, ExecutionException {

		if (initFullBackup)
			exePool.submit(() -> fullBackup()).get(); // pre fullbackup

		for (String wsName : wsNames) {
			toCraken.login(wsName).workspace().executor(exePool);

			fromCraken.login(wsName).workspace().addListener(new NodeListener() {

				@Override
				public void onChanged(EventType etype, Fqn fqn, JsonObject jvalue, JsonObject oldValue) {
					if (fqn.absPath().startsWith("/__endtran")) {
						Set<Entry<Fqn, BackupEvent>> ies = IncrementBackup.this.wevents.entrySet(wsName);
						IncrementBackup.this.wevents.remove(wsName);

						toCraken.login(wsName).tran(wsession -> {
							ies.forEach(entry -> {
								if (entry.getValue().eventType() == EventType.REMOVED) {
									wsession.pathBy(entry.getKey()).removeSelf();
								} else {
									wsession.readFrom(entry.getValue().newAsJson()).merge();
								}
							});
						});

						deferred.notify(wsName + " incremental backuped");
						return;
					}
					if (jvalue != null && !jvalue.keySet().isEmpty()) {
						IncrementBackup.this.wevents.put(wsName, fqn, BackupEvent.create(etype, fqn, jvalue));
					}
				}

				@Override
				public String id() {
					return "backup.incremental";
				}
			});

		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				toCraken.shutdown();
				deferred.notify("============ incremental backup ended");
			}
		});

		return deferred;
	}

	private Craken fullBackup() {

		try {
			for (String wsName : wsNames) {
				deferred.notify(wsName + " backup started");

				ReadSession torsession = toCraken.login(wsName);
				Workspace workspace = torsession.workspace() ;
				
				torsession.tran(new WriteJob<Void>() {
					@Override
					public Void handle(WriteSession wsession) throws Exception {
						ReadSession rsession = fromCraken.login(wsName);

						rsession.root().walkDepth(true, 100).stream().forEach(rnode -> {
							wsession.readFrom(rnode.toJson()).merge();

//							if (toCraken instanceof PGCraken) { // pgcraken.... lob handling.....
//								rnode.properties().filter(p -> PType.Lob.equals(p.type())).forEach(p -> {
//									try {
//										InputStream input = p.asStream();
//										Fqn fqn = rnode.fqn();
//
//										if (input != null) {
//											File dir = new File(((PGWorkspace) workspace).workspaceRootDir(), fqn.getParent().absPath());
//											if (!dir.exists())
//												dir.mkdirs();
//											File targetFile = new File(dir, fqn.name() + "." + p.name()); // rootDir/wname/fqn's parent/nodename.pname
//											IOUtil.copyNCloseSilent(input, new FileOutputStream(targetFile));
//										}
//									} catch (IOException ignore) {
//										ignore.printStackTrace() ;
//									}
//								});
//							}

						});
						deferred.notify(wsName + " backuped");
						return null;
					}
				});
			}
		} catch (Throwable e) {
			deferred.notify(e.getMessage());
		} finally {

		}

		deferred.notify("============ full backup completed at " + DateUtil.currentDateString());
		return toCraken;
	}
}

class BackupEvent {

	private EventType etype;
	private Fqn fqn;
	private JsonObject jvalue;

	public BackupEvent(EventType etype, Fqn fqn, JsonObject jvalue) {
		this.etype = etype;
		this.fqn = fqn;
		this.jvalue = jvalue;
	}

	public static BackupEvent create(EventType etype, Fqn fqn, JsonObject jvalue) {
		return new BackupEvent(etype, fqn, jvalue);
	}

	public JsonObject newAsJson() {
		JsonObject json = new JsonObject();
		json.put("path", fqn.absPath());
		json.put("property", jvalue);
		return json;
	}

	public EventType eventType() {
		return etype;
	}

}
