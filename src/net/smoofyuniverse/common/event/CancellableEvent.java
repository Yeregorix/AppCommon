package net.smoofyuniverse.common.event;

public interface CancellableEvent extends Event {
	public void setCancelled(boolean v);
}
