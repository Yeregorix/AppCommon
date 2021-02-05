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

package net.smoofyuniverse.common.task;

import javafx.beans.value.ObservableValue;

import java.util.Optional;

/**
 * A {@link BaseListener} with a title and a message;
 */
public interface BaseTask extends BaseListener {

	/**
	 * Gets the title.
	 *
	 * @return The title.
	 */
	Optional<String> getTitle();

	/**
	 * Sets the title.
	 *
	 * @param value The title.
	 */
	void setTitle(String value);

	/**
	 * Sets the title.
	 * If possible, binds the value.
	 *
	 * @param value The title.
	 */
	default void setTitle(ObservableValue<String> value) {
		setTitle(value.getValue());
	}

	/**
	 * Gets the message.
	 *
	 * @return The message.
	 */
	Optional<String> getMessage();

	/**
	 * Sets the message.
	 *
	 * @param value The message.
	 */
	void setMessage(String value);

	/**
	 * Sets the message.
	 * If possible, binds the value.
	 *
	 * @param value The message.
	 */
	default void setMessage(ObservableValue<String> value) {
		setTitle(value.getValue());
	}
}
