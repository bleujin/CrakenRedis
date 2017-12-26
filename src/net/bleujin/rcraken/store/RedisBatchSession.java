package net.bleujin.rcraken.store;

import org.redisson.api.RBatch;
import org.redisson.api.RMapAsync;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;

public class RedisBatchSession extends BatchSession {

	private RBatch batch;
	private RMapAsync<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	public RedisBatchSession(RedisWorkspace wspace, ReadSession rsession, RedissonClient rclient) {
		super(wspace, rsession);

		this.batch = rclient.createBatch();
		this.dataMap = batch.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	RBatch batch() {
		return batch;
	}

	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {

			if (!(struMap.containsKey(current.absPath()) && struMap.get(current.getParent().absPath()).contains(current.absPath()))) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.putAsync(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.fastPutAsync(fqn.absPath(), data.toString());
	}

}
