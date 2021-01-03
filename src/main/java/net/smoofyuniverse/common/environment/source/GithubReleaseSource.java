/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.environment.ReleaseInfo;
import net.smoofyuniverse.common.util.URLUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.Optional;

/**
 * An implemention of {@link ReleaseSource} based on a Github repository.
 */
public class GithubReleaseSource implements ReleaseSource {
	private static final Logger logger = Logger.get("GithubReleaseSource");

	/**
	 * Github API base URL.
	 */
	public static final URL URL_BASE = URLUtil.newURL("https://api.github.com/repos/");

	protected final String owner, repo, accessToken, appName;
	protected final ConnectionConfig config;

	/**
	 * Creates a new Github release source.
	 *
	 * @param owner       The owner.
	 * @param repo        The repository.
	 * @param accessToken The access token.
	 * @param appName     The application name.
	 * @param config      The connection configuration.
	 */
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
		URLConnection co = this.config.openConnection(url);
		configureToken(co);

		try (InputStream in = co.getInputStream()) {
			return getRelease(JsonParser.object().withLazyNumbers().from(in));
		}
	}

	protected void configureToken(URLConnection co) throws IOException {
		if (this.accessToken != null)
			co.setRequestProperty("Authorization", "token " + this.accessToken);
	}

	protected URL getURL(String path) throws MalformedURLException {
		return URLUtil.appendSuffix(URL_BASE, this.owner + "/" + this.repo + "/" + path);
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

		URL url = new URL(jarAsset.getString("url"));
		long size = jarAsset.getNumber("size").longValue();

		JsonObject extraData = null;
		String digest = null;

		if (jsonAsset != null) {
			HttpURLConnection co = openAssetConnection(new URL(jsonAsset.getString("url")), this.config);

			try (InputStream in = co.getInputStream()) {
				JsonObject data = JsonParser.object().withLazyNumbers().from(in);
				digest = data.getString("sha1");
				extraData = data.getObject("extra");
			} finally {
				co.disconnect();
			}
		}

		return new ReleaseInfo(version, date, extraData, url, size, digest, "sha1") {
			@Override
			public HttpURLConnection openDownloadConnection(ConnectionConfig config) throws IOException {
				return openAssetConnection(this.url, config);
			}
		};
	}

	private HttpURLConnection openAssetConnection(URL url, ConnectionConfig config) throws IOException {
		HttpURLConnection co = config.openHttpConnection(url);
		co.setInstanceFollowRedirects(false);
		co.setRequestProperty("Accept", "application/octet-stream");
		configureToken(co);

		co.connect();
		if (co.getResponseCode() / 100 == 3) {
			String loc = co.getHeaderField("Location");
			if (loc != null) {
				co.disconnect();
				co = config.openHttpConnection(new URL(loc));
				co.setRequestProperty("Accept", "application/octet-stream");
			}
		}

		return co;
	}
}
