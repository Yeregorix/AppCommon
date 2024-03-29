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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An observable translated string.
 */
public final class ObservableTranslation extends ReadOnlyStringPropertyBase {
	/**
	 * The key of the resource.
	 */
	public final String key;

	private MessageFormat format;

	ObservableTranslation(String key) {
		this.key = key;
	}

	void update(MessageFormat format) {
		if (format.equals(this.format))
			return;
		this.format = format;
		fireValueChangedEvent();
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
		return this.format.format(null);
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
	 * @param arguments The arguments.
	 * @return The new label.
	 */
	public Label newLabel(Object... arguments) {
		Label l = new Label();
		l.textProperty().bind(format(arguments));
		return l;
	}

	/**
	 * Gets an observable value where arguments are replaced.
	 *
	 * @param arguments The arguments.
	 * @return The observable value.
	 */
	public StringExpression format(Object... arguments) {
		if (arguments.length == 0)
			return this;
		return Bindings.createStringBinding(() -> get(arguments), this);
	}

	/**
	 * Gets the value and replaces arguments.
	 *
	 * @param arguments The arguments.
	 * @return The value.
	 */
	public String get(Object... arguments) {
		return this.format.format(arguments);
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
		private final List<ObservableValue<?>> dependencies = new ArrayList<>();
		private final List<Supplier<?>> arguments = new ArrayList<>();

		private FormatBuilder(ObservableTranslation parent) {
			this.parent = parent;
			this.dependencies.add(parent);
		}

		/**
		 * Replaces the argument with a constant value.
		 *
		 * @param argument The argument.
		 * @return this.
		 */
		public FormatBuilder add(Object argument) {
			this.arguments.add(() -> argument);
			return this;
		}

		/**
		 * Replaces the argument with an observable value.
		 *
		 * @param argument The observable argument.
		 * @return this.
		 */
		public FormatBuilder add(ObservableValue<?> argument) {
			this.arguments.add(argument::getValue);
			this.dependencies.add(argument);
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
			if (this.arguments.isEmpty())
				return this.parent;

			Supplier<?>[] suppliers = this.arguments.toArray(new Supplier[0]);
			ObservableValue<?>[] deps = this.dependencies.toArray(new ObservableValue[0]);

			return Bindings.createStringBinding(() -> {
				Object[] args = new Object[suppliers.length];
				for (int i = 0; i < args.length; i++)
					args[i] = suppliers[i].get();
				return this.parent.get(args);
			}, deps);
		}
	}
}
