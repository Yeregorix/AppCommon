package net.smoofyuniverse.common.event.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.Order;

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
	}
}
