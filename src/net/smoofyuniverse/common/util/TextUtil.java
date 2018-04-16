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

package net.smoofyuniverse.common.util;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.smoofyuniverse.common.app.OperatingSystem;

import java.net.URI;
import java.net.URISyntaxException;

public class TextUtil {

	public static TextFlow justify(double lineLength, Node... children) {
		TextFlow t = new TextFlow(children);
		t.setTextAlignment(TextAlignment.JUSTIFY);
		t.setPrefWidth(lineLength);
		return t;
	}

	public static TextFlow justify(Node... children) {
		TextFlow t = new TextFlow(children);
		t.setTextAlignment(TextAlignment.JUSTIFY);
		return t;
	}

	public static TextFlow join(double lineLength, Node... children) {
		TextFlow t = new TextFlow(children);
		t.setPrefWidth(lineLength);
		return t;
	}

	public static TextFlow join(Node... children) {
		return new TextFlow(children);
	}

	public static Hyperlink openLink(String link) {
		return openLink(link, link);
	}

	public static Hyperlink openLink(String text, String link) {
		try {
			return openLink(text, new URI(link));
		} catch (URISyntaxException e) {
			return new Hyperlink(text);
		}
	}

	public static Hyperlink openLink(String text, URI link) {
		Hyperlink l = new Hyperlink(text);
		l.setOnAction((e) -> OperatingSystem.CURRENT.openLink(link));
		return l;
	}
}
