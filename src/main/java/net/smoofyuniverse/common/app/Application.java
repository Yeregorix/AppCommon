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
import net.smoofyuniverse.common.environment.DependencyInfo;
import net.smoofyuniverse.common.environment.ReleaseInfo;
import net.smoofyuniverse.common.environment.source.GithubReleaseSource;
import net.smoofyuniverse.common.environment.source.ReleaseSource;
import net.smoofyuniverse.common.event.EventManager;
import net.smoofyuniverse.common.event.app.ApplicationStateChangeEvent;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.resource.*;
import net.smoofyuniverse.common.task.BaseListener;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.ProgressTask;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.ProcessUtil;
import net.smoofyuniverse.common.util.ResourceLoader;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.appender.log.FormattedAppender;
import net.smoofyuniverse.logger.appender.log.LogAppender;
import net.smoofyuniverse.logger.appender.log.ParentLogAppender;
import net.smoofyuniverse.logger.appender.log.TransformedAppender;
import net.smoofyuniverse.logger.appender.string.DatedRollingFileAppender;
import net.smoofyuniverse.logger.appender.string.PrintStreamAppender;
import net.smoofyuniverse.logger.appender.string.StringAppender;
import net.smoofyuniverse.logger.core.LogLevel;
import net.smoofyuniverse.logger.core.LogMessage;
import net.smoofyuniverse.logger.core.Logger;
import net.smoofyuniverse.logger.core.LoggerFactory;
import net.smoofyuniverse.logger.transformer.ParentTransformer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * The application.
 * Singleton.
 */
public abstract class Application {
	private static Application instance;

	private State state = State.CREATION;
	protected final ResourceLoader resourceLoader;
	protected final Arguments originalArguments;
	protected final Path workingDir, staticArgumentsFile;
	protected final String name, title, version;
	protected final boolean UIEnabled, devEnvironment;
	protected final Set<BaseListener> listeners = Collections.newSetFromMap(new WeakHashMap<>());
	protected final ParentTransformer fileLogTransformer = new ParentTransformer();

	private Arguments staticArguments, arguments;
	private ConnectionConfig connectionConfig;
	private LoggerFactory loggerFactory;
	private EventManager eventManager;
	private ResourceManager resourceManager;
	private ExecutorService executor;
	private Logger logger;
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
		this.logger = new Logger(newFormattedAppender(PrintStreamAppender.system()), "Application");
		System.setProperty("java.net.preferIPv4Stack", "true");

		loadStaticArguments();
		this.UIEnabled = enableUI();

		setState(State.SERVICES_INIT);
	}

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
					this.logger.warn("Static arguments cannot contains parameters");
			} catch (IOException e) {
				this.logger.warn("Failed to load static arguments", e);
			}
		}

		_setStaticArguments(args);
	}

	protected boolean enableUI() {
		return !this.arguments.getBoolean("disableUI");
	}

	private void _setStaticArguments(Arguments arguments) {
		this.staticArguments = arguments;
		this.arguments = this.originalArguments.toBuilder().add(arguments).build();
	}

	public final FormattedAppender newFormattedAppender(StringAppender appender) {
		return new FormattedAppender(appender, this::formatLog);
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

	private static void initJavaFX() {
		Platform.setImplicitExit(false);
		new JFXPanel();
	}

	/**
	 * Shows a fatal error popup then exits the JVM.
	 *
	 * @param throwable The throwable.
	 */
	public final void fatalError(Throwable throwable) {
		if (this.UIEnabled) {
			try {
				Popup.error().title(this.title + " " + this.version + " - Fatal error").message(throwable).submitAndWait();
			} catch (Exception ignored) {
			}
		}
		shutdownNow();
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
				this.logger.info("Shutting down ..");
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
	 * Formats the log message to a string.
	 *
	 * @param msg The log message.
	 * @return The formatted string.
	 */
	public String formatLog(LogMessage msg) {
		return StringUtil.format(msg.time) + " [" + msg.logger.getName() + "] " + msg.level.name() + " - " + msg.getText() + System.lineSeparator();
	}

	protected final void initServices(LoggerFactory loggerFactory, EventManager eventManager, ResourceManager resourceManager, ExecutorService executor) {
		checkState(State.SERVICES_INIT);

		long time = System.currentTimeMillis();

		this.loggerFactory = loggerFactory;
		this.eventManager = eventManager;
		this.resourceManager = resourceManager;
		this.executor = executor;
		this.logger = loggerFactory.provideLogger("Application");
		this.fileLogTransformer.children.add(m -> m.transform(s -> IOUtil.USER_HOME.matcher(s).replaceAll("USER_HOME")));

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> this.logger.log(
				new LogMessage(this.logger, LogLevel.ERROR, LocalTime.now(), t, e, "Uncaught exception in thread: " + t.getName())));

		this.logger.info("Working directory: " + this.workingDir);
		try {
			Files.createDirectories(this.workingDir);
		} catch (IOException e) {
			this.logger.error("Failed to create working directory", e);
			fatalError(e);
		}

		this.logger.info("Loading resources ..");
		try {
			loadResources();
		} catch (Exception e) {
			this.logger.error("Failed to load resources", e);
			fatalError(e);
		}

		try {
			fillTranslations();
		} catch (Exception e) {
			this.logger.error("Failed to fill translations", e);
			fatalError(e);
		}

		String langId = this.arguments.getString("language", "lang").orElse(null);
		if (langId != null && !Language.isValidId(langId)) {
			this.logger.warn("Argument '" + langId + "' is not a valid language identifier.");
			langId = null;
		}
		if (langId == null) {
			langId = System.getProperty("user.language");
			if (langId != null && !Language.isValidId(langId))
				langId = null;
		}
		if (langId != null)
			this.resourceManager.setSelection(Language.of(langId));

		long dur = System.currentTimeMillis() - time;
		this.logger.info("Started " + this.name + " " + this.version + " (" + dur + "ms).");

		setState(State.STAGE_INIT);
	}

	protected final void initServices(LogAppender appender, ExecutorService executor) {
		initServices(new LoggerFactory(appender), executor);
	}

	protected final void initServices(LoggerFactory loggerFactory, ExecutorService executor) {
		initServices(loggerFactory, new EventManager(loggerFactory), new ResourceManager(Languages.ENGLISH, false), executor);
	}

	protected final void tryUpdateApplication(ReleaseSource appSource) {
		tryUpdateApplication(appSource, new GithubReleaseSource("Yeregorix", "AppCommonUpdater", null, "Updater"));
	}

	protected void loadResources() throws Exception {
		loadTranslations(App.getResource("lang/common"), "txt");
	}

	protected final void loadTranslations(Path dir, String extension) {
		for (Entry<Language, ResourceModule<String>> e : Translator.loadAll(dir, "txt").entrySet())
			this.resourceManager.getOrCreatePack(e.getKey()).addModule(e.getValue());
	}

	protected void fillTranslations() throws Exception {
		getTranslator().fill(Translations.class);
	}

	protected final void tryUpdateApplication(ReleaseSource appSource, ReleaseSource updaterSource) {
		if (this.arguments.getBoolean("noUpdateCheck"))
			return;

		Path appJar = getApplicationJar().orElse(null);
		if (appJar != null) {
			ReleaseInfo latestApp = appSource.getLatestRelease().orElse(null);
			if (latestApp != null && !latestApp.matches(appJar)) {
				ReleaseInfo latestUpdater = updaterSource.getLatestRelease().orElse(null);
				if (latestUpdater != null && suggestApplicationUpdate())
					updateApplication(appJar, latestApp, latestUpdater);
			}
		}
	}

	protected final boolean suggestApplicationUpdate() {
		if (this.UIEnabled)
			return Popup.confirmation().title(Translations.update_available_title).message(Translations.update_available_message).submitAndWait();

		if (this.arguments.getBoolean("autoUpdate"))
			return true;

		this.logger.info("An update is available. Please restart with the --autoUpdate argument to update the application.");
		return false;
	}

	protected final void updateApplication(Path appJar, ReleaseInfo latestApp, ReleaseInfo latestUpdater) {
		Consumer<ProgressTask> consumer = task -> {
			this.logger.info("Starting application update task ..");
			task.setTitle(Translations.update_download_title);

			Path updaterJar = this.workingDir.resolve("Updater.jar");
			if (!latestUpdater.matches(updaterJar)) {
				this.logger.info("Downloading latest updater ..");
				IOUtil.download(latestUpdater.url, updaterJar, task);

				if (task.isCancelled())
					return;

				if (!latestUpdater.matches(updaterJar)) {
					task.cancel();
					this.logger.error("Updater file seems invalid, aborting ..");
					Popup.error().title(Translations.update_cancelled).message(Translations.updater_signature_invalid).show();
				}
			}

			if (task.isCancelled())
				return;

			Path appUpdateJar = this.workingDir.resolve(this.name + "-Update.jar");
			if (!latestApp.matches(appUpdateJar)) {
				this.logger.info("Downloading latest application update ..");
				IOUtil.download(latestApp.url, appUpdateJar, task);

				if (task.isCancelled())
					return;

				if (!latestApp.matches(appUpdateJar)) {
					task.cancel();
					this.logger.error("Application update file seems invalid, aborting ..");
					Popup.error().title(Translations.update_cancelled).message(Translations.update_signature_invalid).show();
				}
			}

			if (task.isCancelled())
				return;

			logger.info("Starting updater process ..");
			task.setTitle(Translations.update_process_title);
			task.setMessage(Translations.update_process_message);
			task.setProgress(-1);

			boolean launch = this.UIEnabled && !this.arguments.getBoolean("noUpdateLaunch");
			if (!launch)
				logger.info("The updater will only apply the modifications. You will have to restart the application manually.");

			List<String> cmd = new ArrayList<>();
			cmd.add("java");
			cmd.add("-jar");
			cmd.add(updaterJar.toAbsolutePath().toString());
			cmd.add("1"); // version
			cmd.add(appUpdateJar.toAbsolutePath().toString()); // source
			cmd.add(appJar.toAbsolutePath().toString()); // target
			cmd.add(String.valueOf(launch)); // launch
			this.originalArguments.export(cmd::add);

			if (task.isCancelled())
				return;

			try {
				ProcessUtil.builder().command(cmd).start();
			} catch (IOException e) {
				task.cancel();
				logger.error("Failed to start updater process", e);
				Popup.error().title(Translations.update_cancelled).message(Translations.update_process_error).show();
			}
		};

		boolean r;
		if (this.UIEnabled)
			r = Popup.consumer(consumer).title(Translations.update_title).submitAndWait();
		else
			r = App.submit(consumer);

		if (r)
			shutdownNow();
		else
			this.logger.info("Update task has been cancelled.");
	}

	/**
	 * Launches the application.
	 * Handles any errors the may occur during initialization.
	 */
	public final void launch() {
		try {
			if (this.UIEnabled)
				initJavaFX();
			init();
		} catch (Throwable t) {
			this.logger.error(this.title + " " + this.version + " - A fatal error occurred", t);
			fatalError(t);
		}
	}

	protected final boolean updateDependencies(Path defaultDir, DependencyInfo... deps) {
		List<DependencyInfo> list = new LinkedList<>();
		for (DependencyInfo info : deps)
			list.add(info);

		updateDependencies(defaultDir, list);
		return list.isEmpty();
	}

	protected final void updateDependencies(Path defaultDir, Collection<DependencyInfo> deps) {
		if (deps.isEmpty())
			return;

		long totalSize = 0;
		Iterator<DependencyInfo> it = deps.iterator();
		while (it.hasNext()) {
			DependencyInfo info = it.next();

			if (info.file == null)
				info.file = IOUtil.getMavenPath(defaultDir, info.name, ".jar");

			if (info.matches())
				it.remove();
			else
				totalSize += info.size;
		}

		if (deps.isEmpty())
			return;

		long totalSizeF = totalSize;
		Consumer<ProgressTask> consumer = task -> {
			this.logger.info("Downloading missing dependencies ..");
			task.setTitle(Translations.dependencies_download_title);
			IncrementalListener listener = task.expect(totalSizeF);

			Iterator<DependencyInfo> it2 = deps.iterator();
			while (it2.hasNext()) {
				if (task.isCancelled())
					return;

				DependencyInfo info = it2.next();
				this.logger.info("Downloading dependency " + info.name + " ..");
				task.setMessage(info.name);

				Path dir = info.file.getParent();
				if (!Files.isDirectory(dir)) {
					try {
						Files.createDirectories(dir);
					} catch (IOException e) {
						this.logger.warn("Failed to create directory " + dir, e);
						continue;
					}
				}

				if (!IOUtil.download(info.url, info.file, listener))
					continue;

				if (task.isCancelled())
					return;

				if (info.matches())
					it2.remove();
				else
					this.logger.warn("The downloaded dependency has an incorrect signature.");
			}
		};

		boolean r;
		if (this.UIEnabled)
			r = Popup.consumer(consumer).title(Translations.dependencies_update_title).submitAndWait();
		else
			r = App.submit(consumer);

		if (!r || deps.isEmpty())
			return;

		StringBuilder b = new StringBuilder();
		for (DependencyInfo info : deps)
			b.append("\n- ").append(info.name);

		Popup.error().title(Translations.failed_dependencies_title).message(Translations.failed_dependencies_message.format("list", b.toString())).showAndWait();
	}

	protected final void loadDependencies(DependencyInfo... deps) {
		loadDependencies((URLClassLoader) ClassLoader.getSystemClassLoader(), deps);
	}

	protected final void loadDependencies(URLClassLoader cl, DependencyInfo... deps) {
		for (DependencyInfo info : deps)
			IOUtil.addToClasspath(cl, info.file);
	}

	protected final void loadDependencies(Collection<DependencyInfo> deps) {
		loadDependencies((URLClassLoader) ClassLoader.getSystemClassLoader(), deps);
	}

	protected final void loadDependencies(URLClassLoader cl, Collection<DependencyInfo> deps) {
		for (DependencyInfo info : deps)
			IOUtil.addToClasspath(cl, info.file);
	}

	/**
	 * Initializes the application.
	 */
	public abstract void init() throws Exception;

	protected final void initServices(ExecutorService executor) {
		initServices(new ParentLogAppender(
						newFormattedAppender(PrintStreamAppender.system()),
						new TransformedAppender(newFormattedAppender(
								DatedRollingFileAppender.builder().directory(this.workingDir.resolve("logs")).maxFiles(60).build()),
								this.fileLogTransformer)),
				executor);
	}

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
				this.logger.warn("Can't get application's jar", e);
				this.applicationJar = Optional.empty();
			}
		}
		return this.applicationJar;
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
	 * Checks whether user interface is enabled.
	 *
	 * @throws IllegalArgumentException If user interface is not enabled.
	 */
	public final void requireUI() {
		if (!this.UIEnabled)
			throw new IllegalStateException("UI is not enabled");
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
			this.logger.warn("Failed to save static arguments", e);
		}
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
	 * Gets "Application" logger.
	 *
	 * @return The logger.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Gets the logger factory.
	 *
	 * @return The logger factory.
	 */
	public LoggerFactory getLoggerFactory() {
		if (this.loggerFactory == null)
			throw new IllegalStateException("LoggerFactory not initialized");
		return this.loggerFactory;
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

		this.logger.info("Shutting down ..");
		setState(State.SHUTDOWN);

		cancelListeners();
		this.resourceLoader.close();

		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}

		if (this.UIEnabled)
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
	public final boolean isUIEnabled() {
		return this.UIEnabled;
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
