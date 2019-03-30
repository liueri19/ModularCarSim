package util;

import logging.Logger;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Objects;

/**
 * DeepCopyable objects can be deep cloned.
 * Implementations of this interface are expected to produce objects with no reference
 * to original objects, direct or indirect, through the use of the IdentityHashMap that
 * stores the clones and the IdentityHashSet that stores the object currently being
 * cloned.
 */
public interface DeepCopyable<T> {
	/**
	 * Copies this object. This method assumes that this is the root object.
	 * @return	a copy of this object
	 */
	default T copy() {
		return copy(new IdentityHashMap<>(), new IdentityHashSet<>());
	}

	/**
	 * Copies this object. This method should properly handle any circular references.
	 * @param clones	map of original object to cloned objects
	 * @return	a copy of this object
	 */
	T copy(IdentityHashMap<Object, Object> clones, IdentityHashSet<Object> cloning);

	/**
	 * Fixes the null references created when encountering circular references.
	 * @param original	the original object being copied
	 * @param clones	map of original object to cloned objects
	 */
	void fixNulls(T original, IdentityHashMap<Object, Object> clones);


	/**
	 * Removes all null elements in the cloned collection and add cloned counter part of
	 * missing objects from clonesMap map to the cloned collection. An object is
	 * considered missing if it is present in the original but not in cloned.
	 */
	static <T extends DeepCopyable<T>> void fixCollection(
			Collection<? extends T> original,
			Collection<? super T> cloned,
			IdentityHashMap<Object, Object> clonesMap) {
		cloned.removeIf(Objects::isNull);
		for (T e : original) {
			if (!cloned.contains(e))
				cloned.add(e.copy(clonesMap, new IdentityHashSet<>()));
		}

		// DEBUG
		if (original.size() != cloned.size()) {
			Logger.logln("original and clone have unequal sizes");
			Logger.logf("cloned: %s%n", cloned);
			Logger.logf("original: %s%n", original);
			Logger.logf("clonesMap: %s%n", clonesMap);
		}
	}
}
