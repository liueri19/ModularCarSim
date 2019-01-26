package util;

import java.util.IdentityHashMap;

@Deprecated(forRemoval = true)
public final class Copier {
	public static <T extends DeepCopyable<?>> T deepCopy(T obj) {
		return (T) obj.copy();
	}

	public static <T extends DeepCopyable<?>> T deepCopy(T obj,
														 IdentityHashMap<Object, Object> clones) {
		if (clones.containsKey(obj))
			return (T) clones.get(obj);

		return (T) obj.copy(clones, new IdentityHashSet<>());
	}

	public static <T extends DeepCopyable<?>> T deepCopy(T obj,
														 IdentityHashMap<Object, Object> clones,
														 IdentityHashSet<Object> cloning) {
		if (clones.containsKey(obj))
			return (T) clones.get(obj);

		return (T) obj.copy(clones, cloning);
	}
}
