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

import net.smoofyuniverse.common.event.resource.LanguageSelectionChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A resource manager.
 */
public class ResourceManager {

	/**
	 * Whether existing resources can be overwritten.
	 */
	public final boolean allowOverwrite;

	/**
	 * The translator associated with this manager.
	 */
	public final Translator translator = new Translator(this);

	private final Map<Language, ResourcePack> map = new HashMap<>();
	private final Language defaultLang;
	private final ResourcePack defaultPack;
	private Language selection;
	private ResourcePack selectionPack;

	public ResourceManager(Language defaultLang, boolean allowOverwrite) {
		this.allowOverwrite = allowOverwrite;

		this.defaultLang = defaultLang;
		this.defaultPack = getOrCreatePack(defaultLang);

		this.selection = this.defaultLang;
		this.selectionPack = this.defaultPack;
	}

	/**
	 * Gets the default language.
	 *
	 * @return The default language.
	 */
	public Language getDefaultLanguage() {
		return this.defaultLang;
	}

	/**
	 * Gets the pack associated with the default language.
	 *
	 * @return The default pack.
	 */
	public ResourcePack getDefaultPack() {
		return this.defaultPack;
	}

	/**
	 * Gets the selected language.
	 *
	 * @return The selected language.
	 */
	public Language getSelection() {
		return this.selection;
	}

	/**
	 * Selects a language.
	 *
	 * @param lang The language.
	 */
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

	/**
	 * Gets the pack associated with the language.
	 * Creates if absent.
	 *
	 * @param lang The language.
	 * @return The pack.
	 */
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

	/**
	 * Gets the pack associated with the selected language.
	 *
	 * @return The selected pack.
	 */
	public ResourcePack getSelectionPack() {
		return this.selectionPack;
	}

	/**
	 * Gets the pack associated with the language.
	 *
	 * @param lang The language.
	 * @return The pack.
	 */
	public Optional<ResourcePack> getPack(Language lang) {
		if (lang == null)
			throw new IllegalArgumentException("lang");
		return Optional.ofNullable(this.map.get(lang));
	}
}
