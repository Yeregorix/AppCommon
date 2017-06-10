package net.smoofyuniverse.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.listener.BasicListener;
import net.smoofyuniverse.common.listener.ListenerProvider;
import net.smoofyuniverse.common.logger.core.Logger;

public class DownloadUtil {
	private static final Logger logger = Application.getLogger("DownloadUtil");
	
	public static final int prefReadTimeout = Application.get().getArguments().getIntFlag(3000, "readTimeout"),
			prefConnectTimeout = Application.get().getArguments().getIntFlag(3000, "connectTimeout"),
			prefBuffer = Application.get().getArguments().getIntFlag(65536, "downloadBuffer");
	
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
	
	public static BufferedReader openBufferedReader(URL url) throws IOException {
		return new BufferedReader(new InputStreamReader(openStream(url)));
	}
	
	public static InputStream openStream(URL url) throws IOException {
		return openConnection(url).getInputStream();
	}
	
	public static HttpURLConnection openHttpConnection(URL url) throws IOException {
		return openHttpConnection(url, prefConnectTimeout, prefReadTimeout);
	}
	
	public static HttpURLConnection openHttpConnection(URL url, int connectTimeout, int readTimeout) throws IOException {
		HttpURLConnection co = (HttpURLConnection) openConnection(url);
		co.setUseCaches(false);
		co.setDefaultUseCaches(false);
		co.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		co.setRequestProperty("Pragma", "no-cache");
		co.setRequestProperty("Expires", "0");
		co.setConnectTimeout(connectTimeout);
		co.setReadTimeout(readTimeout);
		return co;
	}
	
	public static URLConnection openConnection(URL url) throws IOException {
		return url.openConnection(Application.get().getProxy());
	}
	
	public static boolean download(URL url, Path file, ListenerProvider p) {
		return download(url, file, prefBuffer, p);
	}
	
	public static boolean download(URL url, Path file, int bufferSize, ListenerProvider p) {
		return download(url, prefConnectTimeout, prefReadTimeout, file, bufferSize, p);
	}
	
	public static boolean download(URL url, int connectTimeout, int readTimeout, Path file, ListenerProvider p) {
		return download(url, connectTimeout, readTimeout, file, prefBuffer, p);
	}
	
	public static boolean download(URL url, int connectTimeout, int readTimeout, Path file, int bufferSize, ListenerProvider p) {
		HttpURLConnection co = null;
		try {
			co = openHttpConnection(url);
			co.connect();
			if (co.getResponseCode() / 100 != 2) {
				logger.info("Server at url '" + url + "' returned a bad response code: " + co.getResponseCode());
				return false;
			}
			
			BasicListener l;
			try {
				l = p.provide(Long.parseLong(co.getHeaderField("Content-Length")));
			} catch (NumberFormatException e) {
				l = p.provide(-1);
			}
			
			logger.info("Downloading from url '" + url + "' to file: " + file + " ..");
			long time = System.currentTimeMillis();
			
			try (InputStream in = co.getInputStream();
					OutputStream out = Files.newOutputStream(file)) {
				byte[] buffer = new byte[bufferSize];
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
			co.disconnect();
		}
	}
}
