package net.bleujin.rcraken.store.infinispan;

import java.util.Collections;
import java.util.Map;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenConfig;

public class ISpanConfig implements CrakenConfig {

	

	private DefaultCacheManager dcm;
	public ISpanConfig(DefaultCacheManager dcm) {
		this.dcm = dcm ;
	}

	public static ISpanConfig memory() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);

		return new ISpanConfig(new DefaultCacheManager(builder.build())) ;
	}

	@Override
	public Craken build() {
		return new ISpanCraken(dcm, Collections.singletonMap(DFT_WORKER_NAME, 3));
	}

	@Override
	public Craken build(Map<String, Integer> workers) {
		return new ISpanCraken(dcm, workers);
	}
}
