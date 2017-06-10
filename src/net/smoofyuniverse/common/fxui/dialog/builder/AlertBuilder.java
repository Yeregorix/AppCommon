package net.smoofyuniverse.common.fxui.dialog.builder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.fxui.task.ObservableTask;
import net.smoofyuniverse.common.logger.core.Logger;

public class AlertBuilder extends AbstractBuilder<ButtonType> {
	private static final Logger logger = Application.getLogger("AlertBuilder");
	
	private Consumer<ObservableTask> consumer;
	private ExecutorService executor;
	private ObservableTask task;
	private AlertType type;
	
	public AlertBuilder type(AlertType t) {
		this.type = t;
		return this;
	}
	
	public AlertBuilder task(ObservableTask t) {
		this.task = t;
		return this;
	}
	
	public AlertBuilder executor(ExecutorService e) {
		this.executor = e;
		return this;
	}
	
	public AlertBuilder consumer(Consumer<ObservableTask> c) {
		this.consumer = c;
		return this;
	}
	
	@Override
	public boolean valid() {
		if (this.consumer != null)
			prepareTask();
		return super.valid() && this.type != null;
	}
	
	private void prepareTask() {
		if (this.task == null)
			this.task = new ObservableTask();
		
		if (this.type == null)
			this.type = AlertType.INFORMATION;
		
		if (this.buttonTypes == null)
			this.buttonTypes = new ButtonType[] { ButtonType.CANCEL };
		
		if (this.message == null && this.messageP == null)
			this.messageP = this.task.titleProperty();
		
		if (this.expandable == null)
			this.expandable = createTaskContent();
		
		if (this.executor == null)
			this.executor = Application.get().getExecutor();
	}
	
	private Node createTaskContent() {
		Label msg = new Label();
		ProgressBar p = new ProgressBar();
		p.setMaxWidth(Integer.MAX_VALUE);
		
		msg.textProperty().bind(this.task.messageProperty());
		p.progressProperty().bind(this.task.progressProperty());
		
		return new VBox(5, msg, p);
	}

	@Override
	protected Dialog<ButtonType> provide() {
		return new Alert(this.type);
	}
	
	@Override
	public boolean submitAndWait() {
		validate();
		if (this.task == null && this.type != AlertType.CONFIRMATION)
			throw new UnsupportedOperationException("No task");
		
		if (Platform.isFxApplicationThread()) {
			Dialog<ButtonType> d = build();
			
			if (this.task == null)
				return d.showAndWait().orElse(null) == ButtonType.OK;
			
			this.executor.submit(() -> {
				this.task.setCancelled(false);
				try {
					this.consumer.accept(this.task);
				} catch (Exception e) {
					this.task.cancel();
					logger.error("An exception occurred while executing a task:", e);
				}
				Platform.runLater(d::hide);
			});
			
			if (d.showAndWait().orElse(null) == ButtonType.CANCEL)
				this.task.setCancelled(true);
			return this.task.isCancelled();
		} else {
			AtomicBoolean result = new AtomicBoolean();
			CountDownLatch lock = new CountDownLatch(1);
			Platform.runLater(() -> {
				result.set(submitAndWait());
				lock.countDown();
			});
			try {
				lock.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return result.get();
		}
	}
}