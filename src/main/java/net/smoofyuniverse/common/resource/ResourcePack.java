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

package net.smoofyuniverse.common.resource;

import net.smoofyuniverse.common.event.resource.ResourceModuleChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResourcePack {
	public final ResourceManager manager;
	public final Language language;
	public final boolean allowOverwrite;
	private final Map<Class<?>, ResourceModule<?>> modules = new HashMap<>();

	public ResourcePack(ResourceManager manager, Language lang, boolean allowOverwrite) {
		this.manager = manager;
		this.language = lang;
		this.allowOverwrite = allowOverwrite;
	}

	public <T> Optional<ResourceModule<T>> getModule(Class<T> type) {
		return Optional.ofNullable((ResourceModule<T>) this.modules.get(type));
	}

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

	public <T> void setModule(ResourceModule<T> module) {
		if (containsModule(module.type))
			checkOverwrite();
		put(module);
	}

	public boolean containsModule(Class<?> type) {
		return this.modules.containsKey(type);
	}

	private <T> void put(ResourceModule<T> module) {
		new ResourceModuleChangeEvent(this, this.modules.put(module.type, module), module).post();
	}

	public <T> void addModule(ResourceModule<T> module) {
		addModule(module, false);
	}

	public <T> void addModule(ResourceModule<T> module, boolean overwrite) {
		if (overwrite)
			checkOverwrite();

		ResourceModule<T> oldMod = (ResourceModule<T>) this.modules.get(module.type);
		put(oldMod == null ? module : oldMod.toBuilder().add(module, overwrite).build());
	}
}
