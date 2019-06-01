/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
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

import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.EventListener;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.util.ReflectionUtil;

public class ListenerRegistration<T extends Event> implements Comparable<ListenerRegistration<?>> {
	public final Class<? extends T> eventType;
	public final EventListener<T> listener;
	public final Order order;
	public final boolean ignoreCancelled;
	
	public ListenerRegistration(EventListener<T> listener) {
		this(listener, Order.DEFAULT);
	}
	
	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener) {
		this(eventType, listener, Order.DEFAULT);
	}
	
	public ListenerRegistration(EventListener<T> listener, Order order) {
		this(listener, order, true);
	}
	
	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener, Order order) {
		this(eventType, listener, order, true);
	}
	
	public ListenerRegistration(EventListener<T> listener, Order order, boolean ignoreCancelled) {
		this((Class<T>) ReflectionUtil.getTypeArguments(listener.getClass(), EventListener.class)[0], listener, order, ignoreCancelled);
	}
	
	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener, Order order, boolean ignoreCancelled) {
		this.eventType = eventType;
		this.listener = listener;
		this.order = order;
		this.ignoreCancelled = ignoreCancelled;
	}

	public final void register() {
		App.get().getEventManager().register(this);
	}

	@Override
	public int compareTo(ListenerRegistration<?> o) {
		return this.order.compareTo(o.order);
	}
}
