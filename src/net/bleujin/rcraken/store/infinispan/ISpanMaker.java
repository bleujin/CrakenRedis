package net.bleujin.rcraken.store.infinispan;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

import net.bleujin.rcraken.store.MapConfig;

public class ISpanMaker {

	public static ISpanConfig localMemory() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
		
		return new ISpanConfig(new DefaultCacheManager(builder.build()));
	}

}
