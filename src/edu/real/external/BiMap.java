package edu.real.external;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiMap<K, V>
{
	Map<K, V> fwd;
	Map<V, K> bwd;

	public BiMap()
	{
		fwd = new HashMap<K, V>();
		bwd = new HashMap<V, K>();
	}

	public V put(K key, V value)
	{
		fwd.put(key, value);
		bwd.put(value, key);
		return value;
	}

	public K getKey(V value)
	{
		return bwd.get(value);
	}

	public V get(K key)
	{
		return fwd.get(key);
	}

	public Collection<V> values()
	{
		return fwd.values();
	}

	public Set<K> keys()
	{
		return fwd.keySet();
	}

	public V pop(K k)
	{
		V v = fwd.remove(k);
		bwd.remove(v);
		return v;
	}
}
