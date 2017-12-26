package net.bleujin.rcraken.store;

import java.util.HashSet;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;

public class MapBatchSession extends BatchSession {

	private HTreeMap<String, String> dataMap;
	private HTreeMap<String, Set<String>> struMap;

	public MapBatchSession(MapWorkspace wspace, ReadSession rsession, DB db) {
		super(wspace, rsession);
		this.dataMap = db.hashMap(wspace.nodeMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).createOrOpen() ;
		this.struMap = db.hashMap(wspace.struMapName()).keySerializer(Serializer.STRING).valueSerializer(new SerializerPath()).createOrOpen() ;
	}

	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				Set<String> struSet = readStruBy(current.getParent()) ;
				struSet.add(current.name()) ;
				struMap.put(current.getParent().absPath(), struSet);
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString());
	}

	protected Set<String> readStruBy(Fqn fqn) {
		return struMap.getOrDefault(fqn.absPath(), new HashSet<String>()) ;
	}
}
