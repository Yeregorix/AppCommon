/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * A configuration for URL connections.
 */
public class ConnectionConfig {
	/**
	 * The connect timeout in milliseconds.
	 * See {@link URLConnection#setConnectTimeout(int)}.
	 */
	public final int connectTimeout;

	/**
	 * The read timeout in milliseconds.
	 * See {@link URLConnection#setReadTimeout(int)}.
	 */
	public final int readTimeout;

	/**
	 * The default byte buffer size for most IO operations with an open connection.
	 */
	public final int bufferSize;

	/**
	 * The user agent.
	 */
	public final String userAgent;

	/**
	 * The proxy.
	 */
	public final Proxy proxy;

	/**
	 * Creates a new configuration.
	 *
	 * @param proxy          The proxy.
	 * @param userAgent      The user agent.
	 * @param connectTimeout The connect timeout.
	 * @param readTimeout    The read timeout.
	 * @param bufferSize     The default buffer size.
	 */
	public ConnectionConfig(Proxy proxy, String userAgent, int connectTimeout, int readTimeout, int bufferSize) {
		if (connectTimeout < 0)
			throw new IllegalArgumentException("connectTimeout");
		if (readTimeout < 0)
			throw new IllegalArgumentException("readTimeout");
		if (bufferSize < 64)
			throw new IllegalArgumentException("bufferSize");

		this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
		this.userAgent = userAgent;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.bufferSize = bufferSize;
	}

	/**
	 * Opens and configures the URL connection.
	 * Gets the input stream wrapped in a buffered reader.
	 *
	 * @param url The URL.
	 * @return The buffered reader.
	 * @throws IOException if an I/O exception occurs.
	 */
	public BufferedReader openBufferedReader(URL url) throws IOException {
		return new BufferedReader(new InputStreamReader(openStream(url)));
	}

	/**
	 * Opens and configures the URL connection.
	 * Gets the input stream.
	 *
	 * @param url The URL.
	 * @return The input stream.
	 * @throws IOException if an I/O exception occurs.
	 */
	public InputStream openStream(URL url) throws IOException {
		return openConnection(url).getInputStream();
	}

	/**
	 * Opens and configures the URL connection.
	 * See {@link URL#openConnection()}
	 *
	 * @param url The URL.
	 * @return The URL connection.
	 * @throws IOException if an I/O exception occurs.
	 */
	public URLConnection openConnection(URL url) throws IOException {
		URLConnection co = url.openConnection(this.proxy);
		configure(co);
		return co;
	}

	/**
	 * Configures the URL connection.
	 * Sets timeouts, user agent and disables cache.
	 *
	 * @param co The URL connection.
	 */
	public void configure(URLConnection co) {
		co.setUseCaches(false);
		co.setDefaultUseCaches(false);
		co.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		co.setRequestProperty("Pragma", "no-cache");
		co.setRequestProperty("Expires", "0");

		if (this.userAgent != null)
			co.setRequestProperty("User-Agent", this.userAgent);
		co.setConnectTimeout(this.connectTimeout);
		co.setReadTimeout(this.readTimeout);
	}

	/**
	 * Opens and configures the URL connection.
	 * Casts to an HTTP connection.
	 *
	 * @param url The URL.
	 * @return The HTTP connection.
	 * @throws IOException if an I/O exception occurs.
	 */
	public HttpURLConnection openHttpConnection(URL url) throws IOException {
		return (HttpURLConnection) openConnection(url);
	}

	/**
	 * Copies this configuration in a new builder.
	 *
	 * @return The new builder.
	 */
	public Builder toBuilder() {
		Builder b = new Builder();
		b.connectTimeout = this.connectTimeout;
		b.readTimeout = this.readTimeout;
		b.bufferSize = this.bufferSize;
		b.userAgent = this.userAgent;
		b.proxy = this.proxy;
		return b;
	}

	/**
	 * Creates a new builder.
	 *
	 * @return The new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder for {@link ConnectionConfig}.
	 */
	public static class Builder {
		private int connectTimeout, readTimeout, bufferSize;
		private String userAgent;
		private Proxy proxy;

		/**
		 * Sets the connect timeout.
		 *
		 * @param v The connect timeout.
		 * @return this.
		 */
		public Builder connectTimeout(int v) {
			this.connectTimeout = v;
			return this;
		}

		/**
		 * Sets the read timeout.
		 *
		 * @param v The read timeout.
		 * @return this.
		 */
		public Builder readTimeout(int v) {
			this.readTimeout = v;
			return this;
		}

		/**
		 * Sets the default buffer size.
		 *
		 * @param v The default buffer size.
		 * @return this.
		 */
		public Builder bufferSize(int v) {
			this.bufferSize = v;
			return this;
		}

		/**
		 * Sets the user agent.
		 *
		 * @param v The user agent.
		 * @return this.
		 */
		public Builder userAgent(String v) {
			this.userAgent = v;
			return this;
		}

		/**
		 * Sets the proxy.
		 *
		 * @param v The proxy.
		 * @return this.
		 */
		public Builder proxy(Proxy v) {
			this.proxy = v;
			return this;
		}

		/**
		 * Builds a new configuration from this builder.
		 *
		 * @return The new configuration.
		 */
		public ConnectionConfig build() {
			return new ConnectionConfig(this.proxy, this.userAgent, this.connectTimeout, this.readTimeout, this.bufferSize);
		}
	}
}
