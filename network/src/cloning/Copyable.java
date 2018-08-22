package cloning;

import java.util.IdentityHashMap;

/**
 * Copyable objects can be deep cloned.
 */
public interface Copyable<T> {
	/**
	 * Copies this object. This method does not handle circular references.
	 * @return	a copy of this object
	 */
	default T copy() {
		return copy(new IdentityHashMap<>());
	}

	/**
	 * Copies this object. This method should properly handle any circular references.
	 * @param clones	previously cloned objects
	 * @return	a copy of this object
	 */
	T copy(IdentityHashMap<Object, Object> clones);
}
