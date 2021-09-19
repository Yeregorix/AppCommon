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

import javafx.application.Platform;
import net.smoofyuniverse.common.event.ListenerRegistration;
import net.smoofyuniverse.common.event.resource.LanguageSelectionChangeEvent;
import net.smoofyuniverse.common.event.resource.ResourceModuleChangeEvent;
import net.smoofyuniverse.common.event.resource.TranslatorUpdateEvent;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.util.StringUtil;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An interpreter for string resource modules.
 */
public final class Translator {
	private static final Logger logger = ApplicationLogger.get(Translator.class);

	/**
	 * The resource manager.
	 */
	public final ResourceManager manager;

	private ResourceModule<String> defaultModule, selectionModule;

	private final Map<String, ObservableTranslation> cache = new ConcurrentHashMap<>();
	private final List<ObservableTranslation> translations = new ArrayList<>();

	Translator(ResourceManager manager) {
		this.manager = manager;
	}

	/**
	 * Binds all empty translations in static fields of the class.
	 *
	 * @param target The class.
	 * @throws IllegalAccessException If any reflection error occurs.
	 */
	public void bindStaticFields(Class<?> target) throws IllegalAccessException {
		if (target == null)
			throw new IllegalArgumentException("target");

		for (Field f : target.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && ObservableTranslation.class.isAssignableFrom(f.getType()) && ResourceModule.isValidKey(f.getName())) {
				f.setAccessible(true);
				ObservableTranslation translation = (ObservableTranslation) f.get(null);
				if (translation.getKey().isEmpty())
					_bindTranslation(translation, f.getName());
			}
		}
	}

	private void _bindTranslation(ObservableTranslation translation, String key) {
		translation.setKey(this, key);
		this.cache.putIfAbsent(key, translation);

		synchronized (this.translations) {
			this.translations.add(translation);
		}
	}

	/**
	 * Binds an empty translation to the given key.
	 *
	 * @param translation The empty translation.
	 * @param key         The key.
	 * @throws IllegalArgumentException If the translation is not empty.
	 */
	public void bindTranslation(ObservableTranslation translation, String key) {
		ResourceModule.checkKey(key);
		if (!translation.getKey().isEmpty())
			throw new IllegalArgumentException("translation is not empty");

		_bindTranslation(translation, key);
	}

	/**
	 * Gets an observable translation for the given key.
	 *
	 * @param key The key.
	 * @return The observable translation.
	 */
	public ObservableTranslation getTranslation(String key) {
		ResourceModule.checkKey(key);
		return this.cache.computeIfAbsent(key, this::createTranslation);
	}

	private ObservableTranslation createTranslation(String key) {
		ObservableTranslation translation = new ObservableTranslation();
		translation.setKey(this, key);

		synchronized (this.translations) {
			this.translations.add(translation);
		}
		return translation;
	}

	/**
	 * Translates the key.
	 * Gets the resource associated with the key in the selected language,
	 * then if not found in the default language,
	 * then if not found, returns the key.
	 *
	 * @param key The key.
	 * @return The translation.
	 */
	public String translate(String key) {
		ResourceModule.checkKey(key);
		return _translate(key);
	}

	String _translate(String key) {
		if (this.selectionModule != null) {
			Optional<String> value = this.selectionModule.get(key);
			if (value.isPresent())
				return value.get();
		}

		if (this.defaultModule != null) {
			Optional<String> value = this.defaultModule.get(key);
			if (value.isPresent())
				return value.get();
		}

		return key;
	}

	/**
	 * Translates the key and replaces the parameters.
	 * See {@link Translator#translate(String)} and {@link StringUtil#replaceParameters(String, String...)}.
	 *
	 * @param key        The key.
	 * @param parameters The parameters.
	 * @return The translation.
	 */
	public String translate(String key, String... parameters) {
		ResourceModule.checkKey(key);

		if (this.selectionModule != null) {
			Optional<String> value = this.selectionModule.get(key);
			if (value.isPresent())
				return StringUtil.replaceParameters(value.get(), parameters);
		}

		if (this.defaultModule != null) {
			Optional<String> value = this.defaultModule.get(key);
			if (value.isPresent())
				return StringUtil.replaceParameters(value.get(), parameters);
		}

		return key;
	}

	private void update() {
		this.defaultModule = this.manager.getDefaultPack().getModule(String.class).orElse(null);
		this.selectionModule = this.manager.getSelectionPack().getModule(String.class).orElse(null);

		if (this.defaultModule == this.selectionModule)
			this.defaultModule = null;

		Platform.runLater(() -> {
			synchronized (this.translations) {
				for (ObservableTranslation t : this.translations)
					t.update(this);
			}

			new TranslatorUpdateEvent(this).post();
		});
	}

	/**
	 * Saves the string resource module to the file.
	 *
	 * @param module The module.
	 * @param file   The file.
	 * @throws IOException If any I/O error occurs.
	 */
	public static void save(ResourceModule<String> module, Path file) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			save(module, writer);
		}
	}

	/**
	 * Saves the string resource module to the writer.
	 *
	 * @param module The module.
	 * @param writer The writer.
	 * @throws IOException If any I/O error occurs.
	 */
	public static void save(ResourceModule<String> module, BufferedWriter writer) throws IOException {
		for (Entry<String, String> e : module.toMap().entrySet()) {
			writer.write(e.getKey());
			writer.write('=');
			writer.write(e.getValue());
			writer.newLine();
		}
	}

	/**
	 * Loads a string resource module from the file.
	 *
	 * @param file The file.
	 * @return The new string resource module.
	 * @throws IOException If any I/O error occurs.
	 */
	public static ResourceModule<String> load(Path file) throws IOException {
		ResourceModule.Builder<String> builder = ResourceModule.builder(String.class);
		load(builder, file);
		return builder.build();
	}

	/**
	 * Loads a string resource module from the file.
	 *
	 * @param builder The module builder.
	 * @param file    The file.
	 * @throws IOException If any I/O error occurs.
	 */
	public static void load(ResourceModule.Builder<String> builder, Path file) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			load(builder, reader);
		}
	}

	/**
	 * Loads a string resource module from the reader.
	 *
	 * @param builder The module builder.
	 * @param reader  The reader.
	 * @throws IOException If any I/O error occurs.
	 */
	public static void load(ResourceModule.Builder<String> builder, BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#')
				continue;

			int i = line.indexOf('=');
			if (i == -1)
				throw new IllegalArgumentException("No '=' separator was found");

			builder.add(line.substring(0, i), StringUtil.unescape(line.substring(i + 1)));
		}
	}

	public static ResourceModule<String> load(BufferedReader reader) throws IOException {
		ResourceModule.Builder<String> builder = ResourceModule.builder(String.class);
		load(builder, reader);
		return builder.build();
	}

	public static Map<Language, ResourceModule<String>> loadAll(Path dir, String extension) {
		if (!extension.isEmpty() && extension.charAt(0) != '.')
			extension = '.' + extension;

		Map<Language, ResourceModule<String>> map = new HashMap<>();
		ResourceModule.Builder<String> builder = ResourceModule.builder(String.class);

		try (DirectoryStream<Path> st = Files.newDirectoryStream(dir)) {
			for (Path p : st) {
				String fn = p.getFileName().toString();
				if (fn.endsWith(extension)) {
					String id = fn.substring(0, fn.length() - extension.length());
					if (Language.isValidId(id)) {
						try {
							load(builder, p);
							map.put(Language.of(id), builder.build());
						} catch (Exception e) {
							logger.error("Failed to load lang file " + fn, e);
						}
						builder.reset();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Can't list lang files in directory " + dir, e);
		}

		return map;
	}

	static {
		new ListenerRegistration<>(LanguageSelectionChangeEvent.class, e -> e.manager.translator.update(), -100).register();

		new ListenerRegistration<>(ResourceModuleChangeEvent.class, e -> {
			Translator t = e.pack.manager.translator;
			if (e.prevModule == t.defaultModule || e.prevModule == t.selectionModule)
				t.update();
		}, -100).register();
	}
}
