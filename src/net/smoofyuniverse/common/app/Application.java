/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.smoofyuniverse.common.download.FileDownloadTask;
import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.app.ApplicationStateEvent;
import net.smoofyuniverse.common.event.core.EventManager;
import net.smoofyuniverse.common.event.core.ListenerRegistration;
import net.smoofyuniverse.common.event.installation.InstallationDetailsLoadEvent;
import net.smoofyuniverse.common.event.installation.InstallationDetailsSaveEvent;
import net.smoofyuniverse.common.event.installation.KeyStoreCreationEvent;
import net.smoofyuniverse.common.event.installation.KeyStoreLoadEvent;
import net.smoofyuniverse.common.event.installation.KeyStorePostCreationEvent;
import net.smoofyuniverse.common.fxui.dialog.Popup;
import net.smoofyuniverse.common.fxui.task.ObservableTask;
import net.smoofyuniverse.common.installation.InstallationDetails;
import net.smoofyuniverse.common.installation.KeyStoreBuilder;
import net.smoofyuniverse.common.logger.appender.DatedRollingFileAppender;
import net.smoofyuniverse.common.logger.appender.LogAppender;
import net.smoofyuniverse.common.logger.appender.PrintStreamAppender;
import net.smoofyuniverse.common.logger.core.LogLevel;
import net.smoofyuniverse.common.logger.core.Logger;
import net.smoofyuniverse.common.logger.core.LoggerFactory;
import net.smoofyuniverse.common.util.ProcessUtil;
import net.smoofyuniverse.common.util.ResourceUtil;

public abstract class Application {
	private static Application instance;
	
	private State state = State.CREATION;
	private Arguments arguments;
	private Path workingDir;
	private Proxy proxy;
	private String name, title, version;
	
	private LoggerFactory loggerFactory;
	
	private EventManager eventManager;
	private ExecutorService executor;
	private Logger logger;
	
	private InstallationDetails installationDetails;
	
	private Stage stage;
	
	private boolean updateKeyStore;
	
	private FileDownloadTask jarUpdateTask, updaterUpdateTask;
	
	public Application(Arguments args, String name, String version) {
		this(args, name, name, version);
	}
	
	public Application(Arguments args, String name, String title, String version) {
		this(args, getDirectory(args, name), getProxy(args), name, title, version);
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
	
	private static Proxy getProxy(Arguments args) {
		Optional<String> host = args.getFlag("proxyHost");
		if (host.isPresent())
			return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host.get(), args.getIntFlag(8080, "proxyPort")));
		return Proxy.NO_PROXY;
	}
	
	public Application(Arguments args, Path dir, Proxy proxy, String name, String title, String version) {
		if (instance != null)
			throw new IllegalStateException("An application instance already exists");
		instance = this;
		
		this.arguments = args;
		this.workingDir = dir;
		this.proxy = proxy;
		this.name = name;
		this.title = title;
		this.version = version;
		
		this.installationDetails = new InstallationDetails(dir.resolve("installation-details"));
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		setState(State.SERVICES_INIT);
	}
	
	private void setState(State state) {
		if (this.eventManager != null)
			this.eventManager.postEvent(new ApplicationStateEvent(this, this.state, state));
		this.state = state;
	}
	
	public void checkState(State state) {
		if (this.state != state)
			throw new IllegalStateException();
	}
	
	public State getState() {
		return this.state;
	}
	
	protected final void initServices(ExecutorService executor) {
		initServices(LogAppender.formattedParent(PrintStreamAppender.system(), DatedRollingFileAppender.logs()), executor);
	}
	
	protected final void initServices(LogAppender appender, ExecutorService executor) {
		initServices(new LoggerFactory(appender), executor);
	}
	
	protected final void initServices(LoggerFactory loggerFactory, ExecutorService executor) {
		initServices(loggerFactory, new EventManager(loggerFactory), executor);
	}
	
	protected final void initServices(LoggerFactory loggerFactory, EventManager eventManager, ExecutorService executor) {
		checkState(State.SERVICES_INIT);
		
		long time = System.currentTimeMillis();
		
		this.loggerFactory = loggerFactory;
		this.eventManager = eventManager;
		this.executor = executor;
		this.logger = loggerFactory.provideLogger("Application");
		
		this.eventManager.registerAll(this, false);
		
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> this.logger.log(LogLevel.ERROR, t, "Uncaught exception in thread: " + t.getName(), e));
		
		if (Files.exists(this.workingDir))
			this.logger.debug("Working directory: " + this.workingDir.toAbsolutePath());
		
		if (loadInstallationDetails(true))
			checkInstallationDetails();
		else
			shutdown();
		
		installKeyStore();
		
		long dur = System.currentTimeMillis() - time;
		this.logger.debug("Started " + this.name + " v" + this.version + " (" + dur + "ms).");
		
		setState(State.STAGE_INIT);
	}
	
	private void checkInstallationDetails() {
		this.updateKeyStore = this.installationDetails.getVersion("common.keystore") != 1;
	}
	
	protected final boolean loadInstallationDetails(boolean autoSave) {
		boolean success;
		try {
			this.installationDetails.read();
			success = true;
		} catch (IOException e) {
			this.logger.error("Failed to read installation details", e);
			success = false;
		}
		this.eventManager.postEvent(new InstallationDetailsLoadEvent(this.installationDetails, success));
		if (this.installationDetails.changed())
			saveInstallationDetails();
		return success;
	}
	
	protected final boolean saveInstallationDetails() {
		boolean success;
		try {
			this.installationDetails.save();
			success = true;
		} catch (IOException e) {
			this.logger.error("Failed to save installation details", e);
			success = false;
		}
		this.eventManager.postEvent(new InstallationDetailsSaveEvent(this.installationDetails, success));
		return success;
	}
	
	protected final Stage initStage(double minWidth, double minHeight, boolean resizable, String... icons) {
		return initStage(this.title + " v" + this.version, minWidth, minHeight, resizable, icons);
	}
	
	protected final Stage initStage(double minWidth, double minHeight, boolean resizable, Image... icons) {
		return initStage(this.title + " v" + this.version, minWidth, minHeight, resizable, icons);
	}
	
	protected final Stage initStage(double minWidth, double minHeight, boolean resizable) {
		return initStage(this.title + " v" + this.version, minWidth, minHeight, resizable);
	}
	
	protected final Stage initStage(String title, double minWidth, double minHeight, boolean resizable, String... icons) {
		Image[] images = new Image[icons.length];
		for (int i = 0; i < icons.length; i++)
			images[i] = ResourceUtil.loadImage(icons[i]);
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
	
	protected final Stage initStage(Stage stage) {
		checkState(State.STAGE_INIT);
		this.stage = stage;
		this.stage.setOnCloseRequest((e) -> shutdown());
		return this.stage;
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
	
	private void installKeyStore() {
		Path file = getWorkingDirectory().resolve(".keystore");
		KeyStoreLoadEvent ev = new KeyStoreLoadEvent(file, !Files.exists(file) || this.updateKeyStore);
		this.eventManager.postEvent(ev);
		if (ev.createNew()) {
			boolean success;
			try {
				KeyStoreBuilder b = new KeyStoreBuilder();
				b.load();
				b.installCertificate("smoofyuniverse.net", 0);
				this.eventManager.postEventUnchecked(new KeyStoreCreationEvent(b));
				b.save(file);
				success = true;
			} catch (Exception e) {
				getLogger().error("Failed to create a new keystore", e);
				success = false;
			}
			
			if (success) {
				this.installationDetails.setVersion("common.keystore", 1);
				this.updateKeyStore = false;
			}	
			
			this.eventManager.postEvent(new KeyStorePostCreationEvent(success));
			
			if (this.installationDetails.changed())
				saveInstallationDetails();
		}
		System.setProperty("javax.net.ssl.trustStore", file.toAbsolutePath().toString());
	}
	
	protected final void checkForUpdate() {
		try {
			checkForUpdate(new URL("https://files.smoofyuniverse.net/apps/" + this.name + ".jar"), new URL("https://files.smoofyuniverse.net/apps/Updater.jar"));
		} catch (MalformedURLException e) {
			this.logger.warn("Failed to form update url", e);
		}
	}
	
	protected final void checkForUpdate(URL jarUrl, URL updateUrl) {
		Path jarFile = getApplicationJarFile().orElse(null);
		if (jarFile != null) {
			this.jarUpdateTask = new FileDownloadTask(jarUrl, jarFile, -1, null, null);
			this.jarUpdateTask.syncExpectedInfos();
			this.updaterUpdateTask = new FileDownloadTask(updateUrl, this.workingDir.resolve("Updater.jar"), -1, null, null);
			this.updaterUpdateTask.syncExpectedInfos();
		}
		
		if (shouldUpdate() && suggestUpdate())
			update();
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
		if (this.jarUpdateTask == null)
			return false;
		return this.jarUpdateTask.shouldUpdate(false);
	}
	
	public boolean suggestUpdate() {
		return Popup.confirmation().title("Mise à jour disponible").message("Une mise à jour est disponible.\nSouhaitez vous l'installer ?").submitAndWait();
	}
	
	public void update() {
		Consumer<ObservableTask> consumer = (task) -> {
			this.logger.info("Starting application update task ..");
			task.setTitle("Téléchargement de la mise à jour ..");
			
			if (this.updaterUpdateTask.shouldUpdate(false)) {
				this.logger.info("Downloading latest updater ..");
				this.updaterUpdateTask.update(task);
				
				if (this.updaterUpdateTask.shouldUpdate(false)) {
					this.logger.error("Updater file seems invalid, aborting ..");
					Popup.error().title("Mise à jour annulée").message("L'application de mise à jour possède une signature incorrecte.\nPar sécurité, la mise à jour a été annulée.").show();
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
					Popup.error().title("Mise à jour annulée").message("La mise à jour téléchargée possède une signature incorrecte.\nPar sécurité, la mise à jour a été annulée.").show();
					this.jarUpdateTask.setPath(appJar);
					return;
				}
			}
			this.jarUpdateTask.setPath(appJar);
			
			this.logger.info("Starting updater process ..");
			task.setTitle("Processus de mise à jour ..");
			task.setMessage("Démarrage du processus ..");
			task.setProgress(-1);
			
			List<String> cmd = new ArrayList<>();
			cmd.add("java");
			cmd.add("-jar");
			cmd.add(this.updaterUpdateTask.getPath().toAbsolutePath().toString());
			cmd.add(updateJar.toAbsolutePath().toString());
			cmd.add(appJar.toAbsolutePath().toString());
			for (String arg : this.arguments.getInitialArguments())
				cmd.add(arg);
			
			try {
				ProcessUtil.builder().command(cmd).start();
			} catch (IOException e) {
				this.logger.error("Failed to start updater process", e);
				Popup.error().title("Mise à jour annulée").message("Une erreur est survenue lors du démarrage du processus de mise à jour.").show();
				return;
			}
			
			Platform.runLater(this::shutdown);
		};
		
		Popup.consumer(consumer).title("Mise à jour de l'application ..").submitAndWait();
	}
	
	public Arguments getArguments() {
		return this.arguments;
	}
	
	public Path getWorkingDirectory() {
		return this.workingDir;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTitle() {
		return this.name;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public LoggerFactory getLoggerFactory() {
		return this.loggerFactory;
	}
	
	public EventManager getEventManager() {
		return this.eventManager;
	}
	
	public ExecutorService getExecutor() {
		return this.executor;
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public InstallationDetails getInstallationDetails() {
		if (this.installationDetails == null)
			throw new IllegalStateException("Installation details not available");
		return this.installationDetails;
	}
	
	public Stage getStage() {
		if (this.stage == null)
			throw new IllegalStateException("Stage not available");
		return this.stage;
	}
	
	public Proxy getProxy() {
		return this.proxy;
	}
	
	public void shutdown() {
		this.logger.debug("Shutting down ..");
		setState(State.SHUTDOWN);
		this.executor.shutdown();
		Platform.runLater(Platform::exit);
	}
	
	public static Logger getLogger(String name) {
		return get().getLoggerFactory().provideLogger(name);
	}
	
	public static boolean registerListener(ListenerRegistration l) {
		return get().getEventManager().register(l);
	}
	
	public static boolean postEvent(Event e) {
		return get().getEventManager().postEvent(e);
	}
	
	public static boolean isShutdown() {
		return get().state == State.SHUTDOWN;
	}
	
	public static Application get() {
		if (instance == null)
			throw new IllegalStateException("Application instance not available");
		return instance;
	}
	
	static {
		Platform.setImplicitExit(false);
		new JFXPanel();
	}
}
