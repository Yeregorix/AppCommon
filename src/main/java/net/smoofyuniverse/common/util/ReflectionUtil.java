/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
