/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.event.core;

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.Order;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectRegistration<T extends Event> extends ListenerRegistration<T> {
	public final Object object;
	public final Method method;
	
	public ObjectRegistration(Object object, Method method, Order order, boolean ignoreCancelled) {
		this((Class<T>) method.getParameterTypes()[0], object, method, order, ignoreCancelled);
	}

	public ObjectRegistration(Class<? extends T> eventType, Object object, Method method, Order order, boolean ignoreCancelled) {
		super(eventType, (event) -> {
			try {
				method.invoke(object, event);
			} catch (InvocationTargetException e) {
				throw (Exception) e.getCause();
			}
		}, order, ignoreCancelled);
		
		this.object = object;
		this.method = method;
		this.method.setAccessible(true);
	}
}
