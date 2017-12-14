package net.bleujin.study;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import junit.framework.TestCase;

public class TestBaseStudy extends TestCase {

	
	protected RedissonClient redisson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		this.redisson = Redisson.create(config);
	}
	
	@Override
	protected void tearDown() throws Exception {
		redisson.getKeys().deleteByPattern("study.") ;
		
		redisson.shutdown();
		super.tearDown();
	}
	
}
