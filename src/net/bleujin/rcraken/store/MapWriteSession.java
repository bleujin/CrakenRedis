package net.bleujin.rcraken.store;

import java.util.Set;

import org.mapdb.DB;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.ion.framework.parse.gson.JsonObject;

public class MapWriteSession extends WriteSession {

	protected MapWriteSession(Workspace wspace, ReadSession rsession, DB db) {
		super(wspace, rsession);
	}

	@Override
	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Set<String> readStruBy(Fqn fqn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected JsonObject readDataBy(Fqn fqn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exist(String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
