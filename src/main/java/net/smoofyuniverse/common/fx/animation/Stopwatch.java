/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common.fx.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * A stopwatch.
 */
public class Stopwatch {
	private final StringProperty text = new SimpleStringProperty(format(0));
	private final Timeline timeline;
	private final long interval;
	private long total;

	/**
	 * Creates a stopwatch that updates at regular intervals.
	 *
	 * @param interval The interval in milliseconds.
	 */
	public Stopwatch(long interval) {
		this.interval = interval;
		this.total = 0;

		this.timeline = new Timeline();
		this.timeline.getKeyFrames().add(new KeyFrame(Duration.millis(interval), e -> increment()));
		this.timeline.setCycleCount(Animation.INDEFINITE);
	}

	/**
	 * Gets the stopwatch text property.
	 *
	 * @return The stopwatch text property.
	 */
	public ReadOnlyStringProperty textProperty() {
		return this.text;
	}

	/**
	 * Gets the stopwatch text.
	 *
	 * @return The stopwatch text.
	 */
	public String getText() {
		return this.text.get();
	}

	/**
	 * Starts the stopwatch.
	 */
	public void start() {
		this.total = 0;
		this.timeline.playFromStart();
	}

	/**
	 * Pauses the stopwatch.
	 */
	public void pause() {
		this.timeline.pause();
	}

	/**
	 * Resumes the stopwatch.
	 */
	public void resume() {
		this.timeline.play();
	}

	/**
	 * Stops the stopwatch.
	 */
	public void stop() {
		this.timeline.stop();
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

	protected String format(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		millis -= seconds * 1000;
		seconds -= minutes * 60;
		minutes -= hours * 60;

		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}
}
