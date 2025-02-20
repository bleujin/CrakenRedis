package net.bleujin.rcraken.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.PoolingConnection;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import net.ion.framework.db.IDBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.manager.DBManager;
import net.ion.framework.db.procedure.RepositoryService;

public abstract class CrakenManager extends DBManager {

	private Connection fake;
	private RepositoryService cservice;

	protected CrakenManager() {
		this.cservice = new CrakenRepositoryService(this);
	}
	
	public abstract Rows queryBy(CrakenUserProcedure crakenUserProcedure) throws Exception;

	public abstract int updateWith(CrakenUserProcedure crakenUserProcedure) throws Exception;

	public abstract int updateWith(CrakenUserProcedureBatch crakenUserProcedureBatch) throws Exception;

	public abstract int updateWith(CrakenUserProcedures crakenUserProcedures) throws Exception ;
	

	@Override
	public Connection getConnection() throws SQLException {
		return this.fake;
	}

	@Override
	public int getDBManagerType() {
		return 77;
	}

	@Override
	public String getDBType() {
		return "crakenFn";
	}

	@Override
	public RepositoryService getRepositoryService() {
		return cservice;
	}

	@Override
	protected void myDestroyPool() throws Exception {

	}

	protected void heartbeatQuery(IDBController dc) throws SQLException {
		// no action
	}

	@Override
	protected void myInitPool() throws SQLException {

//		this.fake = (Connection)Enhancer.create(Connection.class, new ConnectionMock()) ;

//		Enhancer e = new Enhancer();
//		e.setSuperclass(Connection.class);
//		e.setCallback(new ConnectionMock());
//		this.fake = (Connection) e.create();
		
		this.fake  = (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), PoolingConnection.class.getInterfaces(), new ConnectionProxy());
	}

	
	
}


class ConnectionMock implements MethodInterceptor {

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy arg3) throws Throwable {
		return null;
		// throw new IllegalStateException("this is fake object") ;
	}
}


class ConnectionProxy implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return null;
	}
	
}

