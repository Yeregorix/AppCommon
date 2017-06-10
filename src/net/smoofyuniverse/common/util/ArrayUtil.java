package net.smoofyuniverse.common.util;

public class ArrayUtil {
	
	public static boolean contains(char[] array, char key) {
		for (char value : array)
			if (value == key)
				return true;
		return false;
	}

	public static <T> boolean contains(T[] array, T key) {
		for (T value : array)
			if (value.equals(key))
				return true;
		return false;
	}
}
