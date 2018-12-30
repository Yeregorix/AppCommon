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

package net.smoofyuniverse.common.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ResourceModule<T> {
	public static final Pattern KEY_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	public final Class<T> type;
	private final Map<String, T> map;

	private ResourceModule(Class<T> type, Map<String, T> map) {
		this.type = type;
		this.map = map;
	}

	public boolean contains(String key) {
		return isValidKey(key) && this.map.containsKey(key);
	}

	public static boolean isValidKey(String key) {
		return KEY_PATTERN.matcher(key).find();
	}

	public Optional<T> get(String key) {
		checkKey(key);
		return Optional.ofNullable(this.map.get(key));
	}

	public static void checkKey(String key) {
		if (!isValidKey(key))
			throw new IllegalArgumentException("key");
	}

	public Map<String, T> toMap() {
		return new HashMap<>(this.map);
	}

	public int size() {
		return this.map.size();
	}

	public Builder<T> toBuilder() {
		return new Builder<>(this.type, new HashMap<>(this.map));
	}

	public static <T> Builder<T> builder(Class<T> type) {
		return new Builder<>(type, new HashMap<>());
	}

	public static class Builder<T> {
		private final Map<String, T> map;
		private final Class<T> type;

		private Builder(Class<T> type, Map<String, T> map) {
			this.map = map;
			this.type = type;
		}

		public Builder<T> reset() {
			this.map.clear();
			return this;
		}

		public Builder<T> add(String key, T value) {
			checkKey(key);
			this.map.put(key, value);
			return this;
		}

		public Builder<T> add(ResourceModule<T> module, boolean overwrite) {
			if (overwrite)
				this.map.putAll(module.map);
			else
				module.map.forEach(this.map::putIfAbsent);
			return this;
		}

		public ResourceModule<T> build() {
			return new ResourceModule<>(this.type, new HashMap<>(this.map));
		}
	}
}
