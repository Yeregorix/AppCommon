package net.smoofyuniverse.common.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.listener.ListenerProvider;
import net.smoofyuniverse.common.logger.core.Logger;
import net.smoofyuniverse.common.util.DownloadUtil;
import net.smoofyuniverse.common.util.ResourceUtil;
import net.smoofyuniverse.common.util.StringUtil;

public class FileDownloadTask {
	private static final Logger logger = Application.getLogger("FileDownloadTask");
	
	public final URL url;
	public final int connectTimeout, readTimeout, bufferSize;
	public final boolean isDirectory;
	
	private String expectedDigest, digestInstance;
	private long expectedSize;
	
	private Path path;
	
	public FileDownloadTask(URL url, Path path, long expectedSize, String expectedDigest, String digestInstance) {
		this(url, path, expectedSize, expectedDigest, digestInstance, DownloadUtil.prefBuffer);
	}
	
	public FileDownloadTask(URL url, Path path, long expectedSize, String expectedDigest, String digestInstance, int bufferSize) {
		this(url, DownloadUtil.prefConnectTimeout, DownloadUtil.prefReadTimeout, path, expectedSize, expectedDigest, digestInstance, bufferSize);
	}
	
	public FileDownloadTask(URL url, int connectTimeout, int readTimeout, Path path, long expectedSize, String expectedDigest, String digestInstance) {
		this(url, connectTimeout, readTimeout, path, expectedSize, expectedDigest, digestInstance, DownloadUtil.prefBuffer);
	}
	
	public FileDownloadTask(URL url, int connectTimeout, int readTimeout, Path path, long expectedSize, String expectedDigest, String digestInstance, int bufferSize) {
		if (expectedSize < 0 && expectedSize != -1)
			throw new IllegalArgumentException("Size must be positive or indefinite");
		this.url = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.path = path;
		this.bufferSize = bufferSize;
		this.isDirectory = url.getFile().endsWith("/");
		if (!this.isDirectory) {
			this.expectedSize = expectedSize;
			this.expectedDigest = expectedDigest;
			this.digestInstance = digestInstance;
		}
	}
	
	public Optional<String> expectedDigest() {
		return Optional.ofNullable(this.expectedDigest);
	}
	
	public Optional<String> digestInstance() {
		return Optional.ofNullable(this.digestInstance);
	}
	
	public long expectedSize() {
		return this.expectedSize;
	}
	
	public Path getPath() {
		return this.path;
	}
	
	public void setPath(Path p) {
		if (p == null)
			throw new IllegalArgumentException("Null path");
		this.path = p;
	}
	
	public Optional<String> localDigest() {
		if (this.isDirectory || this.digestInstance == null || !exists())
			return Optional.empty();
		try {
			return Optional.of(StringUtil.toHexString(ResourceUtil.digest(this.path, this.digestInstance)));
		} catch (Exception e) {
			logger.warn("Can't get " + this.digestInstance + " digest of file: " + this.path, e);
			return Optional.empty();
		}
	}
	
	public boolean shouldSync() {
		if (this.isDirectory)
			return false;
		return this.expectedSize == -1 || this.expectedDigest == null || this.digestInstance == null;
	}
	
	public boolean exists() {
		return Files.exists(this.path);
	}
	
	public boolean shouldUpdate(boolean unknown) {
		if (!exists())
			return true;
		
		if (this.isDirectory)
			return !Files.isDirectory(this.path);
		
		if (this.expectedSize == -1)
			return unknown;
		try {
			if (this.expectedSize != Files.size(this.path))
				return true;
		} catch (IOException e) {
			logger.warn("Can't get size of file: " + this.path, e);
			return unknown;
		}
		
		if (this.expectedDigest == null)
			return unknown;
		String localDigest = localDigest().orElse(null);
		if (localDigest == null)
			return unknown;
		if (!this.expectedDigest.equals(localDigest))
			return true;
		
		return false;
	}
	
	public void syncExpectedInfos() {
		if (this.isDirectory)
			return;
		URL url = null;
		try {
			url = DownloadUtil.editUrlSuffix(this.url, (s) -> {
				int i = s.lastIndexOf('/') +1;
				return s.substring(0, i) + "?info=" + s.substring(i);
			});
			
			try (BufferedReader in = DownloadUtil.openBufferedReader(url)) {
				this.expectedSize = Long.parseLong(in.readLine());
				this.expectedDigest = in.readLine();
				this.digestInstance = in.readLine();
			}
		} catch (Exception e) {
			logger.warn("Can't sync with url '" + url + "'", e);
		}
	}
	
	public boolean update(ListenerProvider p) {
		if (this.isDirectory) {
			try {
				Files.deleteIfExists(this.path);
				Files.createDirectory(this.path);
				return true;
			} catch (IOException e) {
				logger.warn("Can't create directory: " + this.path, e);
				return false;
			}
		}
		return DownloadUtil.download(this.url, this.connectTimeout, this.readTimeout, this.path, this.bufferSize, p.provide(this.expectedSize));
	}
}
