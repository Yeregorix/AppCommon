package net.smoofyuniverse.common.fxui.task;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.State;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.listener.ListenerProvider;
import net.smoofyuniverse.common.listener.ObservableListener;

public final class ObservableTask implements ListenerProvider {
	private static final Set<ObservableTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());
	
	private AtomicReference<String> titleUpdate = new AtomicReference<String>();
	private AtomicReference<String> messageUpdate = new AtomicReference<String>();
	private AtomicReference<Double> progressUpdate = new AtomicReference<Double>();
	
	private AtomicReference<Boolean> cancellableUpdate = new AtomicReference<Boolean>();
	private AtomicReference<Boolean> cancelledUpdate = new AtomicReference<Boolean>();
	
	private StringProperty title = new SimpleStringProperty();
	private StringProperty message = new SimpleStringProperty();
	private DoubleProperty progress = new SimpleDoubleProperty(-1);
	
	private BooleanProperty cancellable = new SimpleBooleanProperty(true);
	private BooleanProperty cancelled = new SimpleBooleanProperty(false);

	public ObservableTask() {
		tasks.add(this);
	}

	public StringProperty titleProperty() {
		return this.title;
	}

	public StringProperty messageProperty() {
		return this.message;
	}

	public DoubleProperty progressProperty() {
		return this.progress;
	}
	
	public BooleanProperty cancellableProperty() {
		return this.cancellable;
	}
	
	public BooleanProperty cancelledProperty() {
		return this.cancelled;
	}

	public void setTitle(String value) {
		if (Platform.isFxApplicationThread())
			this.title.set(value);
		else if (this.titleUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.title.set(this.titleUpdate.getAndSet(null)));
	}

	public void setMessage(String value) {
		if (Platform.isFxApplicationThread())
			this.message.set(value);
		else if (this.messageUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.message.set(this.messageUpdate.getAndSet(null)));
	}

	public void setProgress(double value) {
		if (Platform.isFxApplicationThread())
			this.progress.set(value);
		else if (this.progressUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.progress.set(this.progressUpdate.getAndSet(null)));
	}
	
	public void setCancellable(boolean value) {
		if (Platform.isFxApplicationThread())
			this.cancellable.set(value);
		else if (this.cancellableUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.cancellable.set(this.cancellableUpdate.getAndSet(null)));
	}
	
	public void setCancelled(boolean value) {
		if (Platform.isFxApplicationThread())
			this.cancelled.set(value);
		else if (this.cancelledUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.cancelled.set(this.cancelledUpdate.getAndSet(null)));
	}
	
	public boolean isCancelled() {
		return this.cancelled.get();
	}
	
	public void cancel() {
		setCancelled(true);
	}
	
	@Override
	public ObservableListener provide(long expectedTotal) {
		return new ObservableListener(this, expectedTotal);
	}
	
	static {
		Application.registerListener(State.SHUTDOWN.newListener((e) -> tasks.forEach(ObservableTask::cancel), Order.EARLY));
	}
}