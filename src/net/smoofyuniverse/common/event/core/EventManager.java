/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common.event.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.Listener;
import net.smoofyuniverse.common.logger.core.Logger;
import net.smoofyuniverse.common.logger.core.LoggerFactory;

public class EventManager {
	private Set<ListenerRegistration<?>> listenerRegistrations = Collections.newSetFromMap(new IdentityHashMap<>());
	private Set<ObjectRegistration<?>> objectRegistrations = Collections.newSetFromMap(new IdentityHashMap<>());
	
	private Logger logger;
	
	public EventManager(LoggerFactory loggerFactory) {
		this(loggerFactory.provideLogger("EventManager"));
	}
	
	public EventManager(Logger logger) {
		this.logger = logger;
	}
	
	public boolean register(ListenerRegistration<?> l) {
		return l instanceof ObjectRegistration ? this.objectRegistrations.add((ObjectRegistration) l) : this.listenerRegistrations.add(l);
	}
	
	public boolean unregister(ListenerRegistration<?> l) {
		return l instanceof ObjectRegistration ? this.objectRegistrations.remove((ObjectRegistration) l) : this.listenerRegistrations.remove(l);
	}
	
	public boolean isRegistered(ListenerRegistration<?> l) {
		return l instanceof ObjectRegistration ? this.objectRegistrations.contains((ObjectRegistration) l) : this.listenerRegistrations.contains(l);
	}
	
	public List<ObjectRegistration<?>> registerAll(Object obj) {
		List<ObjectRegistration<?>> list = new ArrayList<>();
		
		for (Method method : obj.getClass().getDeclaredMethods()) {
			Listener a = method.getAnnotation(Listener.class);
			if (a != null)
				list.add(new ObjectRegistration<>(obj, method, a.order(), a.ignoreCancelled()));
		}
		
		this.objectRegistrations.addAll(list);
		return list;
	}
	
	public List<ObjectRegistration<?>> unregisterAll(Object obj) {
		List<ObjectRegistration<?>> list = new ArrayList<>();
		
		Iterator<ObjectRegistration<?>> it = this.objectRegistrations.iterator();
		while (it.hasNext()) {
			ObjectRegistration<?> l = it.next();
			if (l.object == obj) {
				it.remove();
				list.add(l);
			}
		}
		return list;
	}
	
	public List<ListenerRegistration<?>> getListeners(Class<?> eventType) {
		List<ListenerRegistration<?>> list = new ArrayList<>();
		
		for (ListenerRegistration<?> l : this.listenerRegistrations) {
			if (l.eventType.isAssignableFrom(eventType))
				list.add(l);
		}
		for (ObjectRegistration<?> l : this.objectRegistrations) {
			if (l.eventType.isAssignableFrom(eventType))
				list.add(l);
		}
		
		return list;
	}
	
	public boolean postEvent(Event e) {
		List<ListenerRegistration<?>> listeners = getListeners(e.getClass());
		Collections.sort(listeners);
		
		for (ListenerRegistration l : listeners) {
			if (e.isCancelled() && l.ignoreCancelled)
				continue;
			try {
				l.listener.handle(e);
			} catch (Throwable t) {
				this.logger.error("An exception occurred while handling event " + e.getClass().getSimpleName(), t);
			}
		}
		
		return !e.isCancelled();
	}
}
