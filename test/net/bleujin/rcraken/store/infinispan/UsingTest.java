package net.bleujin.rcraken.store.infinispan;

import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;

public class UsingTest {

	private static DefaultCacheManager cacheManager;

	@BeforeAll
	static void init() throws Exception {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
		// Construct a local cache manager using the configuration we have defined
		cacheManager = new DefaultCacheManager(builder.build());
	}
	
	@AfterAll
	static void done() throws Exception {
		cacheManager.stop();
	}
	
	
	@Test
	public void saveByte() throws Exception {
		Cache<String, byte[]> cache = cacheManager.getCache("byte");
		cache.put("key", "Hello".getBytes()) ;
		//AdvancedCache<?, ?> bcache = cache.getAdvancedCache().withEncoding(IdentityEncoder.class) ;
		Debug.line(cache.get("key"), (byte[])cache.get("key"));
	}
	
	
	@Test
	public void transaction() throws Exception {
		
		// Obtain the default cache
		Cache<String, String> cache = cacheManager.getCache();
		// Obtain the transaction manager
		TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();
		// Perform some operations within a transaction and commit it
		transactionManager.begin();
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		transactionManager.commit();
		// Display the current cache contents
		System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
		// Perform some operations within a transaction and roll it back
		transactionManager.begin();
		cache.put("key1", "value3");
		cache.put("key2", "value4");
		transactionManager.rollback();
		// Display the current cache contents
		System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
		// Stop the cache manager and release all resources
	}
}
