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

package net.smoofyuniverse.common.event.resource;

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.resource.Language;
import net.smoofyuniverse.common.resource.ResourceManager;

public class LanguageSelectionChangeEvent implements Event {
	public final ResourceManager manager;
	public final Language prevLang, newLang;

	public LanguageSelectionChangeEvent(ResourceManager manager, Language prevLang, Language newLang) {
		if (manager == null)
			throw new IllegalArgumentException("manager");
		if (prevLang == null)
			throw new IllegalArgumentException("prevLang");
		if (newLang == null)
			throw new IllegalArgumentException("newLang");
		if (prevLang == newLang)
			throw new IllegalArgumentException("No change");

		this.manager = manager;
		this.prevLang = prevLang;
		this.newLang = newLang;
	}
}
