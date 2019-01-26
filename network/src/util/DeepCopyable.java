package util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Objects;

/**
 * DeepCopyable objects can be deep cloned.
 * This interface does not provide a copy method. Implementations of this method must
 * provide a copy constructor that takes the original object, the IdentityHashMap that
 * stores the clones, and the IdentityHashSet that stores the object currently being
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


	static <T extends DeepCopyable<T>> void fixCollection(
			Collection<T> original, Collection<T> clone,
			IdentityHashMap<Object, Object> clones) {
		clone.removeIf(Objects::isNull);
		for (T e : original) {
			if (!clone.contains(e))
				clone.add(e.copy(clones, new IdentityHashSet<>()));
		}

		// DEBUG
		if (original.size() != clone.size()) {
			System.out.println("DEBUG: clone and original collections have unequal sizes");
			System.out.printf("DEBUG: clone: %s%n", clone);
			System.out.printf("DEBUG: original: %s%n", original);
			System.out.printf("DEBUG: clones: %s%n", clones);
		}
	}
}
