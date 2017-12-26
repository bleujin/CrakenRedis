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

public class RedisBatchSession extends BatchSession{

	private RedissonClient rclient;
	private RBatch batch;
	private RMapAsync<String, String> dataMap;
	private RSetMultimap<String, String> struMap;

	public RedisBatchSession(RedisWorkspace wspace, ReadSession rsession, RedissonClient rclient) {
		super(wspace, rsession) ;
		
		this.rclient = rclient ;
		this.batch = rclient.createBatch();
		this.dataMap = batch.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());
	}

	RBatch batch() {
		return batch ;
	}

	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			
			if (! (struMap.containsKey(current.absPath()) && struMap.get(current.getParent().absPath()).contains(current.absPath())  )) {
				struMap.put(current.getParent().absPath(), current.name());
				dataMap.putAsync(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.fastPutAsync(fqn.absPath(), data.toString());
//		
//		// handle lob
//		childJson(data).filter(p -> "Lob".equals(p.asString("type"))&& hasAttribute(p.asString("value"))).forEach(p -> {
//			if (attrs.get(p.asString("value")) instanceof InputStream) {
//		        try {
//		        	InputStream input = (InputStream) attrs.get(p.asString("value")) ;
//		        	OutputStream output = wspace.outputStream(p.asString("value"));
//					IOUtil.copyNClose(input, output);
//				} catch (IOException ex) {
//					attrs.put(p.asString("value"), ex.getMessage()) ;
//				}
//			}
//		});
		
	}
	
}
