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

import net.smoofyuniverse.common.event.resource.LanguageSelectionChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResourceManager {
	public final boolean allowOverwrite;
	private final Map<Language, ResourcePack> map = new HashMap<>();
	private Language defaultLang, selection;
	private ResourcePack defaultPack, selectionPack;

	public ResourceManager(Language defaultLang, boolean allowOverwrite) {
		this.allowOverwrite = allowOverwrite;

		this.defaultLang = defaultLang;
		this.defaultPack = getOrCreatePack(defaultLang);

		this.selection = this.defaultLang;
		this.selectionPack = this.defaultPack;
	}

	public ResourcePack getOrCreatePack(Language lang) {
		if (lang == null)
			throw new IllegalArgumentException("lang");

		ResourcePack pack = this.map.get(lang);
		if (pack == null) {
			pack = new ResourcePack(this, lang, this.allowOverwrite);
			this.map.put(lang, pack);
		}
		return pack;
	}

	public Language getDefaultLanguage() {
		return this.defaultLang;
	}

	public ResourcePack getDefaultPack() {
		return this.defaultPack;
	}

	public Language getSelection() {
		return this.selection;
	}

	public void setSelection(Language lang) {
		if (lang == null)
			throw new IllegalArgumentException("lang");

		if (this.selection == lang)
			return;

		Language prevLang = this.selection;
		this.selection = lang;
		this.selectionPack = getOrCreatePack(lang);

		new LanguageSelectionChangeEvent(this, prevLang, lang).post();
	}

	public ResourcePack getSelectionPack() {
		return this.selectionPack;
	}

	public boolean containsPack(Language lang) {
		return lang != null && this.map.containsKey(lang);
	}

	public Optional<ResourcePack> getPack(Language lang) {
		if (lang == null)
			throw new IllegalArgumentException("lang");
		return Optional.ofNullable(this.map.get(lang));
	}

	public int size() {
		return this.map.size();
	}
}
