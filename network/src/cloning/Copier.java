package cloning;

import java.util.IdentityHashMap;

public class Copier {
	public static <T extends Copyable<?>> T deepCopy(T obj, IdentityHashMap<Object, Object> clones) {
		if (clones.containsKey(obj))
			return (T) clones.get(obj);

		return (T) obj.copy(clones);
	}
}
