package net.smoofyuniverse.common.listener;

public interface ListenerProvider {
	public BasicListener provide(long expectedTotal);
}
