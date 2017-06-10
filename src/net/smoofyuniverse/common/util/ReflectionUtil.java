package net.smoofyuniverse.common.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {
	
	public static Class<?>[] getTypeArguments(Class<?> cl, Class<?> targetInterface) {
		for (Type type : cl.getGenericInterfaces()) {
			if (targetInterface.isAssignableFrom(getClass(type)))
				return getTypeArguments(type);
		}
		return new Class<?>[0];
	}
	
	public static Class<?>[] getTypeArguments(Type type) {
		if (!(type instanceof ParameterizedType))
			throw new IllegalArgumentException("Class must be parameterized");
		Type[] args = ((ParameterizedType) type).getActualTypeArguments();
		Class<?>[] params = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++)
			params[i] = getClass(args[i]);
		return params;
	}
	
	public static Class<?> getClass(Type type) {
		if (type instanceof Class)
			return (Class<?>) type;
		if (type instanceof ParameterizedType)
			return getClass(((ParameterizedType) type).getRawType());
		if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			return Array.newInstance(componentClass, 0).getClass();
		}
		throw new IllegalArgumentException("Unknown type");
	}
}
