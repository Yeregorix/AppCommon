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
import net.smoofyuniverse.common.download.ConnectionConfiguration;
import net.smoofyuniverse.common.download.FileDownloadTask;
import net.smoofyuniverse.common.event.app.ApplicationStateChangeEvent;
import net.smoofyuniverse.common.event.core.EventManager;
import net.smoofyuniverse.common.fxui.dialog.Popup;
import net.smoofyuniverse.common.resource.Language;
import net.smoofyuniverse.common.resource.Languages;
import net.smoofyuniverse.common.resource.ResourceManager;
import net.smoofyuniverse.common.resource.ResourceModule;
import net.smoofyuniverse.common.resource.translator.Translator;
import net.smoofyuniverse.common.task.Task;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.ProcessUtil;
import net.smoofyuniverse.common.util.ResourceLoader;
import net.smoofyuniverse.logger.appender.*;
import net.smoofyuniverse.logger.core.LogLevel;
import net.smoofyuniverse.logger.core.Logger;
import net.smoofyuniverse.logger.core.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
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

	private ConnectionConfiguration connectionConfig;
	private LoggerFactory loggerFactory;
	private EventManager eventManager;
	private ResourceManager resourceManager;
	private ExecutorService executor;
	private Logger logger;
	private Translator translator;
	private Stage stage;
	private FileDownloadTask jarUpdateTask, updaterUpdateTask;
	
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
	
	public void checkState(State state) {
		if (this.state != state)
			throw new IllegalStateException();
	}
	
	public State getState() {
		return this.state;
	}

	public abstract void init() throws Exception;
	
	protected final void initServices(ExecutorService executor) {
		initServices(new FormattedAppender(new ParentAppender(PrintStreamAppender.system(), new TransformedAppender(DatedRollingFileAppender.builder().directory(this.workingDir.resolve("logs")).maxFiles(60).build(), App::transformLog)), App::formatLog), executor);
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

		setState(State.STAGE_INIT);
	}

	protected final void loadTranslations(Path dir, String extension) {
		for (Entry<Language, ResourceModule<String>> e : Translator.loadAll(dir, "txt").entrySet())
			this.resourceManager.getOrCreatePack(e.getKey()).addModule(e.getValue());
	}

	protected void loadResources() throws Exception {
		loadTranslations(App.getResource("lang/common"), "txt");
	}

	protected void fillTranslations() throws Exception {
		getTranslator().fill(Translations.class);
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

	public void shutdown() {
		if (this.state == State.SHUTDOWN)
			return;

		this.logger.info("Shutting down ..");
		setState(State.SHUTDOWN);

		this.resourceLoader.close();

		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}

		if (this.UIEnabled)
			Platform.runLater(Platform::exit);
	}
	
	public Optional<Path> getApplicationJarFile() {
		try {
			Path p = Paths.get(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (p.getFileName().toString().endsWith(".jar"))
				return Optional.of(p);
			return Optional.empty();
		} catch (URISyntaxException e) {
			this.logger.warn("Can't get application's jar file", e);
			return Optional.empty();
		}
	}
	
	public boolean shouldUpdate() {
		return this.jarUpdateTask != null && this.jarUpdateTask.shouldUpdate(false);
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

	protected final void checkForUpdate() {
		try {
			checkForUpdate(new URL("https://files.smoofyuniverse.net/apps/" + this.name + ".jar"), new URL("https://files.smoofyuniverse.net/apps/updater/Updater.jar"));
		} catch (MalformedURLException e) {
			this.logger.warn("Failed to form update url", e);
		}
	}

	protected final void checkForUpdate(URL jarUrl, URL updaterUrl) {
		checkState(State.RUNNING);
		if (this.arguments.getFlag("noUpdateCheck").isPresent())
			return;

		Path jarFile = getApplicationJarFile().orElse(null);
		if (jarFile != null) {
			this.jarUpdateTask = new FileDownloadTask(jarUrl, jarFile, -1, null, null);
			this.jarUpdateTask.syncExpectedInfo();
			this.updaterUpdateTask = new FileDownloadTask(updaterUrl, this.workingDir.resolve("Updater.jar"), -1, null, null);
			this.updaterUpdateTask.syncExpectedInfo();
		}

		if (shouldUpdate() && suggestUpdate())
			update();
	}

	public boolean suggestUpdate() {
		if (this.UIEnabled)
			return Popup.confirmation().title(Translations.update_available_title).message(Translations.update_available_message).submitAndWait();

		if (this.arguments.getFlag("autoUpdate").isPresent())
			return true;

		this.logger.info("An update is available. Please restart with the --autoUpdate argument to update the application.");
		return false;
	}

	public void update() {
		if (this.updaterUpdateTask == null || this.jarUpdateTask == null)
			throw new IllegalStateException("Update tasks not initialized");

		Consumer<Task> consumer = (task) -> {
			this.logger.info("Starting application update task ..");
			task.setTitle(Translations.update_download_title);

			if (this.updaterUpdateTask.shouldUpdate(false)) {
				this.logger.info("Downloading latest updater ..");
				this.updaterUpdateTask.update(task);

				if (this.updaterUpdateTask.shouldUpdate(false)) {
					this.logger.error("Updater file seems invalid, aborting ..");
					Popup.error().title(Translations.update_cancelled).message(Translations.updater_signature_invalid).show();
					return;
				}
			}

			Path appJar = this.jarUpdateTask.getPath();
			Path updateJar = this.workingDir.resolve(this.name + "-Update.jar");

			this.jarUpdateTask.setPath(updateJar);
			if (this.jarUpdateTask.shouldUpdate(false)) {
				this.logger.info("Downloading latest application update ..");
				this.jarUpdateTask.update(task);

				if (this.jarUpdateTask.shouldUpdate(false)) {
					this.logger.error("Application update file seems invalid, aborting ..");
					Popup.error().title(Translations.update_cancelled).message(Translations.updater_signature_invalid).show();
					this.jarUpdateTask.setPath(appJar);
					return;
				}
			}
			this.jarUpdateTask.setPath(appJar);

			this.logger.info("Starting updater process ..");
			task.setTitle(Translations.update_process_title);
			task.setMessage(Translations.update_process_message);
			task.setProgress(-1);

			boolean launch = this.UIEnabled && !this.arguments.getFlag("noUpdateLaunch").isPresent();
			if (!launch)
				this.logger.info("The updater will only apply the modifications. You will have to restart the application manually.");

			List<String> cmd = new ArrayList<>();
			cmd.add("java");
			cmd.add("-jar");
			cmd.add(this.updaterUpdateTask.getPath().toAbsolutePath().toString());
			cmd.add("1"); // version
			cmd.add(updateJar.toAbsolutePath().toString()); // source
			cmd.add(appJar.toAbsolutePath().toString()); // target
			cmd.add(String.valueOf(launch)); // launch
			for (String arg : this.arguments.getInitialArguments()) // args
				cmd.add(arg);

			try {
				ProcessUtil.builder().command(cmd).start();
			} catch (IOException e) {
				this.logger.error("Failed to start updater process", e);
				Popup.error().title(Translations.update_cancelled).message(Translations.update_process_error).show();
				return;
			}

			if (this.UIEnabled)
				Platform.runLater(this::shutdown);
			else
				shutdown();
		};

		if (this.UIEnabled)
			Popup.consumer(consumer).title(Translations.update_title).submitAndWait();
		else
			App.submit(consumer);
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

	public ConnectionConfiguration getConnectionConfig() {
		if (this.connectionConfig == null) {
			Optional<String> host = this.arguments.getFlag("proxyHost");
			ConnectionConfiguration.Builder b = ConnectionConfiguration.builder();
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
