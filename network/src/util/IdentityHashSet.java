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
	private final IdentityHashMap<E, E> map = new IdentityHashMap<>();

	public IdentityHashSet() {}

	public IdentityHashSet(Collection<? extends E> source) {
		for (E e : source) map.put(e, e);
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() { return map.size(); }

	@Override
	public boolean add(E e) {
		map.put(e, e);
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
}
