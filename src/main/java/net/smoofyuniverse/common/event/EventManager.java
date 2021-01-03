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

package net.smoofyuniverse.common.event;

import net.smoofyuniverse.logger.core.Logger;

import java.util.*;

/**
 * Holds a set of {@link ListenerRegistration}s.
 * Posts {@link Event}s.
 */
public class EventManager {
	private static final Logger logger = Logger.get("EventManager");

	private final Set<ListenerRegistration<?>> listeners = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Registers the {@link ListenerRegistration}.
	 * The listener will receive events from this manager.
	 *
	 * @param listener The listener.
	 * @return Whether the listener wasn't already registered.
	 */
	public boolean register(ListenerRegistration<?> listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener");
		return this.listeners.add(listener);
	}

	/**
	 * Unregisters the {@link ListenerRegistration}.
	 * The listener will no longer receive events from this manager.
	 *
	 * @param listener The listener.
	 * @return Whether the listener was registered.
	 */
	public boolean unregister(ListenerRegistration<?> listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener");
		return this.listeners.remove(listener);
	}

	/**
	 * Gets whether the {@link ListenerRegistration} is registered.
	 *
	 * @param listener The listener.
	 * @return Whether the listener is registered.
	 */
	public boolean isRegistered(ListenerRegistration<?> listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener");
		return this.listeners.contains(listener);
	}

	/**
	 * Posts the event.
	 * This event will be handled by all corresponding listeners in the correct order.
	 *
	 * @param event The event.
	 * @return Whether the event hasn't been cancelled.
	 */
	public boolean postEvent(Event event) {
		if (event == null)
			throw new IllegalArgumentException("event");

		List<ListenerRegistration<?>> listeners = getListeners(event.getClass());
		Collections.sort(listeners);

		for (ListenerRegistration l : listeners) {
			if (event.isCancelled() && l.ignoreCancelled)
				continue;

			try {
				l.listener.handle(event);
			} catch (Exception e) {
				logger.error("Failed to handle event " + event.getClass().getSimpleName(), e);
			}
		}

		return !event.isCancelled();
	}

	/**
	 * Gets all registered listeners for the given event type.
	 *
	 * @param eventType The event type.
	 * @return A list of listeners.
	 */
	public List<ListenerRegistration<?>> getListeners(Class<?> eventType) {
		if (eventType == null)
			throw new IllegalArgumentException("eventType");

		List<ListenerRegistration<?>> list = new ArrayList<>();
		for (ListenerRegistration<?> l : this.listeners) {
			if (l.eventType.isAssignableFrom(eventType))
				list.add(l);
		}
		return list;
	}
}
