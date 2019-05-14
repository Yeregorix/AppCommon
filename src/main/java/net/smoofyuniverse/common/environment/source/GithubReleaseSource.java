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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public class GithubReleaseSource implements ReleaseSource {
	private static final Logger logger = App.getLogger("GithubReleaseSource");
	public static final URL URL_BASE;

	protected final String owner, repo, accessToken, appName;
	protected final ConnectionConfig config;

	public GithubReleaseSource(String owner, String repo, String accessToken, String appName) {
		this(owner, repo, accessToken, appName, App.get().getConnectionConfig());
	}

	public GithubReleaseSource(String owner, String repo, String accessToken, String appName, ConnectionConfig config) {
		if (owner == null || owner.isEmpty())
			throw new IllegalArgumentException("owner");
		if (repo == null || repo.isEmpty())
			throw new IllegalArgumentException("repo");
		if (config == null)
			throw new IllegalArgumentException("config");

		this.owner = owner;
		this.repo = repo;
		this.accessToken = accessToken;
		this.appName = appName;
		this.config = config;
	}

	@Override
	public Optional<String> getLatestVersion() {
		return getLatestRelease().map(info -> info.version);
	}

	@Override
	public Optional<ReleaseInfo> getLatestRelease() {
		try {
			return Optional.of(getRelease(getURL("releases/latest")));
		} catch (Exception e) {
			logger.info("Failed to get latest release", e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<ReleaseInfo> getRelease(String version) {
		try {
			return Optional.of(getRelease(getURL("releases/tags/" + version)));
		} catch (Exception e) {
			logger.info("Failed to get release " + version, e);
			return Optional.empty();
		}
	}

	protected ReleaseInfo getRelease(URL url) throws Exception {
		try (InputStream in = this.config.openStream(url)) {
			return getRelease(JsonParser.object().withLazyNumbers().from(in));
		}
	}

	protected URL getURL(String path) throws MalformedURLException {
		if (this.accessToken != null)
			path += "?access_token=" + this.accessToken;
		return IOUtil.appendSuffix(URL_BASE, this.owner + "/" + this.repo + "/" + path);
	}

	protected ReleaseInfo getRelease(JsonObject obj) throws Exception {
		String version = obj.getString("tag_name");
		Instant date = Instant.parse(obj.getString("published_at"));

		String jarName = this.appName + "-" + version + ".jar", jsonName = this.appName + "-" + version + ".json";
		JsonObject jarAsset = null, jsonAsset = null;

		for (Object child : obj.getArray("assets")) {
			JsonObject asset = (JsonObject) child;
			String name = asset.getString("name");

			if (jarName.equals(name))
				jarAsset = asset;
			else if (jsonName.equals(name))
				jsonAsset = asset;
		}

		if (jarAsset == null)
			throw new IllegalStateException("Jar not found");

		URL url = newURL(jarAsset.getString("url"));
		long size = jarAsset.getNumber("size").longValue();

		JsonObject extraData = null;
		String digest = null;

		if (jsonAsset != null) {
			HttpURLConnection co = this.config.openHttpConnection(newURL(jsonAsset.getString("url")));
			try {
				co.setRequestProperty("Accept", "application/octet-stream");
				try (InputStream in = co.getInputStream()) {
					JsonObject data = JsonParser.object().withLazyNumbers().from(in);
					digest = data.getString("sha1");
					extraData = data.getObject("extra");
				}
			} finally {
				co.disconnect();
			}
		}

		return new ReleaseInfo(version, date, extraData, url, size, digest, "sha1");
	}

	protected URL newURL(String url) throws MalformedURLException {
		if (this.accessToken != null)
			url += "?access_token=" + this.accessToken;
		return new URL(url);
	}

	static {
		try {
			URL_BASE = new URL("https://api.github.com/repos/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
