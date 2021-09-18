/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.event;

import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.util.ReflectionUtil;

/**
 * A listener registration.
 * Contains a listener and few properties for the event manager.
 *
 * @param <T> The listener type.
 */
public class ListenerRegistration<T extends Event> implements Comparable<ListenerRegistration<?>> {
	/**
	 * The event type.
	 * All events of this type or assignable to this type will be handled by the listener.
	 */
	public final Class<? extends T> eventType;

	/**
	 * The listener that will handle events.
	 */
	public final EventListener<T> listener;

	/**
	 * The order of execution.
	 * Lower is earlier.
	 */
	public final int order;

	/**
	 * Whether cancelled events should be ignored.
	 */
	public final boolean ignoreCancelled;

	public ListenerRegistration(EventListener<T> listener) {
		this(listener, 0);
	}

	public ListenerRegistration(EventListener<T> listener, int order) {
		this(listener, order, true);
	}

	public ListenerRegistration(EventListener<T> listener, int order, boolean ignoreCancelled) {
		this((Class<T>) ReflectionUtil.getTypeArguments(listener.getClass(), EventListener.class)[0], listener, order, ignoreCancelled);
	}

	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener, int order, boolean ignoreCancelled) {
		this.eventType = eventType;
		this.listener = listener;
		this.order = order;
		this.ignoreCancelled = ignoreCancelled;
	}

	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener) {
		this(eventType, listener, 0);
	}

	public ListenerRegistration(Class<? extends T> eventType, EventListener<T> listener, int order) {
		this(eventType, listener, order, true);
	}

	/**
	 * Registers this listener in {@link ApplicationManager}'s event manager.
	 *
	 * @return Whether this listener wasn't already registered.
	 */
	public final boolean register() {
		return ApplicationManager.get().getEventManager().register(this);
	}

	@Override
	public int compareTo(ListenerRegistration<?> o) {
		return Integer.compare(this.order, o.order);
	}
}
