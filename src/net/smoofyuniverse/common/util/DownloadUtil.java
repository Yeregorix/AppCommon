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

package net.smoofyuniverse.common.util;

import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.download.ConnectionConfiguration;
import net.smoofyuniverse.common.task.listener.IncrementalListener;
import net.smoofyuniverse.common.task.listener.IncrementalListenerProvider;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class DownloadUtil {
	private static final Logger logger = App.getLogger("DownloadUtil");
	
	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to encode string '" + s + "' in UTF-8 format", e);
			return s;
		}
	}
	
	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to decode string '" + s + "' in UTF-8 format", e);
			return s;
		}
	}
	
	public static URL appendUrlSuffix(URL url, String suffix) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + suffix);
	}
	
	public static URL editUrlSuffix(URL url, UnaryOperator<String> edit) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), edit.apply(url.getFile()));
	}

	public static boolean download(URL url, Path file, IncrementalListenerProvider p) {
		return download(url, file, App.get().getConnectionConfig(), p);
	}

	public static boolean download(URL url, Path file, ConnectionConfiguration config, IncrementalListenerProvider p) {
		HttpURLConnection co = null;
		try {
			co = config.openHttpConnection(url);
			co.connect();
			if (co.getResponseCode() / 100 != 2) {
				logger.info("Server at url '" + url + "' returned a bad response code: " + co.getResponseCode());
				return false;
			}

			IncrementalListener l;
			try {
				l = p.provide(Long.parseLong(co.getHeaderField("Content-Length")));
			} catch (NumberFormatException e) {
				l = p.provide(-1);
			}
			
			logger.info("Downloading from url '" + url + "' to file: " + file + " ..");
			long time = System.currentTimeMillis();
			
			try (InputStream in = co.getInputStream();
					OutputStream out = Files.newOutputStream(file)) {
				byte[] buffer = new byte[config.bufferSize];
				int length;
				while ((length = in.read(buffer)) > 0) {
					if (l.isCancelled()) {
						logger.debug("Download cancelled (" + (System.currentTimeMillis() - time) /1000F + "s).");
						return false;
					}
					out.write(buffer, 0, length);
					l.increment(length);
				}
			}
			logger.debug("Download ended (" + (System.currentTimeMillis() - time) /1000F + "s).");
			return true;
		} catch (IOException e) {
			logger.warn("Download from url '" + url + "' failed.", e);
			return false;
		} finally {
			if (co != null)
				co.disconnect();
		}
	}
}
