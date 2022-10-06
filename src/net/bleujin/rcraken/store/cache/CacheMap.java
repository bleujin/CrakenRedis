package net.bleujin.rcraken.store.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;

public class CacheMap<K, V>  {

	private Map<K, V> inner ;
	public CacheMap(int maxSize) {
		this.inner = new LRUMap(maxSize) ;
	}

	public synchronized V get(K k, FutureValue<V> call) {
		V val = get(k) ;
		if (val == null) {
			val = call.handle() ;
			put(k, val) ;
		}
		return val ;
	}
	



	public void clear() {
		inner.clear();
	}

	public boolean containsKey(K key) {
		return inner.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return inner.containsValue(value);
	}

	public Set<Entry<K, V>> entrySet() {
		return inner.entrySet();
	}

	public V get(K key) {
		return inner.get(key);
	}

	public boolean isEmpty() {
		return inner.isEmpty();
	}

	public Set<K> keySet() {
		return inner.keySet();
	}

	public V put(K key, V value) {
		return inner.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		inner.putAll(m);
	}

	public V remove(K key) {
		return inner.remove(key);
	}

	public int size() {
		return inner.size();
	}

	public Collection<V> values() {
		return inner.values();
	}
	
	
}


