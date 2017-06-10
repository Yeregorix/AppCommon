package net.smoofyuniverse.common.event.app;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.State;
import net.smoofyuniverse.common.event.Event;

public class ApplicationStateEvent implements Event {
	public final Application application;
	public final State prevState, newState;
	
	public ApplicationStateEvent(Application app, State prevState, State newState) {
		this.application = app;
		this.prevState = prevState;
		this.newState = newState;
	}
}
