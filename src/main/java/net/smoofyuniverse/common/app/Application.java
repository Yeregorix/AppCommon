/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.common.environment.source.EmptyReleaseSource;
import net.smoofyuniverse.common.environment.source.GithubReleaseSource;
import net.smoofyuniverse.common.environment.source.ReleaseSource;
import net.smoofyuniverse.common.event.app.ApplicationStateChangeEvent;
import net.smoofyuniverse.common.event.core.EventManager;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.resource.Language;
import net.smoofyuniverse.common.resource.Languages;
import net.smoofyuniverse.common.resource.ResourceManager;
import net.smoofyuniverse.common.resource.ResourceModule;
import net.smoofyuniverse.common.resource.translator.Translator;
import net.smoofyuniverse.common.task.BaseListener;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.ProgressTask;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.ProcessUtil;
import net.smoofyuniverse.common.util.ResourceLoader;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.appender.*;
import net.smoofyuniverse.logger.core.LogLevel;
import net.smoofyuniverse.logger.core.LogMessage;
import net.smoofyuniverse.logger.core.Logger;
import net.smoofyuniverse.logger.core.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public abstract class Application {
	private static Application instance;

	private State state = State.CREATION;
	protected final ResourceLoader resourceLoader;
	protected final Arguments arguments;
	protected final Path workingDir;
	protected final String name, title, version;
	protected final boolean UIEnabled;
	protected final Set<BaseListener> listeners = Collections.newSetFromMap(new WeakHashMap<>());
	protected final Map<String, String> logBlacklist = new HashMap<>();

	private ConnectionConfig connectionConfig;
	private LoggerFactory loggerFactory;
	private EventManager eventManager;
	private ResourceManager resourceManager;
	private ExecutorService executor;
	private Logger logger;
	private Translator translator;
	private ReleaseSource appSource, updaterSource;
	private Stage stage;
	private Optional<Path> applicationJar;
	
	public Application(Arguments args, String name, String version) {
		this(args, name, name, version);
	}
	
	public Application(Arguments args, String name, String title, String version) {
		this(args, getDirectory(args, name), name, title, version, !args.getFlag("disableUI").isPresent());
	}

	public Application(Arguments args, Path dir, String name, String title, String version, boolean enableUI) {
		if (instance != null)
			throw new IllegalStateException("An application instance already exists");
		instance = this;

		this.resourceLoader = new ResourceLoader();
		this.arguments = args;
		this.workingDir = dir.toAbsolutePath();
		this.name = name;
		this.title = title;
		this.version = version;
		this.UIEnabled = enableUI;
		this.logger = new Logger(PrintStreamAppender.system(), "Application");
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		setState(State.SERVICES_INIT);
	}

	private static Path getDirectory(Arguments args, String defaultDir) {
		String dirName = args.getFlag("directory", "dir").orElse("");
		if (!dirName.isEmpty())
			return Paths.get(dirName);
		dirName = args.getFlag("directoryName", "dirName").orElse("");
		if (dirName.isEmpty())
			dirName = defaultDir;
		if (args.getFlag("development", "dev").isPresent())
			dirName += "-dev";
		return OperatingSystem.CURRENT.getWorkingDirectory().resolve(dirName);
	}

	private void setState(State state) {
		if (this.state == state)
			return;
		if (this.eventManager != null)
			this.eventManager.postEvent(new ApplicationStateChangeEvent(this, this.state, state));
		this.state = state;
	}

	public final void launch() {
		try {
			if (this.UIEnabled)
				initJavaFX();
			init();
		} catch (Exception e) {
			this.logger.error(this.title + " " + this.version + " - A fatal error occurred", e);
			fatalError(e);
		}
	}

	private static void initJavaFX() {
		Platform.setImplicitExit(false);
		new JFXPanel();
	}

	protected final void initServices(ExecutorService executor) {
		initServices(new FormattedAppender(new ParentAppender(PrintStreamAppender.system(), new TransformedAppender(DatedRollingFileAppender.builder().directory(this.workingDir.resolve("logs")).maxFiles(60).build(), this::transformLog)), this::formatLog), executor);
	}

	public String transformLog(String msg) {
		for (Entry<String, String> e : this.logBlacklist.entrySet())
			msg = msg.replace(e.getKey(), e.getValue());
		return msg;
	}
	
	public void checkState(State state) {
		if (this.state != state)
			throw new IllegalStateException();
	}
	
	public State getState() {
		return this.state;
	}

	public abstract void init() throws Exception;

	public String formatLog(LogMessage msg) {
		return StringUtil.format(msg.time) + " [" + msg.logger.getName() + "] " + msg.level.name() + " - " + msg.getText() + System.lineSeparator();
	}
	
	protected final void initServices(LogAppender appender, ExecutorService executor) {
		initServices(new LoggerFactory(appender), executor);
	}
	
	protected final void initServices(LoggerFactory loggerFactory, ExecutorService executor) {
		initServices(loggerFactory, new EventManager(loggerFactory), new ResourceManager(Languages.ENGLISH, false), executor);
	}

	protected final void initServices(LoggerFactory loggerFactory, EventManager eventManager, ResourceManager resourceManager, ExecutorService executor) {
		checkState(State.SERVICES_INIT);

		long time = System.currentTimeMillis();

		this.loggerFactory = loggerFactory;
		this.eventManager = eventManager;
		this.resourceManager = resourceManager;
		this.executor = executor;
		this.logger = loggerFactory.provideLogger("Application");
		this.logBlacklist.put(IOUtil.USER_HOME, "USER_HOME");

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> this.logger.log(LogLevel.ERROR, t, "Uncaught exception in thread: " + t.getName(), e));

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

		this.translator = Translator.of(this.resourceManager);
		try {
			fillTranslations();
		} catch (Exception e) {
			this.logger.error("Failed to fill translations", e);
			fatalError(e);
		}

		String langId = this.arguments.getFlag("language", "lang").orElse(null);
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

		setState(State.ENVIRONMENT_UPDATE);
	}

	protected final void loadLibraries(Collection<DependencyInfo> libs) throws Exception {
		if (libs.isEmpty() || this.arguments.getFlag("development", "dev").isPresent())
			return;

		Path libsDir = this.workingDir.resolve("libraries");
		Files.createDirectory(libsDir);

		List<DependencyInfo> toUpdate = new ArrayList<>();
		long totalSize = 0;

		this.logger.info("Verifying libraries ..");
		for (DependencyInfo info : libs) {
			info.file = libsDir.resolve(info.path);
			if (!info.matches()) {
				toUpdate.add(info);
				totalSize += info.size;
			}
		}

		if (!toUpdate.isEmpty()) {
			long totalSizeF = totalSize;
			Consumer<ProgressTask> consumer = task -> {
				this.logger.info("Downloading missing libraries ..");
				task.setTitle(Translations.libraries_download_title);
				IncrementalListener listener = task.expect(totalSizeF);

				for (DependencyInfo info : toUpdate) {
					if (task.isCancelled())
						return;

					this.logger.info("Downloading library " + info.path + " ..");
					task.setMessage(info.path);

					IOUtil.download(info.url, info.file, listener);

					if (task.isCancelled())
						return;

					if (!info.matches()) {
						task.cancel();
						this.logger.error("Downloaded library seems invalid, aborting ..");
						Popup.error().title(Translations.launch_cancelled).message(Translations.library_signature_invalid.format("path", info.path)).showAndWait();
						return;
					}
				}
			};

			boolean r;
			if (this.UIEnabled)
				r = Popup.consumer(consumer).title(Translations.launch_title).submitAndWait();
			else
				r = App.submit(consumer);

			if (!r) {
				this.logger.info("Some libraries have not been downloaded correctly. The launch is cancelled.");
				shutdownNow();
			}
		}

		this.logger.info("Loading libraries ..");
		for (DependencyInfo info : libs)
			IOUtil.addToClasspath(info.file);
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

	protected final void skipEnvironment() {
		updateEnvironment(new EmptyReleaseSource());
	}

	protected final void updateEnvironment(ReleaseSource appSource) {
		updateEnvironment(appSource, new GithubReleaseSource("Yeregorix", "AppCommonUpdater", null, "Updater"));
	}

	protected final void updateEnvironment(ReleaseSource appSource, ReleaseSource updaterSource) {
		checkState(State.ENVIRONMENT_UPDATE);

		this.appSource = appSource;
		this.updaterSource = updaterSource;

		if (!this.arguments.getFlag("noUpdateCheck").isPresent()) {
			Path appJar = getApplicationJar().orElse(null);
			if (appJar != null) {
				ReleaseInfo latestApp = this.appSource.getLatestRelease().orElse(null);
				if (latestApp != null && !this.version.equals(latestApp.version)) {
					ReleaseInfo latestUpdater = this.updaterSource.getLatestRelease().orElse(null);
					if (latestUpdater != null && suggestApplicationUpdate())
						updateApplication(appJar, latestApp, latestUpdater);
				}
			}
		}

		setState(State.STAGE_INIT);
	}

	public Optional<Path> getApplicationJar() {
		if (this.applicationJar == null) {
			try {
				Path p = Paths.get(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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

	protected final boolean suggestApplicationUpdate() {
		if (this.UIEnabled)
			return Popup.confirmation().title(Translations.update_available_title).message(Translations.update_available_message).submitAndWait();

		if (this.arguments.getFlag("autoUpdate").isPresent())
			return true;

		this.logger.info("An update is available. Please restart with the --autoUpdate argument to update the application.");
		return false;
	}
	
	protected final Stage initStage(double minWidth, double minHeight, boolean resizable, String... icons) {
		return initStage(this.title + " " + this.version, minWidth, minHeight, resizable, icons);
	}
	
	protected final Stage initStage(String title, double minWidth, double minHeight, boolean resizable, String... icons) {
		Image[] images = new Image[icons.length];
		for (int i = 0; i < icons.length; i++)
			images[i] = IOUtil.loadImage(icons[i]);
		return initStage(title, minWidth, minHeight, resizable, images);
	}
	
	protected final Stage initStage(String title, double minWidth, double minHeight, boolean resizable, Image... icons) {
		Stage stage = initStage(title, minWidth, minHeight, resizable);
		stage.getIcons().addAll(icons);
		return stage;
	}
	
	protected final Stage initStage(String title, double minWidth, double minHeight, boolean resizable) {
		Stage stage = new Stage();
		stage.setTitle(title);

		stage.setMinWidth(minWidth);
		stage.setMinHeight(minHeight);
		stage.setWidth(minWidth);
		stage.setHeight(minHeight);
		stage.setResizable(resizable);

		return initStage(stage);
	}

	public final void fatalError(Throwable t) {
		if (this.UIEnabled) {
			try {
				Popup.error().title(this.title + " " + this.version + " - Fatal error").message(t).submitAndWait();
			} catch (Exception ignored) {
			}
		}
		shutdownNow();
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

	public void shutdownNow() {
		shutdownNow(0);
	}

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

	protected final Stage initStage(Stage stage) {
		checkState(State.STAGE_INIT);
		this.stage = stage;
		this.stage.setOnCloseRequest(e -> shutdown());
		return this.stage;
	}

	public final void cancelListeners() {
		for (BaseListener l : this.listeners) {
			try {
				l.cancel();
			} catch (Exception ignored) {
			}
		}
		this.listeners.clear();
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

			boolean launch = this.UIEnabled && !this.arguments.getFlag("noUpdateLaunch").isPresent();
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
			for (String arg : this.arguments.getInitialArguments()) // args
				cmd.add(arg);

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

	public Map<String, String> getLogBlacklist() {
		return this.logBlacklist;
	}

	protected final void skipStage() {
		checkState(State.STAGE_INIT);
		if (this.stage != null)
			throw new IllegalStateException();
		setState(State.RUNNING);
	}

	public final void requireUI() {
		if (!this.UIEnabled)
			throw new IllegalStateException("UI is not enabled");
	}

	public final ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public final Arguments getArguments() {
		return this.arguments;
	}

	public final Path getWorkingDirectory() {
		return this.workingDir;
	}

	protected final Stage initStage(double minWidth, double minHeight, boolean resizable, Image... icons) {
		return initStage(this.title + " " + this.version, minWidth, minHeight, resizable, icons);
	}

	protected final Stage initStage(double minWidth, double minHeight, boolean resizable) {
		return initStage(this.title + " " + this.version, minWidth, minHeight, resizable);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public LoggerFactory getLoggerFactory() {
		if (this.loggerFactory == null)
			throw new IllegalStateException("LoggerFactory not initialized");
		return this.loggerFactory;
	}

	public EventManager getEventManager() {
		if (this.eventManager == null)
			throw new IllegalStateException("EventManager not initialized");
		return this.eventManager;
	}

	public ResourceManager getResourceManager() {
		if (this.resourceManager == null)
			throw new IllegalStateException("ResourceManager not initialized");
		return this.resourceManager;
	}

	public ConnectionConfig getConnectionConfig() {
		if (this.connectionConfig == null) {
			Optional<String> host = this.arguments.getFlag("proxyHost");
			ConnectionConfig.Builder b = ConnectionConfig.builder();
			host.ifPresent(s -> b.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(s, this.arguments.getIntFlag(8080, "proxyPort")))));
			this.connectionConfig = b.connectTimeout(this.arguments.getIntFlag(3000, "connectTimeout")).readTimeout(this.arguments.getIntFlag(3000, "readTimeout"))
					.userAgent(this.arguments.getFlag("userAgent").orElse(null)).bufferSize(this.arguments.getIntFlag(65536, "bufferSize")).build();
		}
		return this.connectionConfig;
	}

	public ExecutorService getExecutor() {
		if (this.executor == null)
			throw new IllegalStateException("ExecutorService not initialized");
		return this.executor;
	}

	public Translator getTranslator() {
		if (this.translator == null)
			throw new IllegalStateException("Translator not initialized");
		return this.translator;
	}

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

	public final void registerListener(BaseListener l) {
		if (this.state == State.SHUTDOWN) {
			try {
				l.cancel();
			} catch (Exception ignored) {
			}
		} else {
			this.listeners.add(l);
		}
	}

	public final String getName() {
		return this.name;
	}

	public final String getTitle() {
		return this.name;
	}

	public final String getVersion() {
		return this.version;
	}

	public Optional<Stage> getStage() {
		return Optional.ofNullable(this.stage);
	}

	public Optional<Scene> getScene() {
		return this.stage == null ? Optional.empty() : Optional.of(this.stage.getScene());
	}

	public static Application get() {
		if (instance == null)
			throw new IllegalStateException("Application instance not available");
		return instance;
	}

	public final boolean isUIEnabled() {
		return this.UIEnabled;
	}
}
