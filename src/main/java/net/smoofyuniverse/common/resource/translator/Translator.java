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

package net.smoofyuniverse.common.resource.translator;

import javafx.application.Platform;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.event.core.ListenerRegistration;
import net.smoofyuniverse.common.event.resource.LanguageSelectionChangeEvent;
import net.smoofyuniverse.common.event.resource.ResourceModuleChangeEvent;
import net.smoofyuniverse.common.event.resource.TranslatorUpdateEvent;
import net.smoofyuniverse.common.resource.Language;
import net.smoofyuniverse.common.resource.ResourceManager;
import net.smoofyuniverse.common.resource.ResourceModule;
import net.smoofyuniverse.common.util.ReflectionUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Translator {
	private static final Logger logger = App.getLogger("Translator");
	private static final Map<ResourceManager, Translator> translators = new HashMap<>();

	public final ResourceManager manager;
	private ResourceModule<String> defaultModule, selectionModule;
	private final Map<String, ObservableTranslation> translations = new ConcurrentHashMap<>();

	private Translator(ResourceManager manager) {
		this.manager = manager;
	}

	public void fill(Class<?> receiver) throws IllegalAccessException {
		if (receiver == null)
			throw new IllegalArgumentException("receiver");

		for (Field f : receiver.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && ObservableTranslation.class.isAssignableFrom(f.getType()) && ResourceModule.isValidKey(f.getName())) {
				f.setAccessible(true);
				ReflectionUtil.removeFinal(f);
				f.set(null, observableTranslation(f.getName()));
			}
		}
	}

	public ObservableTranslation observableTranslation(String key) {
		ResourceModule.checkKey(key);
		ObservableTranslation t = this.translations.get(key);
		if (t == null) {
			t = new ObservableTranslation(this, key);
			this.translations.put(key, t);
		}
		return t;
	}

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

	public static Translator of(ResourceManager manager) {
		Translator t = translators.get(manager);
		if (t == null) {
			t = new Translator(manager);
			t.update();
			translators.put(manager, t);
		}
		return t;
	}

	private void update() {
		this.defaultModule = this.manager.getDefaultPack().getModule(String.class).orElse(null);
		this.selectionModule = this.manager.getSelectionPack().getModule(String.class).orElse(null);

		if (this.defaultModule == this.selectionModule)
			this.defaultModule = null;

		Platform.runLater(() -> {
			for (ObservableTranslation t : this.translations.values())
				t.update();

			new TranslatorUpdateEvent(this).post();
		});
	}

	public static void save(ResourceModule<String> module, Path file) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			save(module, writer);
		}
	}

	public static void save(ResourceModule<String> module, BufferedWriter writer) throws IOException {
		for (Entry<String, String> e : module.toMap().entrySet()) {
			writer.write(e.getKey());
			writer.write('=');
			writer.write(e.getValue());
			writer.newLine();
		}
	}

	public static ResourceModule<String> load(Path file) throws IOException {
		ResourceModule.Builder<String> builder = ResourceModule.builder(String.class);
		load(builder, file);
		return builder.build();
	}

	public static void load(ResourceModule.Builder<String> builder, Path file) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			load(builder, reader);
		}
	}

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
		new ListenerRegistration<>(LanguageSelectionChangeEvent.class, e -> {
			Translator t = translators.get(e.manager);
			if (t != null)
				t.update();
		}, Order.EARLY).register();

		new ListenerRegistration<>(ResourceModuleChangeEvent.class, e -> {
			Translator t = translators.get(e.pack.manager);
			if (t != null) {
				if (e.prevModule == t.defaultModule || e.prevModule == t.selectionModule)
					t.update();
			}
		}, Order.EARLY).register();
	}
}
