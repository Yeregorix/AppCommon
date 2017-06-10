package net.smoofyuniverse.common.fxui.task;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

public class Chrono {
	private StringProperty text = new SimpleStringProperty(format(0));
	private Timeline timeline;
	private long interval, total;
	
	public Chrono(long millis) {
		this.interval = millis;
		this.total = 0;
		
		this.timeline = new Timeline();
		this.timeline.getKeyFrames().add(new KeyFrame(Duration.millis(millis), (e) -> increment()));
		this.timeline.setCycleCount(Animation.INDEFINITE);
	}
	
	public ReadOnlyStringProperty textProperty() {
		return this.text;
	}
	
	public String getText() {
		return this.text.get();
	}
	
	public void start() {
		this.total = 0;
		this.timeline.playFromStart();
	}
	
	public void pause() {
		this.timeline.pause();
	}
	
	public void stop() {
		pause();
		this.total = 0;
		update();
	}
	
	private void increment() {
		this.total += this.interval;
		update();
	}
	
	private void update() {
		this.text.set(format(this.total));
	}
	
	public String format(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		
		millis -= seconds * 1000;
		seconds -= minutes * 60;
		minutes -= hours * 60;
		
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}
}
