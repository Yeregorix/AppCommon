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

package net.smoofyuniverse.common.environment;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import net.smoofyuniverse.common.download.FileInfo;
import net.smoofyuniverse.common.platform.Architecture;
import net.smoofyuniverse.common.platform.OperatingSystem;
import net.smoofyuniverse.common.util.ArrayUtil;
import net.smoofyuniverse.common.util.URLUtil;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

/**
 * Information about a dependency.
 */
public class DependencyInfo extends FileInfo {
	/**
	 * The name of this dependency.
	 */
	public final String name;

	/**
	 * The operating systems compatible with this dependency.
	 */
	public final OperatingSystem[] systems;

	/**
	 * The processor architectures compatible with this dependency.
	 */
	public final Architecture[] archs;

	/**
	 * Creates a new dependency.
	 *
	 * @param name            The name.
	 * @param url             The URL.
	 * @param size            The size.
	 * @param digest          The hexadecimal representation of the digest.
	 * @param digestAlgorithm The algorithm used to compute the digest.
	 * @param systems         The systems.
	 * @param archs           The architectures.
	 */
	public DependencyInfo(String name, URL url, long size,
						  String digest, String digestAlgorithm,
						  OperatingSystem[] systems, Architecture[] archs) {
		super(url, size, digest, digestAlgorithm);
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("name");
		if (systems == null)
			throw new IllegalArgumentException("systems");
		if (archs == null)
			throw new IllegalArgumentException("archs");

		this.name = name;
		this.systems = systems;
		this.archs = archs;
	}

	/**
	 * Gets whether this dependency is compatible with the current system and architecture.
	 *
	 * @return Whether this dependency is compatible.
	 */
	public boolean isCompatible() {
		return ArrayUtil.contains(this.systems, OperatingSystem.CURRENT) && ArrayUtil.contains(this.archs, Architecture.CURRENT);
	}

	/**
	 * Loads all {@link DependencyInfo}s from a json file to a collection.
	 *
	 * @param file The file.
	 * @param col  The collection.
	 * @throws Exception if any exception occurs while loading file.
	 */
	public static void loadAll(Path file, Collection<DependencyInfo> col) throws Exception {
		if (!Files.exists(file))
			return;

		try (BufferedReader r = Files.newBufferedReader(file)) {
			loadAll(JsonParser.array().from(r), col);
		}
	}

	/**
	 * Loads all {@link DependencyInfo}s from a json array to a collection.
	 *
	 * @param array The json array.
	 * @param col   The collection.
	 */
	public static void loadAll(JsonArray array, Collection<DependencyInfo> col) {
		for (Object obj : array)
			col.add(load((JsonObject) obj));
	}

	/**
	 * Loads a {@link DependencyInfo} from a json object.
	 *
	 * @param obj The json object.
	 * @return The dependency.
	 */
	public static DependencyInfo load(JsonObject obj) {
		JsonArray systemsArray = obj.getArray("systems");
		OperatingSystem[] systems;
		if (systemsArray == null) {
			systems = OperatingSystem.values();
		} else {
			systems = new OperatingSystem[systemsArray.size()];
			for (int i = 0; i < systems.length; i++)
				systems[i] = OperatingSystem.valueOf(systemsArray.getString(i).toUpperCase(Locale.ROOT));
		}

		JsonArray archsArray = obj.getArray("archs");
		Architecture[] archs;
		if (archsArray == null) {
			archs = Architecture.values();
		} else {
			archs = new Architecture[archsArray.size()];
			for (int i = 0; i < archs.length; i++)
				archs[i] = Architecture.valueOf(archsArray.getString(i).toUpperCase(Locale.ROOT));
		}

		return new DependencyInfo(obj.getString("name"),
				URLUtil.newURL(obj.getString("url")), obj.getLong("size", -1),
				obj.getString("digest"), obj.getString("digest-algorithm", "sha1"),
				systems, archs);
	}
}
