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

package net.smoofyuniverse.common.resource;

import net.smoofyuniverse.common.event.resource.ResourceModuleChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A pack of resource modules for a given language.
 */
public class ResourcePack {
	/**
	 * The resource manager.
	 */
	public final ResourceManager manager;

	/**
	 * The language.
	 */
	public final Language language;

	/**
	 * Whether existing resources can be overwritten.
	 */
	public final boolean allowOverwrite;

	private final Map<Class<?>, ResourceModule<?>> modules = new HashMap<>();

	/**
	 * Creates a new pack.
	 *
	 * @param manager        The resource manager.
	 * @param lang           The language.
	 * @param allowOverwrite Whether existing resources can be overwritten.
	 */
	public ResourcePack(ResourceManager manager, Language lang, boolean allowOverwrite) {
		this.manager = manager;
		this.language = lang;
		this.allowOverwrite = allowOverwrite;
	}

	/**
	 * Gets the module of the given type.
	 *
	 * @param type The type of resources.
	 * @param <T>  The type of resources.
	 * @return The module.
	 */
	public <T> Optional<ResourceModule<T>> getModule(Class<T> type) {
		return Optional.ofNullable((ResourceModule<T>) this.modules.get(type));
	}

	/**
	 * Removes the module of the given type.
	 *
	 * @param type The type of resources.
	 * @return Whether a module of the given type was present.
	 * @throws UnsupportedOperationException If overwriting is not allowed.
	 */
	public boolean removeModule(Class<?> type) {
		checkOverwrite();
		return remove(type);
	}

	private void checkOverwrite() {
		if (!this.allowOverwrite)
			throw new UnsupportedOperationException("Overwrite not allowed");
	}

	private boolean remove(Class<?> type) {
		ResourceModule<?> mod = this.modules.remove(type);
		if (mod != null) {
			new ResourceModuleChangeEvent(this, mod, null).post();
			return true;
		}
		return false;
	}

	/**
	 * Adds the module.
	 * If a module of the same type already exists, it will be overwritten.
	 *
	 * @param module The module.
	 * @throws UnsupportedOperationException If a module already exists and overwriting is not allowed.
	 */
	public <T> void setModule(ResourceModule<T> module) {
		if (containsModule(module.type))
			checkOverwrite();
		put(module);
	}

	/**
	 * Gets whether a module of the given type exists.
	 *
	 * @param type The type of resources.
	 * @return Whether a module of the given type exists.
	 */
	public boolean containsModule(Class<?> type) {
		return this.modules.containsKey(type);
	}

	private <T> void put(ResourceModule<T> module) {
		new ResourceModuleChangeEvent(this, this.modules.put(module.type, module), module).post();
	}

	/**
	 * Adds the module.
	 * If a module of the same type already exists, resources are merged in a new module.
	 * Existing resources are preserved.
	 *
	 * @param module The module.
	 * @param <T>    The type of resources.
	 */
	public <T> void addModule(ResourceModule<T> module) {
		addModule(module, false);
	}

	/**
	 * Adds the module.
	 * If a module of the same type already exists, resources are merged in a new module.
	 *
	 * @param module    The module.
	 * @param overwrite Whether resources from the new module can replace existing resources from the previous module.
	 * @param <T>       The type of resources.
	 */
	public <T> void addModule(ResourceModule<T> module, boolean overwrite) {
		if (overwrite)
			checkOverwrite();

		ResourceModule<T> oldMod = (ResourceModule<T>) this.modules.get(module.type);
		put(oldMod == null ? module : oldMod.toBuilder().add(module, overwrite).build());
	}
}
