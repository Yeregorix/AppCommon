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

package net.smoofyuniverse.common.app;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import javafx.application.Platform;
import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.environment.ApplicationUpdater;
import net.smoofyuniverse.common.environment.DependencyInfo;
import net.smoofyuniverse.common.environment.DependencyManager;
import net.smoofyuniverse.common.environment.source.GitHubReleaseSource;
import net.smoofyuniverse.common.environment.source.ReleaseSource;
import net.smoofyuniverse.common.event.EventManager;
import net.smoofyuniverse.common.event.app.ApplicationLocaleChangeEvent;
import net.smoofyuniverse.common.event.app.ApplicationStateChangeEvent;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.platform.OperatingSystem;
import net.smoofyuniverse.common.task.BaseListener;
import net.smoofyuniverse.common.util.ResourceLoader;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The application.
 * Singleton.
 */
public class ApplicationManager {
	private static final Logger logger = ApplicationLogger.get(ApplicationManager.class);
	private static ApplicationManager instance;
	private final Arguments originalArguments;
	private final ExecutorService executor;
	private final ResourceLoader resourceLoader;
	private final boolean devEnvironment;
	private final Set<BaseListener> listeners = Collections.newSetFromMap(new WeakHashMap<>());
	private State state = State.CREATION;
	private Locale locale = Locale.getDefault();
	private boolean javaFXLoaded = false;
	private String name, title, version;
	private Path directory, staticArgumentsFile;
	private Arguments staticArguments, arguments;
	private Application application;

	private EventManager eventManager;

	private ConnectionConfig connectionConfig;
	private Optional<Path> applicationJar;

	/**
	 * Creates the application.
	 *
	 * @param arguments The arguments.
	 */
	public ApplicationManager(Arguments arguments) {
		if (instance != null)
			throw new IllegalStateException("An application instance already exists");
		instance = this;

		this.originalArguments = arguments;
		this.executor = Executors.newCachedThreadPool();
		this.resourceLoader = new ResourceLoader();
		this.devEnvironment = arguments.getBoolean("development", "dev");

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread: {}", t.getName(), e));
	}

	/**
	 * Gets the path to the application jar file.
	 *
	 * @return The path to the jar file.
	 */
	public Optional<Path> getApplicationJar() {
		if (this.applicationJar == null) {
			try {
				Path p = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
				if (p.getFileName().toString().endsWith(".jar"))
					this.applicationJar = Optional.of(p);
				else
					this.applicationJar = Optional.empty();
			} catch (URISyntaxException e) {
				logger.warn("Can't get application's jar", e);
				this.applicationJar = Optional.empty();
			}
		}
		return this.applicationJar;
	}

	/**
	 * Gets the state of the application.
	 *
	 * @return The state.
	 */
	public final State getState() {
		return this.state;
	}

	private void setState(State state) {
		if (this.state == state)
			return;

		State oldState = this.state;
		this.state = state;

		if (this.eventManager != null)
			this.eventManager.postEvent(new ApplicationStateChangeEvent(oldState, state));
	}

	public final Locale getLocale() {
		return this.locale;
	}

	public final void setLocale(Locale locale) {
		if (locale == null)
			throw new IllegalArgumentException("locale");

		Locale oldLocale = this.locale;
		this.locale = locale;

		Locale.setDefault(locale);
		this.eventManager.postEvent(new ApplicationLocaleChangeEvent(oldLocale, locale));
	}

	/**
	 * Gets the original arguments parsed from command line.
	 *
	 * @return The original arguments.
	 */
	public final Arguments getOriginalArguments() {
		return this.originalArguments;
	}

	/**
	 * Updates the application if possible.
	 *
	 * @param appSource The application release source.
	 */
	public void runUpdater(ReleaseSource appSource) {
		runUpdater(appSource, new GitHubReleaseSource("Yeregorix", "AppCommonUpdater", null, "Updater", getConnectionConfig()));
	}

	/**
	 * Updates the application if possible.
	 *
	 * @param appSource     The application release source.
	 * @param updaterSource The updater release source.
	 */
	public void runUpdater(ReleaseSource appSource, ReleaseSource updaterSource) {
		if (!disableUpdateCheck())
			new ApplicationUpdater(this, appSource, updaterSource).run();
	}

	/**
	 * Gets the default connection config.
	 *
	 * @return The default connection config.
	 */
	public ConnectionConfig getConnectionConfig() {
		if (this.connectionConfig == null) {
			ConnectionConfig.Builder b = ConnectionConfig.builder();

			Optional<String> host = this.arguments.getString("proxyHost");
			host.ifPresent(s -> b.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(s, this.arguments.getInt("proxyPort").orElse(8080)))));

			this.connectionConfig = b.connectTimeout(this.arguments.getInt("connectTimeout").orElse(3000))
					.readTimeout(this.arguments.getInt("readTimeout").orElse(3000))
					.userAgent(this.arguments.getString("userAgent").orElse(this.name + "/" + this.version))
					.bufferSize(this.arguments.getInt("bufferSize").orElse(65536)).build();
		}
		return this.connectionConfig;
	}

	/**
	 * Determines whether the update check should be disabled.
	 *
	 * @return Whether the update check should be disabled.
	 */
	public boolean disableUpdateCheck() {
		return this.arguments.getBoolean("noUpdateCheck");
	}

	/**
	 * Gets the resource loader.
	 *
	 * @return The resource loader.
	 */
	public final ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Gets the static arguments.
	 * These are loaded from a text file.
	 *
	 * @return The static arguments.
	 */
	public final Arguments getStaticArguments() {
		return this.staticArguments;
	}

	/**
	 * Sets and saves static arguments.
	 *
	 * @param arguments The static arguments.
	 */
	public final void setStaticArguments(Arguments arguments) {
		if (arguments.getParametersCount() != 0)
			throw new IllegalArgumentException("Parameters are not allowed");
		_setStaticArguments(arguments);

		try (BufferedWriter writer = Files.newBufferedWriter(this.staticArgumentsFile)) {
			writer.write(arguments.toString());
		} catch (IOException e) {
			logger.warn("Failed to save static arguments", e);
		}
	}

	private void _setStaticArguments(Arguments arguments) {
		this.staticArguments = arguments;
		this.arguments = this.originalArguments.toBuilder().add(arguments).build();
	}

	/**
	 * Gets the arguments.
	 * These are the result of a merge of original arguments and static arguments.
	 *
	 * @return The arguments.
	 */
	public final Arguments getArguments() {
		return this.arguments;
	}

	/**
	 * Gets the directory containing application's data.
	 *
	 * @return The directory.
	 */
	public final Path getDirectory() {
		return this.directory;
	}

	/**
	 * Gets the event manager.
	 *
	 * @return The event manager.
	 */
	public EventManager getEventManager() {
		if (this.eventManager == null)
			throw new IllegalStateException("Event manager is not initialized");
		return this.eventManager;
	}

	/**
	 * Gets the default executor.
	 *
	 * @return The executor.
	 */
	public ExecutorService getExecutor() {
		return this.executor;
	}

	/**
	 * Shutdowns gracefully the application.
	 */
	public void shutdown() {
		if (this.state == State.SHUTDOWN)
			return;

		logger.info("Shutting down ...");
		setState(State.SHUTDOWN);

		cancelListeners();
		this.resourceLoader.close();
		this.executor.shutdown();

		if (this.javaFXLoaded)
			Platform.runLater(Platform::exit);
	}

	private void cancelListeners() {
		for (BaseListener l : this.listeners) {
			try {
				l.cancel();
			} catch (Exception ignored) {
			}
		}
		this.listeners.clear();
	}

	/**
	 * Registers the listener (weak reference).
	 * This listener will be automatically cancelled when the application shutdowns.
	 * If the application is already shutting down then the listener is cancelled immediately.
	 *
	 * @param listener The listener.
	 */
	public final void registerListener(BaseListener listener) {
		if (this.state == State.SHUTDOWN) {
			try {
				listener.cancel();
			} catch (Exception ignored) {
			}
		} else {
			this.listeners.add(listener);
		}
	}

	/**
	 * Gets the application.
	 *
	 * @return The application.
	 */
	public final Optional<Application> getApplication() {
		return Optional.ofNullable(this.application);
	}

	/**
	 * Gets the name.
	 *
	 * @return The name.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Gets the title.
	 *
	 * @return The title.
	 */
	public final String getTitle() {
		return this.name;
	}

	/**
	 * Gets the version.
	 *
	 * @return The version.
	 */
	public final String getVersion() {
		return this.version;
	}

	/**
	 * Gets whether JavaFX is ready to use.
	 *
	 * @return Whether JavaFX is ready to use.
	 */
	public boolean isJavaFXLoaded() {
		return this.javaFXLoaded;
	}

	/**
	 * Gets whether this application is in a development environment.
	 *
	 * @return Whether this application is in a development environment.
	 */
	public final boolean isDevEnvironment() {
		return this.devEnvironment;
	}

	/**
	 * Gets the application manager.
	 *
	 * @return The application manager.
	 */
	public static ApplicationManager get() {
		if (instance == null)
			throw new IllegalStateException("ApplicationManager instance not available");
		return instance;
	}

	public static void main(String[] args) {
		new ApplicationManager(Arguments.parse(args)).launch();
	}

	/**
	 * Launches the application.
	 * Handles any errors the may occur during initialization.
	 */
	public final void launch() {
		try {
			initialize();
		} catch (Throwable t) {
			logger.error("{} {} - A fatal error occurred", this.name, this.version, t);
			fatalError(t);
		}
	}

	private void initialize() throws Exception {
		checkState(State.CREATION);
		setState(State.INITIALIZATION);

		long start = System.currentTimeMillis();

		// Load configuration
		JsonObject config = getApplicationConfig();
		String appClass = config.getString("application");
		this.name = config.getString("name", "Application");
		this.title = config.getString("title", this.name);
		this.version = config.getString("version", "");
		List<String> deps = (List) config.getArray("dependencies");
		if (deps == null)
			deps = Collections.singletonList("application");

		// Prepare directory
		this.directory = resolveDirectory().toAbsolutePath();
		String dirStr = this.directory.toString();
		String dirSep = this.directory.getFileSystem().getSeparator();
		if (!dirStr.endsWith(dirSep))
			dirStr += dirSep;

		System.setProperty("app.directory", dirStr);

		logger.info("Application directory: {}", dirStr);
		Files.createDirectories(this.directory);

		// Load static arguments
		this.staticArgumentsFile = this.directory.resolve("static-arguments.txt");
		loadStaticArguments();

		// Setup logger
		if (!this.devEnvironment)
			setupDependencies("logger");

		logger.info("Switching logger implementation ...");
		ApplicationLogger._bind();
		logger.info("Logger implementation switched: {}", ApplicationLogger.getFactory().getClass().getName());

		// Setup JavaFX
		if (!detectJavaFX())
			setupDependencies("javafx");

		logger.info("Initializing JavaFX ...");
		initJavaFX();
		this.javaFXLoaded = true;

		// Instantiate services
		this.eventManager = new EventManager();

		// Setup application dependencies
		if (!this.devEnvironment)
			setupDependencies(deps);

		// Initialize application
		logger.info("Constructing application {} ...", appClass);
		this.application = (Application) getClass().getClassLoader().loadClass(appClass).getConstructor().newInstance();
		this.application.manager = this;

		this.application.init();

		setState(State.RUNNING);

		logger.info("Started {} {} ({}ms).", this.name, this.version, System.currentTimeMillis() - start);

		this.application.run();
	}

	/**
	 * Shows a fatal error popup then exits the JVM.
	 *
	 * @param throwable The throwable.
	 */
	public final void fatalError(Throwable throwable) {
		if (this.javaFXLoaded) {
			try {
				Popup.error().title(this.title + " " + this.version + " - Fatal error").message(throwable).submitAndWait();
			} catch (Exception ignored) {
			}
		}
		shutdownNow();
	}

	/**
	 * Compares the current state and the expected state.
	 *
	 * @param state The expected state.
	 * @throws IllegalArgumentException If the current state is not the expected one.
	 */
	public void checkState(State state) {
		if (this.state != state)
			throw new IllegalStateException();
	}

	protected JsonObject getApplicationConfig() throws Exception {
		try (BufferedReader r = Files.newBufferedReader(getResource("application.json"))) {
			return JsonParser.object().from(r);
		}
	}

	/**
	 * Resolves the working directory.
	 *
	 * @return The directory.
	 */
	protected Path resolveDirectory() {
		String dirName = this.originalArguments.getString("directory", "dir").orElse("");
		if (!dirName.isEmpty())
			return Paths.get(dirName);

		dirName = this.originalArguments.getString("directoryName", "dirName").orElse("");
		if (dirName.isEmpty())
			dirName = this.name;

		if (this.devEnvironment)
			dirName += "-dev";

		return OperatingSystem.CURRENT.getApplicationDirectory().resolve(dirName);
	}

	private void loadStaticArguments() {
		Arguments args = Arguments.empty();

		if (Files.exists(this.staticArgumentsFile)) {
			try (BufferedReader reader = Files.newBufferedReader(this.staticArgumentsFile)) {
				Arguments tmp = Arguments.builder().parse(reader.readLine()).build();
				if (tmp.getParametersCount() == 0)
					args = tmp;
				else
					logger.warn("Static arguments cannot contains parameters");
			} catch (IOException e) {
				logger.warn("Failed to load static arguments", e);
			}
		}

		_setStaticArguments(args);
	}

	private static boolean detectJavaFX() {
		try {
			Class.forName("javafx.application.Platform");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Updates and loads dependencies specified in the corresponding configuration files.
	 *
	 * @param names The names of the configurations.
	 * @throws Exception if any exception occurs while loading configurations.
	 */
	public void setupDependencies(String... names) throws Exception {
		setupDependencies(Arrays.asList(names));
	}

	/**
	 * Updates and loads dependencies specified in the corresponding configuration files.
	 *
	 * @param names The names of the configurations.
	 * @throws Exception if any exception occurs while loading configurations.
	 */
	public void setupDependencies(Iterable<String> names) throws Exception {
		List<DependencyInfo> deps = new ArrayList<>();
		for (String name : names)
			DependencyInfo.loadAll(getResource("dep/" + name + ".json"), deps);
		DependencyManager.create(this, deps).setup();
	}

	private static void initJavaFX() throws Exception {
		Platform.setImplicitExit(false);
		Platform.startup(() -> {
		});
	}

	/**
	 * Forces the application shutdown.
	 * Terminates the JVM with status 0.
	 */
	public void shutdownNow() {
		shutdownNow(0);
	}

	/**
	 * Finds the resource with the given name.
	 *
	 * @param name The resource name.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path getResource(String name) throws IOException {
		return this.resourceLoader.getResource(ApplicationManager.class, name);
	}

	/**
	 * Forces the application shutdown.
	 * Terminates the JVM.
	 *
	 * @param code The exit status.
	 */
	public void shutdownNow(int code) {
		try {
			if (this.state != State.SHUTDOWN) {
				logger.info("Shutting down ...");
				setState(State.SHUTDOWN);
			}

			cancelListeners();
			this.resourceLoader.close();

			Thread.setDefaultUncaughtExceptionHandler((t, e) -> {});
		} catch (Exception ignored) {
		}
		System.exit(code);
	}
}
