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

package net.smoofyuniverse.common.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A collection of resources indexed by string keys.
 *
 * @param <T> The type of resources.
 */
public final class ResourceModule<T> {
	/**
	 * A key must matches this pattern to be valid.
	 */
	public static final Pattern KEY_PATTERN = Pattern.compile("^[a-z0-9_]+$");

	/**
	 * The type of resources.
	 */
	public final Class<T> type;

	private final Map<String, T> map;

	private ResourceModule(Class<T> type, Map<String, T> map) {
		this.type = type;
		this.map = map;
	}

	/**
	 * Gets whether this module contains a resource indexed by the key.
	 *
	 * @param key The key.
	 * @return Whether this module contains a resource indexed by the key.
	 */
	public boolean contains(String key) {
		return this.map.containsKey(key);
	}

	/**
	 * Gets the resource indexed by the key.
	 *
	 * @param key The key.
	 * @return The resource indexed by the key.
	 */
	public Optional<T> get(String key) {
		checkKey(key);
		return Optional.ofNullable(this.map.get(key));
	}

	/**
	 * Checks whether the key is valid.
	 *
	 * @param key The key.
	 * @throws IllegalArgumentException If the key is not valid.
	 */
	public static void checkKey(String key) {
		if (!isValidKey(key))
			throw new IllegalArgumentException("key");
	}

	/**
	 * Gets whether the key is valid.
	 *
	 * @param key The key.
	 * @return Whether the key is valid.
	 */
	public static boolean isValidKey(String key) {
		return KEY_PATTERN.matcher(key).matches();
	}

	/**
	 * Gets the number of resources.
	 *
	 * @return The number of resources.
	 */
	public int size() {
		return this.map.size();
	}

	/**
	 * Copies all resources in a new builder.
	 *
	 * @return The new builder.
	 */
	public Builder<T> toBuilder() {
		return new Builder<>(this.type, toMap());
	}

	/**
	 * Creates a map of keys and resources.
	 *
	 * @return The map of keys and resources.
	 */
	public Map<String, T> toMap() {
		return new HashMap<>(this.map);
	}

	/**
	 * Creates a new builder.
	 *
	 * @param type The type of resources.
	 * @param <T>  The type of resources.
	 * @return The new builder.
	 */
	public static <T> Builder<T> builder(Class<T> type) {
		return new Builder<>(type, new HashMap<>());
	}

	/**
	 * A builder for {@link ResourceModule}.
	 *
	 * @param <T> The type of resources.
	 */
	public static class Builder<T> {
		private final Map<String, T> map;
		private final Class<T> type;

		private Builder(Class<T> type, Map<String, T> map) {
			this.map = map;
			this.type = type;
		}

		/**
		 * Clears all resources.
		 *
		 * @return this.
		 */
		public Builder<T> reset() {
			this.map.clear();
			return this;
		}

		/**
		 * Adds the resource.
		 *
		 * @param key   The key.
		 * @param value The resource.
		 * @return this.
		 */
		public Builder<T> add(String key, T value) {
			checkKey(key);
			this.map.put(key, value);
			return this;
		}

		/**
		 * Adds all resources from the module.
		 *
		 * @param module    The module.
		 * @param overwrite Whether resources from the module can replace existing resources in the builder.
		 * @return this.
		 */
		public Builder<T> add(ResourceModule<T> module, boolean overwrite) {
			if (overwrite)
				this.map.putAll(module.map);
			else
				module.map.forEach(this.map::putIfAbsent);
			return this;
		}

		/**
		 * Builds a new module from this builder.
		 *
		 * @return The new module.
		 */
		public ResourceModule<T> build() {
			return new ResourceModule<>(this.type, new HashMap<>(this.map));
		}
	}
}
