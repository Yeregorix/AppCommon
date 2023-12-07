/*
 * Copyright (c) 2023 Hugo Dupanloup (Yeregorix)
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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * A translator based on a {@link ResourceBundle}.
 */
public class ResourceBundleTranslator extends Translator {
    private final Function<Locale, ResourceBundle> loader;
    private ResourceBundle bundle;

    public ResourceBundleTranslator(String path) {
        this((locale) -> ResourceBundle.getBundle(path, locale));
    }

    public ResourceBundleTranslator(Function<Locale, ResourceBundle> loader) {
        this.loader = loader;
        loadResources();
    }

    @Override
    public Object getObject(String key) {
        try {
            return this.bundle.getObject(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    @Override
    protected void loadResources() {
        this.bundle = this.loader.apply(this.targetLocale);
        this.formatLocale = this.bundle.getLocale();
        if (this.formatLocale.equals(Locale.ROOT)) {
            this.formatLocale = Locale.ENGLISH;
        }
    }
}
