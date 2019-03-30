package util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * A Set implementation that uses == instead of equals() to check for equality.
 * @param <E>	the type of elements maintained by this set
 */
public class IdentityHashSet<E> extends AbstractSet<E> {
	/*
	This implementation basically copies that of HashSet, replacing usages of HashMap
	with IdentityHashMap and skipping a few methods for unused features (e.g. Serialization).
	 */

	private final IdentityHashMap<E, Object> map = new IdentityHashMap<>();

	// dummy object as value
	private static final Object OBJECT = new Object();

	public IdentityHashSet() {}

	public IdentityHashSet(Collection<? extends E> source) { addAll(source); }

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() { return map.size(); }

	@Override
	public boolean isEmpty() { return map.isEmpty(); }

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean add(E e) {
		return map.put(e, OBJECT) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) == OBJECT;
	}

	@Override
	public void clear() {
		map.clear();
	}
}
