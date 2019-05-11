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

package net.smoofyuniverse.common.environment.source;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.environment.ReleaseInfo;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public class SimpleReleaseSource implements ReleaseSource {
	private static final Logger logger = App.getLogger("SimpleReleaseSource");

	protected final String appName;
	protected final URL baseUrl;
	protected final ConnectionConfig config;

	public SimpleReleaseSource(String baseUrl, String appName) {
		this(IOUtil.newURL(baseUrl), appName);
	}

	public SimpleReleaseSource(URL baseUrl, String appName) {
		this(baseUrl, appName, App.get().getConnectionConfig());
	}

	public SimpleReleaseSource(URL baseUrl, String appName, ConnectionConfig config) {
		if (baseUrl == null)
			throw new IllegalArgumentException("baseUrl");
		if (appName == null)
			throw new IllegalArgumentException("appName");
		if (config == null)
			throw new IllegalArgumentException("config");

		this.baseUrl = baseUrl;
		this.appName = appName;
		this.config = config;
	}

	@Override
	public Optional<String> getLatestVersion() {
		try (BufferedReader r = this.config.openBufferedReader(getURL("latest"))) {
			return Optional.of(r.readLine());
		} catch (Exception e) {
			logger.warn("Failed to get latest version", e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<ReleaseInfo> getLatestRelease() {
		return getLatestVersion().flatMap(this::getRelease);
	}

	@Override
	public Optional<ReleaseInfo> getRelease(String version) {
		try {
			return Optional.of(getRelease(version, getURL(version + "/" + this.appName + "-" + version + ".json")));
		} catch (Exception e) {
			logger.info("Failed to get release " + version, e);
			return Optional.empty();
		}
	}

	protected URL getURL(String path) throws MalformedURLException {
		return IOUtil.appendSuffix(this.baseUrl, path);
	}

	protected ReleaseInfo getRelease(String version, URL url) throws Exception {
		try (Reader r = this.config.openReader(url)) {
			return getRelease(version, JsonParser.object().withLazyNumbers().from(r));
		}
	}

	protected ReleaseInfo getRelease(String version, JsonObject obj) throws Exception {
		return new ReleaseInfo(version, Instant.parse(obj.getString("date")), obj.getObject("extra"),
				getURL(version + "/" + this.appName + "-" + version + ".jar"), obj.getNumber("size").longValue(), obj.getString("sha1"), "sha1");
	}
}
