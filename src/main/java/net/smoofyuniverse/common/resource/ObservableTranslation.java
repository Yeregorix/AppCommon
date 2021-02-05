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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import net.smoofyuniverse.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An observable translated string.
 */
public final class ObservableTranslation extends ReadOnlyStringPropertyBase {
	private String key, value;

	ObservableTranslation() {
		this.key = "";
		this.value = "";
	}

	void setKey(Translator translator, String key) {
		this.key = key;
		this.value = translator._translate(key);
	}

	/**
	 * Gets the key of the resource.
	 *
	 * @return The key.
	 */
	public String getKey() {
		return this.key;
	}

	void update(Translator translator) {
		String newValue = translator._translate(this.key);
		if (this.value.equals(newValue))
			return;

		this.value = newValue;
		fireValueChangedEvent();
	}

	/**
	 * Creates an empty translation.
	 * This translation currently have an empty key and an empty value.
	 * The key can be late-initiliazed
	 *
	 * @return The new translation.
	 */
	public static ObservableTranslation empty() {
		return new ObservableTranslation();
	}

	@Override
	public Object getBean() {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String get() {
		return this.value;
	}

	/**
	 * Creates a new label with text bound to this translation.
	 *
	 * @return The new label.
	 */
	public Label newLabel() {
		Label l = new Label();
		l.textProperty().bind(this);
		return l;
	}

	/**
	 * Creates a new label with text bound to this translation.
	 *
	 * @param parameters The parameters.
	 * @return The new label.
	 */
	public Label newLabel(String... parameters) {
		Label l = new Label();
		l.textProperty().bind(format(parameters));
		return l;
	}

	/**
	 * Gets an observable value where parameters are replaced.
	 *
	 * @param parameters The parameters.
	 * @return The observable value.
	 */
	public StringExpression format(String... parameters) {
		if (parameters.length == 0)
			return this;
		return Bindings.createStringBinding(() -> get(parameters), this);
	}

	/**
	 * Gets the value and replaces parameters.
	 *
	 * @param parameters The parameters.
	 * @return The value.
	 */
	public String get(String... parameters) {
		return StringUtil.replaceParameters(this.value, parameters);
	}

	/**
	 * Creates a new format builder.
	 *
	 * @return The new builder.
	 */
	public FormatBuilder formatBuilder() {
		return new FormatBuilder(this);
	}

	/**
	 * A builder for an observable formatted translation.
	 */
	public static class FormatBuilder {
		private final ObservableTranslation parent;
		private final List<ObservableValue<String>> dependencies = new ArrayList<>();
		private final Map<String, Supplier<String>> map = new HashMap<>();

		private FormatBuilder(ObservableTranslation parent) {
			this.parent = parent;
			this.dependencies.add(parent);
		}

		/**
		 * Replaces the parameter with a constant value.
		 *
		 * @param name The name of the parameter.
		 * @param replacement The replacement.
		 * @return this.
		 */
		public FormatBuilder add(String name, String replacement) {
			this.map.put(name, () -> replacement);
			return this;
		}

		/**
		 * Replaces the parameter with an observable value.
		 *
		 * @param name The name of the parameter.
		 * @param replacement The observable replacement.
		 * @return this.
		 */
		public FormatBuilder add(String name, ObservableValue<String> replacement) {
			this.map.put(name, replacement::getValue);
			this.dependencies.add(replacement);
			return this;
		}

		/**
		 * Builds and creates a new label with text bound to the result.
		 *
		 * @return The new label.
		 */
		public Label newLabel() {
			Label l = new Label();
			l.textProperty().bind(build());
			return l;
		}

		/**
		 * Builds an observable formatted translation from this builder.
		 *
		 * @return The observable translation.
		 */
		public StringExpression build() {
			if (this.map.isEmpty())
				return this.parent;
			return Bindings.createStringBinding(() -> StringUtil.replaceParameters(this.parent.value, s -> map.get(s).get()), this.dependencies.toArray(new ObservableValue[0]));
		}
	}
}
