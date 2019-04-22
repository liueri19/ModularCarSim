package util;

import logging.Logger;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * DeepCopyable objects can be deep cloned.
 * Implementations of this interface are expected to produce objects with no reference
 * to original objects, direct or indirect, through the use of the IdentityHashMap that
 * stores the clones and the IdentityHashSet that stores the object currently being
 * cloned.
 */
public interface DeepCopyable<T extends DeepCopyable<T>> {
	/**
	 * Copies this object. This method assumes that this is the root object.
	 * @return	a copy of this object
	 */
	default T copy() {
		final var clones = new IdentityHashMap<>();
		final T clone = copy(clones, new IdentityHashSet<>());
		clones.forEach((source, copy) -> ((T) copy).fixNulls((T) source, clones));
		return clone;
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
	 * Removes all null elements in the cloned collection and add cloned counterpart of
	 * missing objects from clonesMap map to the cloned collection. An object is
	 * considered missing if it is present in the original but not in cloned. Equality is
	 * checked using the objects' equals method.
	 * @see #fixCollection(Collection, Collection, IdentityHashMap, BiPredicate)
	 */
	static <T extends DeepCopyable<T>> void fixCollection(
			Collection<? extends T> original,
			Collection<T> cloned,
			IdentityHashMap<Object, Object> clonesMap) {
		fixCollection(original, cloned, clonesMap, Objects::equals);
	}

	/**
	 * Removes all null elements in the cloned collection and add cloned counterpart of
	 * missing objects from clonesMap map to the cloned collection. An object is
	 * considered missing if it is present in the original but not in cloned. Equality is
	 * checked using the specified comparator.
	 * @see #fixCollection(Collection, Collection, IdentityHashMap)
	 */
	static <T extends DeepCopyable<T>> void fixCollection(
			Collection<? extends T> original,
			Collection<T> cloned,
			IdentityHashMap<Object, Object> clonesMap,
			BiPredicate<? super T, ? super T> comparator) {

		// remove nulls
		cloned.removeIf(Objects::isNull);

		// for every element in original collection
		for (T e : original) {
			// if absent from cloned collection
			if (cloned.stream().noneMatch(obj -> comparator.test(e, obj)))
				// find a copy and add it
				cloned.add(e.copy(clonesMap, new IdentityHashSet<>()));
		}

		// DEBUG
		if (original.size() != cloned.size()) {
			Logger.logln("original and clone have unequal sizes");
//			Logger.logln("cloned: " + cloned);
//			Logger.logln("original: " + original);
//			Logger.logln("clonesMap: " + clonesMap);
		}
	}
}
