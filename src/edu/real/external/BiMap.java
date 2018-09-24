package edu.real.external;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BiMap<K, V>
{
	Map<K, V> fwd;
	Map<V, K> bwd;

	public BiMap()
	{
		this.fwd = new HashMap<K, V>();
		this.bwd = new HashMap<V, K>();
	}

	public V put(K key, V value)
	{
		this.fwd.put(key, value);
		this.bwd.put(value, key);
		return value;
	}

	public K getKey(V value)
	{
		return this.bwd.get(value);
	}

	public V get(K key)
	{
		return this.fwd.get(key);
	}

	public Collection<V> values()
	{
		return this.fwd.values();
	}
}
