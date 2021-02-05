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

package net.smoofyuniverse.common.environment;

import net.smoofyuniverse.common.Main;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.Translations;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.ProgressTask;
import net.smoofyuniverse.common.task.impl.SimpleProgressTask;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * An helper to update and load dependencies.
 */
public class DependencyManager {
	private static final Logger logger = Logger.get("DependencyManager");
	private static Method addURL;

	private final Application app;
	private final Collection<DependencyInfo> deps;

	/**
	 * Creates a manager.
	 *
	 * @param app  The application.
	 * @param deps The dependencies.
	 */
	public DependencyManager(Application app, DependencyInfo... deps) {
		this(app, Arrays.asList(deps));
	}

	/**
	 * Creates a manager.
	 *
	 * @param app  The application.
	 * @param deps The dependencies.
	 */
	public DependencyManager(Application app, Collection<DependencyInfo> deps) {
		this.app = app;
		this.deps = deps;
	}

	/**
	 * Updates and loads dependencies.
	 * Any missing dependency or error is fatal.
	 */
	public void setup() {
		if (update())
			load();
		else
			this.app.shutdownNow();
	}

	/**
	 * Updates the dependencies using the default directory.
	 *
	 * @return Whether all dependencies has been updated.
	 */
	public boolean update() {
		return update(this.app.getWorkingDirectory().resolve("libraries"));
	}

	/**
	 * Loads the dependencies.
	 * Any error is fatal.
	 */
	public void load() {
		for (DependencyInfo dep : this.deps) {
			try {
				addToSystemClasspath(dep.file);
			} catch (Exception e) {
				logger.error("Failed to load dependency " + dep.name, e);
				this.app.fatalError(e);
			}
		}
	}

	/**
	 * Updates the dependencies.
	 *
	 * @param defaultDir The default directory where dependencies will be saved.
	 * @return Whether all dependencies has been updated.
	 */
	public boolean update(Path defaultDir) {
		if (this.deps.isEmpty())
			return true;

		List<DependencyInfo> toUpdate = new LinkedList<>(this.deps);

		long totalSize = 0;
		Iterator<DependencyInfo> checkIt = toUpdate.iterator();
		while (checkIt.hasNext()) {
			DependencyInfo info = checkIt.next();

			if (info.file == null)
				info.file = IOUtil.getMavenPath(defaultDir, info.name, ".jar");

			if (info.matches())
				checkIt.remove();
			else
				totalSize += info.size;
		}

		if (toUpdate.isEmpty())
			return true;

		long totalSizeF = totalSize;
		Consumer<ProgressTask> consumer = task -> {
			logger.info("Downloading missing dependencies ..");
			task.setTitle(Translations.dependencies_download_title);
			IncrementalListener listener = task.expect(totalSizeF);

			Iterator<DependencyInfo> updateIt = toUpdate.iterator();
			while (updateIt.hasNext()) {
				if (task.isCancelled())
					return;

				DependencyInfo info = updateIt.next();
				logger.info("Downloading dependency " + info.name + " ..");
				task.setMessage(info.name);

				Path dir = info.file.getParent();
				if (!Files.isDirectory(dir)) {
					try {
						Files.createDirectories(dir);
					} catch (IOException e) {
						logger.warn("Failed to create directory " + dir, e);
						continue;
					}
				}

				if (!info.download(this.app.getConnectionConfig(), listener))
					continue;

				if (task.isCancelled())
					return;

				if (info.matches())
					updateIt.remove();
				else
					logger.warn("The downloaded dependency has an incorrect signature.");
			}
		};

		if (this.app.isGUIEnabled())
			Popup.consumer(consumer).title(Translations.dependencies_update_title).submitAndWait();
		else
			new SimpleProgressTask().submit(consumer);

		if (toUpdate.isEmpty())
			return true;

		StringBuilder b = new StringBuilder();
		for (DependencyInfo info : toUpdate)
			b.append("\n- ").append(info.name);

		Popup.error().title(Translations.failed_dependencies_title).message(Translations.failed_dependencies_message.format("list", b.toString())).showAndWait();
		return false;
	}

	/**
	 * Adds the jar file to the system classpath.
	 *
	 * @param jar The path to the jar file.
	 * @throws Exception if any exception occurs.
	 */
	public static void addToSystemClasspath(Path jar) throws Exception {
		if (Main.getInstrumentation() != null) {
			Main.getInstrumentation().appendToSystemClassLoaderSearch(new JarFile(jar.toFile()));
			return;
		}

		URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();

		if (addURL == null) {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);
		}

		addURL.invoke(cl, jar.toUri().toURL());
	}
}
