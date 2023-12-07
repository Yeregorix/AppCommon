/*
 * Copyright (c) 2017-2023 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.environment;

import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.environment.source.ReleaseSource;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.task.ProgressTask;
import net.smoofyuniverse.common.util.ProcessUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.smoofyuniverse.common.app.Translations.t;

/**
 * A helper to check and apply application updates.
 */
public class ApplicationUpdater {
	private static final Logger logger = ApplicationLogger.get(ApplicationUpdater.class);

	private final ApplicationManager app;
	private final ReleaseSource appSource, updaterSource;

	private Path appJar;
	private ReleaseInfo latestApp, latestUpdater;

	/**
	 * Creates an updater.
	 *
	 * @param app           The application.
	 * @param appSource     The application release source.
	 * @param updaterSource The updater release source.
	 */
	public ApplicationUpdater(ApplicationManager app, ReleaseSource appSource, ReleaseSource updaterSource) {
		this.app = app;
		this.appSource = appSource;
		this.updaterSource = updaterSource;
	}

	/**
	 * Checks whether an update is available.
	 * If available and accepted by the user, updates the application.
	 */
	public void run() {
		if (isUpdateAvailable() && notifyUpdate())
			applyUpdate();
	}

	/**
	 * Checks whether an update is available.
	 *
	 * @return Whether an update is available.
	 */
	public boolean isUpdateAvailable() {
		this.appJar = this.app.getApplicationJar().orElse(null);
		if (this.appJar == null)
			return false;

		this.latestApp = this.appSource.getLatestRelease().orElse(null);
		if (this.latestApp == null || this.latestApp.matches(this.appJar))
			return false;

		this.latestUpdater = this.updaterSource.getLatestRelease().orElse(null);
		return this.latestUpdater != null;
	}

	/**
	 * Notifies the user that an update is available.
	 *
	 * @return Whether the update should be applied.
	 */
	public boolean notifyUpdate() {
		return Popup.confirmation().title(t("update.available.title")).message(t("update.available.message")).submitAndWait();
	}

	/**
	 * Updates the application.
	 */
	public void applyUpdate() {
		if (this.appJar == null || this.latestApp == null || this.latestUpdater == null)
			throw new IllegalStateException();

		Consumer<ProgressTask> consumer = task -> {
			logger.info("Starting application update task ...");
			task.setTitle(t("update.download.title"));

			Path updaterJar = this.app.getDirectory().resolve("Updater.jar");
			if (!this.latestUpdater.matches(updaterJar)) {
				logger.info("Downloading latest updater ...");
				this.latestUpdater.download(updaterJar, this.app.getConnectionConfig(), task);

				if (task.isCancelled())
					return;

				if (!this.latestUpdater.matches(updaterJar)) {
					task.cancel();
					logger.error("Updater file seems invalid, aborting ...");
					Popup.error().title(t("update.cancelled")).message(t("update.signature.invalid.updater")).show();
				}
			}

			if (task.isCancelled())
				return;

			Path appUpdateJar = this.app.getDirectory().resolve(this.app.getName() + "-Update.jar");
			if (!this.latestApp.matches(appUpdateJar)) {
				logger.info("Downloading latest application update ...");
				this.latestApp.download(appUpdateJar, this.app.getConnectionConfig(), task);

				if (task.isCancelled())
					return;

				if (!this.latestApp.matches(appUpdateJar)) {
					task.cancel();
					logger.error("Application update file seems invalid, aborting ...");
					Popup.error().title(t("update.cancelled")).message(t("update.signature.invalid.app")).show();
				}
			}

			if (task.isCancelled())
				return;

			logger.info("Starting updater process ...");
			task.setTitle(t("update.process.title"));
			task.setMessage(t("update.process.message"));
			task.setProgress(-1);

			boolean launch = !this.app.getArguments().getBoolean("noUpdateLaunch");
			if (!launch)
				logger.info("The updater will only apply the modifications. You will have to restart the application manually.");

			List<String> cmd = new ArrayList<>();
			cmd.add("java");
			cmd.add("-jar");
			cmd.add(updaterJar.toAbsolutePath().toString());
			cmd.add("1"); // version
			cmd.add(appUpdateJar.toAbsolutePath().toString()); // source
			cmd.add(this.appJar.toAbsolutePath().toString()); // target
			cmd.add(String.valueOf(launch)); // launch
			this.app.getOriginalArguments().export(cmd::add);

			if (task.isCancelled())
				return;

			try {
				ProcessUtil.builder().command(cmd).start();
			} catch (IOException e) {
				task.cancel();
				logger.error("Failed to start updater process", e);
				Popup.error().title(t("update.cancelled")).message(t("update.process.error")).show();
			}
		};

		boolean success = Popup.consumer(consumer).title(t("update.title")).submitAndWait();
		if (success)
			this.app.shutdownNow();
		else
			logger.info("Update task has been cancelled.");
	}
}
