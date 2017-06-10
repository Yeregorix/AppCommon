package net.smoofyuniverse.common.app;

import net.smoofyuniverse.common.event.EventListener;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.event.app.ApplicationStateEvent;
import net.smoofyuniverse.common.event.core.ListenerRegistration;

public enum State {
	CREATION, SERVICES_INIT, STAGE_INIT, RUNNING, SHUTDOWN;
	
	public ListenerRegistration<ApplicationStateEvent> newListener(EventListener<ApplicationStateEvent> listener, Order order) {
		return new ListenerRegistration<>(ApplicationStateEvent.class, (e) -> {
			if (e.newState == State.SHUTDOWN)
				listener.handle(e);
		}, Order.EARLY);
	}
}
