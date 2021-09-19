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
import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.impl.SimpleIncrementalListener;
import net.smoofyuniverse.common.util.IOUtil;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.jar.JarFile;

/**
 * An helper to update and load dependencies.
 */
public class DependencyManager {
	protected static final Logger logger = ApplicationLogger.get(DependencyManager.class);
	private static Method addURL;

	protected final ApplicationManager app;
	protected final Collection<DependencyInfo> dependencies;

	/**
	 * Creates a dependency manager.
	 *
	 * @param app          The application.
	 * @param dependencies The dependencies.
	 */
	protected DependencyManager(ApplicationManager app, Collection<DependencyInfo> dependencies) {
		this.app = app;
		this.dependencies = dependencies;
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
	 * @return Whether all dependencies have been updated.
	 */
	public boolean update() {
		return update(this.app.getWorkingDirectory().resolve("libraries"));
	}

	/**
	 * Updates the dependencies.
	 *
	 * @param defaultDir The default directory where dependencies will be saved.
	 * @return Whether all dependencies have been updated.
	 */
	public boolean update(Path defaultDir) {
		if (this.dependencies.isEmpty())
			return true;

		List<DependencyInfo> deps = new LinkedList<>(this.dependencies);

		long totalSize = 0;
		Iterator<DependencyInfo> it = deps.iterator();
		while (it.hasNext()) {
			DependencyInfo info = it.next();

			if (!info.isCompatible()) {
				it.remove();
				continue;
			}

			if (info.file == null)
				info.file = IOUtil.getMavenPath(defaultDir, info.name, ".jar");

			if (info.matches())
				it.remove();
			else
				totalSize += info.size;
		}

		if (deps.isEmpty())
			return true;

		download(deps, totalSize);

		if (deps.isEmpty())
			return true;

		failed(deps);
		return false;
	}

	protected void download(List<DependencyInfo> deps, long totalSize) {
		JLabel label = new JLabel();
		JOptionPane pane = new JOptionPane(label, JOptionPane.INFORMATION_MESSAGE);
		pane.setOptions(new Object[0]);

		JDialog dialog = new JDialog((Frame) null, "Dependencies update");
		dialog.setContentPane(pane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		dialog.setLocationRelativeTo(null);

		logger.info("Downloading missing dependencies ...");
		IncrementalListener listener = new SimpleIncrementalListener(0);

		Iterator<DependencyInfo> it = deps.iterator();
		while (it.hasNext()) {
			DependencyInfo dep = it.next();
			logger.info("Downloading dependency " + dep.name + " ...");
			label.setText(dep.name);
			dialog.pack();

			if (!dep.createParent() || !dep.download(this.app.getConnectionConfig(), listener))
				continue;

			if (dep.matches())
				it.remove();
			else
				logger.warn("The downloaded dependency has an incorrect signature.");
		}

		dialog.dispose();
	}

	protected void failed(List<DependencyInfo> deps) {}

	/**
	 * Loads the dependencies.
	 * Any error is fatal.
	 */
	public void load() {
		for (DependencyInfo dep : this.dependencies) {
			if (dep.isCompatible()) {
				try {
					addToSystemClasspath(dep.file);
				} catch (Exception e) {
					logger.error("Failed to load dependency " + dep.name, e);
					this.app.fatalError(e);
				}
			}
		}
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

	/**
	 * Creates a dependency manager.
	 * This manager uses JavaFX if loaded, Swing otherwise.
	 *
	 * @param app          The application manager.
	 * @param dependencies The dependencies.
	 * @return The dependency manager.
	 */
	public static DependencyManager create(ApplicationManager app, DependencyInfo... dependencies) {
		return create(app, Arrays.asList(dependencies));
	}

	/**
	 * Creates a dependency manager.
	 * This manager uses JavaFX if loaded, Swing otherwise.
	 *
	 * @param app          The application manager.
	 * @param dependencies The dependencies.
	 * @return The dependency manager.
	 */
	public static DependencyManager create(ApplicationManager app, Collection<DependencyInfo> dependencies) {
		if (app.isJavaFXLoaded())
			return new DependencyManagerFX(app, dependencies);
		return new DependencyManager(app, dependencies);
	}
}
