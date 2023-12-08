/*
 * Copyright (c) 2017-2023 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.translator;

import javafx.application.Platform;
import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.event.ListenerRegistration;
import net.smoofyuniverse.common.event.app.ApplicationLocaleChangeEvent;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A helper for managing localized resources.
 */
public abstract class Translator {
	private static final Logger logger = ApplicationLogger.get(Translator.class);

	private final Map<String, ObservableTranslation> translations = new ConcurrentHashMap<>();
	protected Locale targetLocale, formatLocale;

	public Translator() {
		this.targetLocale = ApplicationManager.get().getLocale();
		new ListenerRegistration<>(ApplicationLocaleChangeEvent.class, e -> setTargetLocale(e.newLocale()), -100).register();
	}

	/**
	 * Gets an observable translation for the given key.
	 *
	 * @param key The key.
	 * @return The observable translation.
	 */
	public final ObservableTranslation getTranslation(String key) {
		return this.translations.computeIfAbsent(key, (k) -> {
			ObservableTranslation translation = new ObservableTranslation(k);
			updateTranslation(translation);
			return translation;
		});
	}

	private void updateTranslation(ObservableTranslation translation) {
		translation.update(getFormat(translation.key));
	}

	/**
	 * Gets the resource associated with the key in the current locale.
	 * Returns the key if the resource is not found.
	 *
	 * @param key The key.
	 * @return The resource.
	 */
	public MessageFormat getFormat(String key) {
		return new MessageFormat(getString(key), this.formatLocale);
	}

	/**
	 * Gets the resource associated with the key in the current locale.
	 * Returns the key if the resource is not found.
	 *
	 * @param key The key.
	 * @return The resource.
	 */
	public String getString(String key) {
		return getObject(key).toString();
	}

	/**
	 * Gets the resource associated with the key in the current locale.
	 * Returns the key if the resource is not found.
	 *
	 * @param key The key.
	 * @return The resource.
	 */
	public abstract Object getObject(String key);

	protected abstract void loadResources();

	protected final void update() {
		loadResources();
		Platform.runLater(() -> this.translations.values().forEach(this::updateTranslation));
	}

	protected void setTargetLocale(Locale locale) {
		this.targetLocale = locale;
		update();
	}
}
