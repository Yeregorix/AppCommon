package net.smoofyuniverse.common.event;

public interface Event {
	public default boolean isCancelled() { return false; };
}
