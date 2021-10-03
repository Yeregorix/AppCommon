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

package net.smoofyuniverse.common.app;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.util.ImageUtil;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public abstract class Application {
	private static final Logger logger = ApplicationLogger.get(Application.class);

	ApplicationManager manager;
	private Stage stage;

	public final ApplicationManager getManager() {
		return this.manager;
	}

	/**
	 * Initialization final step.
	 *
	 * @throws Exception if any exception occurs.
	 */
	public void init() throws Exception {}

	/**
	 * Application main action.
	 * Starts stage.
	 *
	 * @throws Exception if any exception occurs.
	 */
	public abstract void run() throws Exception;

	protected Stage createStage(double width, double height, String... icons) {
		Image[] images = new Image[icons.length];
		for (int i = 0; i < icons.length; i++)
			images[i] = ImageUtil.loadImage(icons[i]);
		return createStage(width, height, images);
	}

	protected Stage createStage(double width, double height, Image... icons) {
		Stage stage = createStage(width, height);
		stage.getIcons().addAll(icons);
		return stage;
	}

	protected Stage createStage(double width, double height) {
		Stage stage = new Stage();
		stage.setTitle(getManager().getTitle() + " " + getManager().getVersion());
		stage.setWidth(width);
		stage.setHeight(height);
		return stage;
	}

	/**
	 * Gets the stage.
	 *
	 * @return The stage.
	 */
	public Optional<Stage> getStage() {
		return Optional.ofNullable(this.stage);
	}

	protected void setStage(Stage stage) {
		this.stage = stage;
		if (stage != null)
			stage.setOnCloseRequest(e -> getManager().shutdown());
	}

	/**
	 * Gets the application.
	 *
	 * @return The application.
	 */
	public static Application get() {
		return ApplicationManager.get().getApplication().get();
	}

	/**
	 * Delegates to {@link Platform#runLater(Runnable)} and blocks until execution is complete.
	 *
	 * @param runnable The runnable.
	 */
	public static void runLater(Runnable runnable) {
		CountDownLatch lock = new CountDownLatch(1);

		Platform.runLater(() -> {
			try {
				runnable.run();
			} finally {
				lock.countDown();
			}
		});

		try {
			lock.await();
		} catch (InterruptedException e) {
			logger.error("Interruption", e);
		}
	}
}
