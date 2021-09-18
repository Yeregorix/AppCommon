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

import net.smoofyuniverse.common.app.ApplicationManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A language.
 */
public final class Language {
	/**
	 * A identifier must matches this pattern to be valid.
	 */
	public static final Pattern ID_PATTERN = Pattern.compile("^[a-z_]+$");

	private static final Map<String, Language> map = new HashMap<>();

	/**
	 * The identifier.
	 */
	public final String id;

	/**
	 * The locale.
	 */
	public final Locale locale;

	private Language(String id) {
		this.id = id;
		this.locale = new Locale(id);
	}

	/**
	 * The name of this language, in this language.
	 *
	 * @return The name.
	 */
	public Optional<String> getName() {
		return ApplicationManager.get().getResourceManager().getPack(this).flatMap(p -> p.getModule(String.class)).flatMap(m -> m.get("lang_name"));
	}

	/**
	 * Gets the language for the given identifier.
	 *
	 * @param id The identifier.
	 * @return The language.
	 */
	public static Language of(String id) {
		checkId(id);
		Language lang = map.get(id);
		if (lang == null) {
			lang = new Language(id);
			map.put(id, lang);
		}
		return lang;
	}

	/**
	 * Checks whether the identifier is valid.
	 *
	 * @param id The identifier.
	 * @throws IllegalArgumentException If the identifier is not valid.
	 */
	public static void checkId(String id) {
		if (!isValidId(id))
			throw new IllegalArgumentException("id");
	}

	/**
	 * Gets whether the identifier is valid.
	 *
	 * @param id The identifier.
	 * @return Whether the identifier is valid.
	 */
	public static boolean isValidId(String id) {
		return ID_PATTERN.matcher(id).matches();
	}
}
