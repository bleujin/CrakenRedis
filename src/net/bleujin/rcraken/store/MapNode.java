package net.bleujin.rcraken.store;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

import org.mapdb.DB;
import org.mapdb.DBMaker.Maker;
import org.redisson.api.RMap;

import net.bleujin.rcraken.CrakenNode;

public class MapNode implements CrakenNode{

	public MapNode(DB db, Maker maker) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public MapNode start() {
		return this;
	}

	@Override
	public ScheduledExecutorService executorService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledExecutorService executorService(String workerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadWriteLock rwLock(String rwName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T, R> RMap<T, R> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
