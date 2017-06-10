package net.smoofyuniverse.common.download;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.listener.BasicListener;
import net.smoofyuniverse.common.listener.ListenerProvider;
import net.smoofyuniverse.common.logger.core.Logger;
import net.smoofyuniverse.common.util.DownloadUtil;

public class DirectoryDownloadTask {
	private static final Logger logger = Application.getLogger("DirectoryDownloadTask");
	
	public final URL baseUrl;
	public final Path baseDirectory;
	public final int connectTimeout, readTimeout, bufferSize;
	public final List<FileDownloadTask> files = new ArrayList<>(), toUpdate = new ArrayList<>(), updateFailed = new ArrayList<>();
	
	private long totalSize = 0, toUpdateSize = 0;
	
	public DirectoryDownloadTask(URL baseUrl, Path baseDirectory) {
		this(baseUrl, baseDirectory, DownloadUtil.prefBuffer);
	}
	
	public DirectoryDownloadTask(URL baseUrl, Path baseDirectory, int bufferSize) {
		this(baseUrl, DownloadUtil.prefConnectTimeout, DownloadUtil.prefReadTimeout, baseDirectory, bufferSize);
	}
	
	public DirectoryDownloadTask(URL baseUrl, int connectTimeout, int readTimeout, Path baseDirectory) {
		this(baseUrl, connectTimeout, readTimeout, baseDirectory, DownloadUtil.prefBuffer);
	}
	
	public DirectoryDownloadTask(URL baseUrl, int connectTimeout, int readTimeout, Path baseDirectory, int bufferSize) {
		this.baseUrl = baseUrl;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.baseDirectory = baseDirectory;
		this.bufferSize = bufferSize;
	}
	
	public long totalSize() {
		return this.totalSize;
	}
	
	public long toUpdateSize() {
		return this.toUpdateSize;
	}
	
	public void update(ListenerProvider p, boolean force, boolean filter) {
		update(p, force, true, true, filter);
	}
	
	public void update(ListenerProvider p, boolean force, boolean verify, boolean deleteFailed, boolean filter) {
		listRemoteFiles(p);
		if (!force)
			listFilesToUpdate(p);
		updateFiles(p, force, verify);
		if (deleteFailed)
			deleteFailedFiles();
		if (filter)
			filterFiles();
	}
	
	public void listRemoteFiles(ListenerProvider p) {
		this.files.clear();
		this.totalSize = 0;
		logger.info("Listing remote files at url '" + this.baseUrl + "' ..");
		long time = System.currentTimeMillis();
		
		BasicListener l = null;
		try (BufferedReader in = DownloadUtil.openBufferedReader(this.baseUrl)) {
			l = p.provide(Long.parseLong(in.readLine()) *3);
			String digestInstance = in.readLine();
			
			l.setMessage("Listage ..");
			String path = null, digest = null;
			String line;
			while ((line = in.readLine()) != null) {
				if (l.isCancelled()) {
					logger.debug("Listing cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
					l.setMessage("Listage annulé.");
					return;
				}
				
				if (path == null) {
					path = line;
					l.setMessage("Listage: " + path);
					continue;
				}
				if (digest == null) {
					digest = line;
					continue;
				}
				
				long size = Long.parseLong(line);
				this.files.add(new FileDownloadTask(DownloadUtil.appendUrlSuffix(this.baseUrl, path), this.connectTimeout, this.readTimeout,
						this.baseDirectory.resolve(path), size, digest, digestInstance, this.bufferSize));
				this.totalSize += size;
				l.increment(1);
			}
			
			logger.debug("Listing ended (" + this.files.size() + " files, " + (System.currentTimeMillis() - time) /1000F + "s).");
			l.setMessage("Listage terminé (" + this.files.size() + " fichiers listés).");
		} catch (Exception e) {
			logger.warn("Listing at url '" + this.baseUrl + "' failed.", e);
			if (l != null)
				l.setMessage("Listage interrompu par une erreur.");
		}
	}
	
	public void listFilesToUpdate(ListenerProvider p) {
		this.toUpdate.clear();
		this.toUpdateSize = 0;
		BasicListener l = p.provide(this.files.size());
		logger.info("Listing files to update in directory: " + this.baseDirectory.getFileName() + " ..");
		l.setMessage("Vérification des fichiers à jour ..");
		long time = System.currentTimeMillis();
		
		for (FileDownloadTask t : this.files) {
			if (l.isCancelled()) {
				logger.debug("Checking cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
				l.setMessage("Vérification annulée.");
				return;
			}
			
			if (t.shouldUpdate(true)) {
				this.toUpdate.add(t);
				this.toUpdateSize += t.expectedSize();
			}
			
			l.increment(1);
		}
		
		logger.debug("Listing ended (" + this.toUpdate.size() + " files to update, " + (System.currentTimeMillis() - time) /1000F + "s).");
		l.setMessage("Vérification terminée (" + this.toUpdate.size() + " fichiers à mettre à jour).");
	}
	
	public void updateFiles(ListenerProvider p, boolean force, boolean verify) {
		List<FileDownloadTask> list = force ? this.files : this.toUpdate;
		this.updateFailed.clear();
		BasicListener l = p.provide(force ? this.totalSize : this.toUpdateSize);
		logger.info("Updating " + list.size() + " files in directory: " + this.baseDirectory.getFileName() + " ..");
		l.setMessage("Mise à jour des fichiers ..");
		long time = System.currentTimeMillis();
		
		for (FileDownloadTask t : list) {
			if (l.isCancelled()) {
				logger.debug("Update cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
				l.setMessage("Mise à jour annulée.");
				return;
			}
			
			l.setMessage((t.isDirectory ? "Création du dossier: " : "Téléchargement du fichier: ") + t.getPath() + " ..");
			if (!t.update(p) || (verify && t.shouldUpdate(false)))
				this.updateFailed.add(t);
		}
		
		logger.debug("Update ended (" + list.size() + " updated files, " + this.updateFailed.size() + " failed, " + (System.currentTimeMillis() - time) /1000F + "s).");
		l.setMessage("Mise à jour terminée (" + list.size() + " fichiers mis à jour, " + this.updateFailed.size() + " échoués).");
	}
	
	public void deleteFailedFiles() {
		// TODO
	}
	
	public void filterFiles() {
		// TODO
	}
}
