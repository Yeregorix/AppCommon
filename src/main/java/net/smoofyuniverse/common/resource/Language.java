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

import net.smoofyuniverse.common.app.App;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Language {
	public static final Pattern ID_PATTERN = Pattern.compile("^[a-z_]+$");
	private static final Map<String, Language> map = new HashMap<>();

	public final String id;
	public final Locale locale;

	private Language(String id) {
		this.id = id;
		this.locale = new Locale(id);
	}

	public Optional<String> getName() {
		return App.get().getResourceManager().getPack(this).flatMap(p -> p.getModule(String.class)).flatMap(m -> m.get("lang_name"));
	}

	public static Language of(String id) {
		checkId(id);
		Language lang = map.get(id);
		if (lang == null) {
			lang = new Language(id);
			map.put(id, lang);
		}
		return lang;
	}

	public static void checkId(String id) {
		if (!isValidId(id))
			throw new IllegalArgumentException("id");
	}

	public static boolean isValidId(String key) {
		return ID_PATTERN.matcher(key).find();
	}
}
