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

package net.smoofyuniverse.common.fx.value;


import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Immutable implementation of {@link ObservableValue}.
 *
 * @param <T> The type of the wrapped value.
 */
public class ImmutableValue<T> implements ObservableValue<T> {
	private final T value;

	/**
	 * Wraps the value in a {@link ImmutableValue}.
	 *
	 * @param value The value.
	 */
	public ImmutableValue(T value) {
		this.value = value;
	}

	@Override
	public void addListener(ChangeListener<? super T> listener) {}

	@Override
	public void removeListener(ChangeListener<? super T> listener) {}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public void addListener(InvalidationListener listener) {}

	@Override
	public void removeListener(InvalidationListener listener) {}
}
