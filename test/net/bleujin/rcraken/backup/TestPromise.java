package net.bleujin.rcraken.backup;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Fqn;
import net.ion.framework.promise.Deferred;
import net.ion.framework.promise.Promise;
import net.ion.framework.promise.impl.DeferredObject;
import net.ion.framework.util.Debug;

public class TestPromise {

	
	@Test
	public void progress() {
		
		Multiplier m = new Multiplier(5);
		m.promise().progress(System.out::println).done((res) -> System.out.println("the result is: " + res));
		m.multiplyNTimes(4);
		
	}
	
	@Test
	public void fqnPattern() {
		Debug.line( Fqn.from("/a/b/c/d/e/f").isPattern("/*")) ;
	}
	
}


class Multiplier {
	private final long factor;
	private final Deferred<Long, Long, String> deferred = new DeferredObject<>();
 
	public Multiplier(long factor) {
		this.factor = factor;
	}
 
	public long multiplyNTimes(int rounds) {
		long result = 1;
		for (int i = 1; i <= rounds; i++) {
			deferred.notify("status: " + (i * 100 / rounds) + "%");
			result *= factor;
		}
		deferred.resolve(result);
		return result;
	}
 
	public Promise<Long, Long, String> promise() {
		return deferred.promise();
	}
}