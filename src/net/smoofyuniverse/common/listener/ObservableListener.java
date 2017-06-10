package net.smoofyuniverse.common.listener;

import net.smoofyuniverse.common.fxui.task.ObservableTask;

public class ObservableListener implements BasicListener {
	public final ObservableTask task;
	public final long expectedTotal;
	private long total = 0;
	
	public ObservableListener(ObservableTask task, long expectedTotal) {
		this.task = task;
		this.expectedTotal = expectedTotal;
		this.task.setProgress(expectedTotal == -1 ? -1 : 0);
	}
	
	public long getTotal() {
		return this.total;
	}
	
	@Override
	public boolean isCancelled() {
		return this.task.isCancelled();
	}

	@Override
	public void increment(long v) {
		this.total += v;
		if (this.expectedTotal != -1)
			this.task.setProgress(this.total / (double) this.expectedTotal);
	}
	
	@Override
	public void setMessage(String v) {
		this.task.setMessage(v);
	}
}
