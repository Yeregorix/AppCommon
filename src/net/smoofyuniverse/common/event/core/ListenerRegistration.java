package net.smoofyuniverse.common.event.core;

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.EventListener;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.util.ReflectionUtil;

public class ListenerRegistration<T extends Event> implements Comparable<ListenerRegistration<T>> {
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

	@Override
	public int compareTo(ListenerRegistration<T> o) {
		return order.compareTo(o.order);
	}
}
