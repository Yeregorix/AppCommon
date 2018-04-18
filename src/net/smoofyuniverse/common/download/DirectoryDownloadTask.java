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

import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.task.listener.IncrementalListener;
import net.smoofyuniverse.common.task.listener.IncrementalListenerProvider;
import net.smoofyuniverse.common.util.DownloadUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectoryDownloadTask {
	private static final Logger logger = App.getLogger("DirectoryDownloadTask");
	
	public final List<FileDownloadTask> files = new ArrayList<>(), toUpdate = new ArrayList<>(), updateFailed = new ArrayList<>();
	public final ConnectionConfiguration config;
	public final Path baseDirectory;
	public final URL baseUrl;
	
	private long totalSize = 0, toUpdateSize = 0;
	
	public DirectoryDownloadTask(URL baseUrl, Path baseDirectory) {
		this(baseUrl, baseDirectory, App.get().getConnectionConfig());
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

	public void update(IncrementalListenerProvider p, boolean force, boolean filter) {
		update(p, force, true, true, filter);
	}

	public void update(IncrementalListenerProvider p, boolean force, boolean verify, boolean deleteFailed, boolean filter) {
		listRemoteFiles(p);
		if (!force)
			checkFilesToUpdate(p);
		updateFiles(p, force, verify);
		if (deleteFailed)
			deleteFailedFiles();
		if (filter)
			filterFiles();
	}

	public void listRemoteFiles(IncrementalListenerProvider p) {
		this.files.clear();
		this.totalSize = 0;
		logger.info("Listing remote files at url '" + this.baseUrl + "' ..");
		long time = System.currentTimeMillis();

		IncrementalListener l = null;
		try (BufferedReader in = this.config.openBufferedReader(this.baseUrl)) {
			l = p.provide(Long.parseLong(in.readLine()) *3);
			String digestInstance = in.readLine();

			l.setMessage(App.translate("dirdl_listing_message"));
			String msg_path = App.translate("dirdl_listing_path");

			String path = null, digest = null;
			String line;
			while ((line = in.readLine()) != null) {
				if (l.isCancelled()) {
					logger.debug("Listing cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
					l.setMessage(App.translate("dirdl_listing_cancelled"));
					return;
				}
				
				if (path == null) {
					path = line;
					l.setMessage(StringUtil.replaceParameters(msg_path, "path", path));
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
			l.setMessage(App.translate("dirdl_listing_end", "count", String.valueOf(this.files.size())));
		} catch (Exception e) {
			logger.warn("Listing at url '" + this.baseUrl + "' failed.", e);
			if (l != null)
				l.setMessage(App.translate("dirdl_listing_error"));
		}
	}

	public void checkFilesToUpdate(IncrementalListenerProvider p) {
		this.toUpdate.clear();
		this.toUpdateSize = 0;
		IncrementalListener l = p.provide(this.files.size());
		logger.info("Checking files to update in directory: " + this.baseDirectory.getFileName() + " ..");
		l.setMessage(App.translate("dirdl_checking_message"));
		long time = System.currentTimeMillis();
		
		for (FileDownloadTask t : this.files) {
			if (l.isCancelled()) {
				logger.debug("Checking cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
				l.setMessage(App.translate("dirdl_checking_cancelled"));
				return;
			}
			
			if (t.shouldUpdate(true)) {
				this.toUpdate.add(t);
				this.toUpdateSize += t.expectedSize();
			}
			
			l.increment(1);
		}

		logger.debug("Checking ended (" + this.toUpdate.size() + " files to update, " + (System.currentTimeMillis() - time) / 1000F + "s).");
		l.setMessage(App.translate("dirdl_checkind_end", "count", String.valueOf(this.toUpdate.size())));
	}

	public void updateFiles(IncrementalListenerProvider p, boolean force, boolean verify) {
		List<FileDownloadTask> list = force ? this.files : this.toUpdate;
		this.updateFailed.clear();
		IncrementalListener l = p.provide(force ? this.totalSize : this.toUpdateSize);
		logger.info("Updating " + list.size() + " files in directory: " + this.baseDirectory.getFileName() + " ..");
		long time = System.currentTimeMillis();

		l.setMessage(App.translate("dirdl_update_message"));
		String msg_creating_dir = App.translate("dirdl_update_creating_directory"), msg_downloading_file = App.translate("dirdl_update_downloading_file");
		
		for (FileDownloadTask t : list) {
			if (l.isCancelled()) {
				logger.debug("Update cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
				l.setMessage(App.translate("dirdl_update_cancelled"));
				return;
			}

			l.setMessage(StringUtil.replaceParameters(t.isDirectory ? msg_creating_dir : msg_downloading_file, "path", t.getPath().toString()));
			if (!t.update(p) || (verify && t.shouldUpdate(false)))
				this.updateFailed.add(t);
		}
		
		logger.debug("Update ended (" + list.size() + " updated files, " + this.updateFailed.size() + " failed, " + (System.currentTimeMillis() - time) /1000F + "s).");
		l.setMessage(App.translate("dirdl_update_end", "success_count", String.valueOf(list.size()), "fail_count", String.valueOf(this.updateFailed.size())));
	}
	
	public void deleteFailedFiles() {
		// TODO
	}
	
	public void filterFiles() {
		// TODO
	}
}
