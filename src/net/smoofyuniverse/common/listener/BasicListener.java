package net.smoofyuniverse.common.listener;

import net.smoofyuniverse.common.app.Application;

public interface BasicListener extends ListenerProvider {
	public static final BasicListener DEFAULT = (v) -> {};
	
	public default boolean isCancelled() {
		return Application.isShutdown();
	}
	
	public void increment(long v);
	
	public default void setMessage(String v) {}
	
	@Override
	public default BasicListener provide(long expectedTotal) { return this; }
}
