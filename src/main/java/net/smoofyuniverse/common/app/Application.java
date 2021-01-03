/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.event.EventManager;
import net.smoofyuniverse.common.event.app.ApplicationStateChangeEvent;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.resource.*;
import net.smoofyuniverse.common.task.BaseListener;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.ResourceLoader;
import net.smoofyuniverse.logger.appender.log.FormattedAppender;
import net.smoofyuniverse.logger.appender.log.LogAppender;
import net.smoofyuniverse.logger.appender.log.ParentLogAppender;
import net.smoofyuniverse.logger.appender.log.TransformedAppender;
import net.smoofyuniverse.logger.appender.string.DatedRollingFileAppender;
import net.smoofyuniverse.logger.core.DefaultImpl;
import net.smoofyuniverse.logger.core.LogLevel;
import net.smoofyuniverse.logger.core.LogMessage;
import net.smoofyuniverse.logger.core.Logger;
import net.smoofyuniverse.logger.transformer.ParentTransformer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

/**
 * The application.
 * Singleton.
 */
public abstract class Application {
	private static final Logger logger = Logger.get("Application");
	private static Application instance;

	private State state = State.CREATION;
	protected final ResourceLoader resourceLoader;
	protected final Arguments originalArguments;
	protected final Path workingDir, staticArgumentsFile;
	protected final String name, title, version;
	protected final boolean GUIEnabled, devEnvironment;
	protected final Set<BaseListener> listeners = Collections.newSetFromMap(new WeakHashMap<>());
	protected final ParentTransformer fileLogTransformer = new ParentTransformer();

	private Arguments staticArguments, arguments;
	private ConnectionConfig connectionConfig;
	private LogAppender logAppender;
	private EventManager eventManager;
	private ResourceManager resourceManager;
	private ExecutorService executor;
	private Stage stage;
	private Optional<Path> applicationJar;

	/**
	 * Creates the application.
	 *
	 * @param arguments The arguments.
	 * @param name      The name.
	 * @param version   The version.
	 */
	public Application(Arguments arguments, String name, String version) {
		this(arguments, name, name, version);
	}

	/**
	 * Creates the application.
	 *
	 * @param arguments The arguments.
	 * @param name      The name, used in the default directory and default user agent.
	 * @param title     The title, used in the UI.
	 * @param version   The version.
	 */
	public Application(Arguments arguments, String name, String title, String version) {
		if (instance != null)
			throw new IllegalStateException("An application instance already exists");
		instance = this;

		this.name = name;
		this.title = title;
		this.version = version;

		this.originalArguments = arguments;
		this.devEnvironment = arguments.getBoolean("development", "dev");
		this.workingDir = resolveDirectory().toAbsolutePath();
		this.staticArgumentsFile = this.workingDir.resolve("static-arguments.txt");

		this.resourceLoader = new ResourceLoader();
		System.setProperty("java.net.preferIPv4Stack", "true");

		loadStaticArguments();
		this.GUIEnabled = !disableGUI();

		this.fileLogTransformer.children.add(m -> m.transform(s -> IOUtil.USER_HOME.matcher(s).replaceAll("USER_HOME")));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.log(
				new LogMessage(logger, LogLevel.ERROR, LocalTime.now(), t, e, "Uncaught exception in thread: " + t.getName())));

		setState(State.SERVICES_INIT);
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

	/**
	 * Determines whether the GUI should be disabled.
	 * This method is called once during {@link State#CREATION}.
	 *
	 * @return Whether the GUI should be disabled.
	 */
	protected boolean disableGUI() {
		return this.arguments.getBoolean("disableGUI");
	}

	private void _setStaticArguments(Arguments arguments) {
		this.staticArguments = arguments;
		this.arguments = this.originalArguments.toBuilder().add(arguments).build();
	}

	private void setState(State state) {
		if (this.state == state)
			return;
		if (this.eventManager != null)
			this.eventManager.postEvent(new ApplicationStateChangeEvent(this, this.state, state));
		this.state = state;
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

	private static void initJavaFX() throws Exception {
		Platform.setImplicitExit(false);
		try {
			Platform.class.getDeclaredMethod("startup", Runnable.class).invoke(null, (Runnable) () -> {});
		} catch (NoSuchMethodException e) {
			new JFXPanel();
		}
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
	 * Forces the application shutdown.
	 * Terminates the JVM with status 0.
	 */
	public void shutdownNow() {
		shutdownNow(0);
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
				logger.info("Shutting down ..");
				setState(State.SHUTDOWN);
			}

			cancelListeners();
			this.resourceLoader.close();

			Thread.setDefaultUncaughtExceptionHandler((t, e) -> {});
		} catch (Exception ignored) {
		}
		System.exit(code);
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
	 * Initializes the following services: logger factory, event manager, resource manager, executor.
	 *
	 * @param executor The executor.
	 */
	protected void initServices(ExecutorService executor) {
		checkState(State.SERVICES_INIT);
		long start = System.currentTimeMillis();

		initLogAppender();
		initEventManager();
		initResourceManager();
		this.executor = executor;

		logger.info("Started " + this.name + " " + this.version + " (" + (System.currentTimeMillis() - start) + "ms).");
		setState(State.STAGE_INIT);
	}

	/**
	 * Initializes the logger factory using {@link Application#setLogAppender}.
	 */
	protected void initLogAppender() {
		setLogAppender(new ParentLogAppender(
				DefaultImpl.FORMATTED_SYSTEM_APPENDER,
				new TransformedAppender(new FormattedAppender(
						DatedRollingFileAppender.builder().directory(this.workingDir.resolve("logs")).maxFiles(60).build()),
						this.fileLogTransformer)));
	}

	/**
	 * Initializes the event manager using {@link Application#setEventManager}.
	 */
	protected void initEventManager() {
		setEventManager(new EventManager());
	}

	/**
	 * Initializes the resource manager using {@link Application#setResourceManager}.
	 */
	protected void initResourceManager() {
		setResourceManager(new ResourceManager(Languages.ENGLISH, false));
	}

	/**
	 * Sets the event manager.
	 * Must be called once and during {@link State#SERVICES_INIT}.
	 *
	 * @param eventManager The event manager.
	 */
	protected final void setEventManager(EventManager eventManager) {
		checkState(State.SERVICES_INIT);

		if (this.eventManager != null)
			throw new IllegalStateException();
		this.eventManager = eventManager;
	}

	/**
	 * Sets the resource manager.
	 * Must be called once and during {@link State#SERVICES_INIT}.
	 *
	 * @param resourceManager The resource manager.
	 */
	protected final void setResourceManager(ResourceManager resourceManager) {
		checkState(State.SERVICES_INIT);

		if (this.resourceManager != null)
			throw new IllegalStateException();
		this.resourceManager = resourceManager;

		logger.info("Loading resources ..");
		try {
			loadResources();
		} catch (Exception e) {
			logger.error("Failed to load resources", e);
			fatalError(e);
		}

		try {
			bindTranslations();
		} catch (Exception e) {
			logger.error("Failed to fill translations", e);
			fatalError(e);
		}

		String langId = this.arguments.getString("language", "lang").orElse(null);
		if (langId != null && !Language.isValidId(langId)) {
			logger.warn("Argument '" + langId + "' is not a valid language identifier.");
			langId = null;
		}
		if (langId == null) {
			langId = System.getProperty("user.language");
			if (langId != null && !Language.isValidId(langId))
				langId = null;
		}
		if (langId != null)
			this.resourceManager.setSelection(Language.of(langId));
	}

	/**
	 * Launches the application.
	 * Handles any errors the may occur during initialization.
	 */
	public final void launch() {
		try {
			if (this.GUIEnabled)
				initJavaFX();
			init();
		} catch (Throwable t) {
			logger.error(this.title + " " + this.version + " - A fatal error occurred", t);
			fatalError(t);
		}
	}

	protected void loadResources() throws Exception {
		loadTranslations(getResource("lang/common"), "txt");
	}

	protected final void loadTranslations(Path dir, String extension) {
		for (Entry<Language, ResourceModule<String>> e : Translator.loadAll(dir, "txt").entrySet())
			this.resourceManager.getOrCreatePack(e.getKey()).addModule(e.getValue());
	}

	protected void bindTranslations() throws Exception {
		getTranslator().bindStaticFields(Translations.class);
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
	 * Shows a fatal error popup then exits the JVM.
	 *
	 * @param throwable The throwable.
	 */
	public final void fatalError(Throwable throwable) {
		if (this.GUIEnabled) {
			try {
				Popup.error().title(this.title + " " + this.version + " - Fatal error").message(throwable).submitAndWait();
			} catch (Exception ignored) {
			}
		}
		shutdownNow();
	}

	/**
	 * Initializes the application.
	 *
	 * @throws Exception if any exception occurs.
	 */
	public abstract void init() throws Exception;

	protected final Stage setScene(Parent root) {
		return setScene(new Scene(root));
	}

	protected final Stage setScene(Scene scene) {
		checkState(State.STAGE_INIT);
		this.stage.setScene(scene);
		setState(State.RUNNING);
		return this.stage;
	}

	/**
	 * Gets the state.
	 *
	 * @return The state.
	 */
	public State getState() {
		return this.state;
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

	/**
	 * Gets the file log transformer.
	 *
	 * @return The file log transformer.
	 */
	public ParentTransformer getFileLogTransformer() {
		return this.fileLogTransformer;
	}

	protected final Stage initStage(double width, double height, String... icons) {
		Image[] images = new Image[icons.length];
		for (int i = 0; i < icons.length; i++)
			images[i] = IOUtil.loadImage(icons[i]);
		return initStage(width, height, images);
	}

	protected final Stage initStage(double width, double height, Image... icons) {
		Stage stage = initStage(width, height);
		stage.getIcons().addAll(icons);
		return stage;
	}

	protected final Stage initStage(double width, double height) {
		Stage stage = new Stage();
		stage.setTitle(this.title + " " + this.version);
		stage.setWidth(width);
		stage.setHeight(height);
		return initStage(stage);
	}

	protected final Stage initStage(Stage stage) {
		checkState(State.STAGE_INIT);
		this.stage = stage;
		this.stage.setOnCloseRequest(e -> shutdown());
		return this.stage;
	}

	protected final void skipStage() {
		checkState(State.STAGE_INIT);
		if (this.stage != null)
			throw new IllegalStateException();
		setState(State.RUNNING);
	}

	/**
	 * Checks whether the graphical user interface is enabled.
	 *
	 * @throws IllegalArgumentException If the graphical user interface is not enabled.
	 */
	public final void requireGUI() {
		if (!this.GUIEnabled)
			throw new IllegalStateException("GUI is not enabled");
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
	 * Finds the resource with the given name.
	 *
	 * @param name The resource name.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path getResource(String name) throws IOException {
		return this.resourceLoader.getResource(Application.class, name);
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
	 * Gets "Application" logger.
	 *
	 * @return The logger.
	 */
	public Logger getLogger() {
		return logger;
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
	 * Gets the working directory.
	 *
	 * @return The working directory.
	 */
	public final Path getWorkingDirectory() {
		return this.workingDir;
	}

	/**
	 * Gets the log appender.
	 *
	 * @return The log appender.
	 */
	public LogAppender getLogAppender() {
		if (this.logAppender == null)
			throw new IllegalStateException("LogAppender not initialized");
		return this.logAppender;
	}

	/**
	 * Sets the log appender.
	 * Must be called once and during {@link State#SERVICES_INIT}.
	 *
	 * @param appender The log appender.
	 */
	protected final void setLogAppender(LogAppender appender) {
		checkState(State.SERVICES_INIT);

		if (this.logAppender != null)
			throw new IllegalStateException();
		this.logAppender = appender;
		DefaultImpl.FACTORY.setAppender(appender);

		try {
			Files.createDirectories(this.workingDir);
			logger.info("Working directory: " + this.workingDir);
		} catch (IOException e) {
			logger.error("Failed to create working directory", e);
			fatalError(e);
		}
	}

	/**
	 * Gets the event manager.
	 *
	 * @return The event manager.
	 */
	public EventManager getEventManager() {
		if (this.eventManager == null)
			throw new IllegalStateException("EventManager not initialized");
		return this.eventManager;
	}

	/**
	 * Gets the resource manager.
	 *
	 * @return The resource manager.
	 */
	public ResourceManager getResourceManager() {
		if (this.resourceManager == null)
			throw new IllegalStateException("ResourceManager not initialized");
		return this.resourceManager;
	}

	/**
	 * Gets the default connection config.
	 *
	 * @return The default connection config.
	 */
	public ConnectionConfig getConnectionConfig() {
		if (this.connectionConfig == null) {
			Optional<String> host = this.arguments.getString("proxyHost");
			ConnectionConfig.Builder b = ConnectionConfig.builder();
			host.ifPresent(s -> b.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(s, this.arguments.getInt("proxyPort").orElse(8080)))));
			this.connectionConfig = b.connectTimeout(this.arguments.getInt("connectTimeout").orElse(3000)).readTimeout(this.arguments.getInt("readTimeout").orElse(3000))
					.userAgent(this.arguments.getString("userAgent").orElseGet(this::getDefaultUserAgent)).bufferSize(this.arguments.getInt("bufferSize").orElse(65536)).build();
		}
		return this.connectionConfig;
	}

	/**
	 * Gets the default user agent.
	 *
	 * @return The default user agent.
	 */
	public String getDefaultUserAgent() {
		return this.name + "/" + this.version;
	}

	/**
	 * Gets the default executor.
	 *
	 * @return The executor.
	 */
	public ExecutorService getExecutor() {
		if (this.executor == null)
			throw new IllegalStateException("ExecutorService not initialized");
		return this.executor;
	}

	/**
	 * Gets the translator.
	 *
	 * @return The translator.
	 */
	public Translator getTranslator() {
		return getResourceManager().translator;
	}

	/**
	 * Shutdowns gracefully the application.
	 */
	public void shutdown() {
		if (this.state == State.SHUTDOWN)
			return;

		logger.info("Shutting down ..");
		setState(State.SHUTDOWN);

		cancelListeners();
		this.resourceLoader.close();

		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}

		if (this.GUIEnabled)
			Platform.runLater(Platform::exit);
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
	 * Gets the stage.
	 *
	 * @return The stage.
	 */
	public Optional<Stage> getStage() {
		return Optional.ofNullable(this.stage);
	}

	/**
	 * Gets the scene.
	 *
	 * @return The scene.
	 */
	public Optional<Scene> getScene() {
		return this.stage == null ? Optional.empty() : Optional.of(this.stage.getScene());
	}

	/**
	 * Gets whether the graphical user interface is enabled.
	 *
	 * @return Whether the graphical user interface is enabled.
	 */
	public final boolean isGUIEnabled() {
		return this.GUIEnabled;
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
	 * Gets the application.
	 *
	 * @return The application.
	 */
	public static Application get() {
		if (instance == null)
			throw new IllegalStateException("Application instance not available");
		return instance;
	}
}
