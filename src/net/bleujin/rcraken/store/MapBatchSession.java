package net.bleujin.rcraken.store;

import org.mapdb.DB;

import net.bleujin.rcraken.BatchNode;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;

public class MapBatchSession extends BatchSession {

	public MapBatchSession(MapWorkspace wspace, ReadSession rsession, DB db) {
		super(wspace, rsession);
	}

	@Override
	protected void insert(BatchNode wnode, Fqn fqn, JsonObject data) {
		// TODO Auto-generated method stub

	}

}
