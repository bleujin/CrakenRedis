package net.bleujin.rcraken.store.infinispan;

import java.util.HashSet;
import java.util.Set;

import org.infinispan.Cache;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.StringUtil;

public class ISpanBatchSession extends BatchSession {

	private Cache<String, String> dataMap;
	private ISpanReadSession irs;

	public ISpanBatchSession(ISpanWorkspace wspace, ReadSession rsession, Cache<String, String> db) {
		super(wspace, rsession);
		this.dataMap = db ;
		this.irs = (ISpanReadSession) rsession ;
	}

	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				Set<String> struSet = irs.readStruBy(current.getParent()) ;
				struSet.add(current.name()) ;
				dataMap.put(current.getParent().struPath(), StringUtil.join(struSet, '/'));
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString());
	}

}
