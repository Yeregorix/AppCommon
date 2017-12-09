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

package net.smoofyuniverse.common.download;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.listener.BasicListener;
import net.smoofyuniverse.common.listener.ListenerProvider;
import net.smoofyuniverse.common.util.DownloadUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectoryDownloadTask {
	private static final Logger logger = Application.getLogger("DirectoryDownloadTask");
	
	public final List<FileDownloadTask> files = new ArrayList<>(), toUpdate = new ArrayList<>(), updateFailed = new ArrayList<>();
	public final ConnectionConfiguration config;
	public final Path baseDirectory;
	public final URL baseUrl;
	
	private long totalSize = 0, toUpdateSize = 0;
	
	public DirectoryDownloadTask(URL baseUrl, Path baseDirectory) {
		this(baseUrl, baseDirectory, Application.get().getConnectionConfig());
	}
	
	public DirectoryDownloadTask(URL baseUrl, Path baseDirectory, ConnectionConfiguration config) {
		this.baseUrl = baseUrl;
		this.baseDirectory = baseDirectory;
		this.config = config;
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
		try (BufferedReader in = this.config.openBufferedReader(this.baseUrl)) {
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
				this.files.add(new FileDownloadTask(DownloadUtil.appendUrlSuffix(this.baseUrl, path), this.baseDirectory.resolve(path), this.config, size, digest, digestInstance));
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
