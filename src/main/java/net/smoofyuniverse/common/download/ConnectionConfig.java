/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
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

public class ConnectionConfig {
	public final int connectTimeout, readTimeout, bufferSize;
	public final String userAgent;
	public final Proxy proxy;

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
	
	public BufferedReader openBufferedReader(URL url) throws IOException {
		return new BufferedReader(new InputStreamReader(openStream(url)));
	}
	
	public InputStream openStream(URL url) throws IOException {
		return openConnection(url).getInputStream();
	}
	
	public URLConnection openConnection(URL url) throws IOException {
		return url.openConnection(this.proxy);
	}
	
	public HttpURLConnection openHttpConnection(URL url) throws IOException {
		HttpURLConnection co = (HttpURLConnection) openConnection(url);
		configure(co);
		return co;
	}
	
	public void configure(HttpURLConnection co) {
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
	
	public Builder toBuilder() {
		Builder b = new Builder();
		b.connectTimeout = this.connectTimeout;
		b.readTimeout = this.readTimeout;
		b.bufferSize = this.bufferSize;
		b.userAgent = this.userAgent;
		b.proxy = this.proxy;
		return b;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private int connectTimeout, readTimeout, bufferSize;
		private String userAgent;
		private Proxy proxy;
		
		public Builder connectTimeout(int v) {
			this.connectTimeout = v;
			return this;
		}
		
		public Builder readTimeout(int v) {
			this.readTimeout = v;
			return this;
		}
		
		public Builder bufferSize(int v) {
			this.bufferSize = v;
			return this;
		}
		
		public Builder userAgent(String v) {
			this.userAgent = v;
			return this;
		}
		
		public Builder proxy(Proxy v) {
			this.proxy = v;
			return this;
		}

		public ConnectionConfig build() {
			return new ConnectionConfig(this.proxy, this.userAgent, this.connectTimeout, this.readTimeout, this.bufferSize);
		}
	}
}
