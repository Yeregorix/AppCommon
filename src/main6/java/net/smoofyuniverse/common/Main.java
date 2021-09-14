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

package net.smoofyuniverse.common;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

public class Main {
	public static String ERROR_MESSAGE_TITLE, ERROR_MESSAGE, FORMATTED_ERROR_MESSAGE;

	private static Instrumentation instrumentation;

	public static void agentmain(String args, Instrumentation inst) {
		instrumentation = inst;
	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}

	public static void main(String[] args) throws Throwable {
		String[] info = getApplicationInfo();
		requireVersion(info[1]);
		launchApplication(Main.class.getClassLoader().loadClass(info[0]), args);
	}

	public static String[] getApplicationInfo() throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/application.main")));
			String main = in.readLine(), javaVersion = in.readLine();
			return new String[]{main, javaVersion == null ? "1.8.0_40" : javaVersion};
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static void requireVersion(String requiredVersion) {
		String version = getCurrentVersion();
		if (getVersionValue(version) < getVersionValue(requiredVersion)) {
			loadLanguage();
			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, createClickableMessage(String.format(FORMATTED_ERROR_MESSAGE, version, requiredVersion)), ERROR_MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(String.format(ERROR_MESSAGE, version, requiredVersion));
		}
	}

	public static void launchApplication(Class<?> main, String[] args) throws Throwable {
		System.out.println("Launching application " + main.getName() + " ...");
		try {
			main.getMethod("main", String[].class).invoke(null, new Object[]{args});
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public static String getCurrentVersion() {
		return System.getProperty("java.version");
	}

	// https://github.com/SpongePowered/SpongeCommon/blob/stable-7/src/java6/java/org/spongepowered/launch/JavaVersionCheckUtils.java
	public static double getVersionValue(String version) {
		// Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
		int i = version.indexOf('-');
		if (i != -1)
			version = version.substring(0, i);

		// Replace underscores with periods for easier String splitting
		version = version.replace('_', '.');

		double value = 0;
		double weight = 1;
		for (String part : version.split("\\.")) {
			try {
				// The value of the part of the version is related to it's proximity to the beginning
				value += Integer.parseInt(part) * weight;
			} catch (NumberFormatException ignored) {
			}

			weight /= 1000;
		}

		return value;
	}

	public static void loadLanguage() {
		if ("fr".equals(System.getProperty("user.language"))) {
			ERROR_MESSAGE_TITLE = "Version de Java invalide";

			ERROR_MESSAGE = "La version de votre environnement d'éxecution (%s) n'est pas à jour.\n"
					+ "Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.\n"
					+ "Vous pouvez la télécharger depuis le site AdoptOpenJDK:\n"
					+ "https://adoptopenjdk.net/";

			FORMATTED_ERROR_MESSAGE = "<br>La version de votre environnement d'éxecution (%s) n'est pas à jour.</br>"
					+ "<br>Pour fonctionner, l'application nécessite la version %s (ou plus) de Java.</br>"
					+ "<br>Vous pouvez la télécharger depuis <a href=\"https://adoptopenjdk.net/\">le site AdoptOpenJDK</a>.</br>";
		} else {
			ERROR_MESSAGE_TITLE = "Invalid Java version";

			ERROR_MESSAGE = "The version of your runtime environment (%s) is out of date.\n"
					+ "To run, the application requires the version %s (or more) of Java.\n"
					+ "You can download it from AdoptOpenJDK site:\n"
					+ "https://adoptopenjdk.net/";

			FORMATTED_ERROR_MESSAGE = "<br>The version of your runtime environment (%s) is out of date.</br>"
					+ "<br>To run, the application requires the version %s (or more) of Java.</br>"
					+ "<br>You can download it from <a href=\"https://adoptopenjdk.net/\">AdoptOpenJDK site</a>.</br>";
		}
	}

	public static JEditorPane createClickableMessage(String htmlBody) {
		JEditorPane pane = new JEditorPane();

		pane.setContentType("text/html");
		pane.setText("<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");

		pane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(ev.getURL().toURI());
					} catch (Exception ignored) {
					}
				}
			}
		});

		pane.setEditable(false);
		pane.setBorder(null);
		return pane;
	}

	private static String getStyle() {
		JLabel label = new JLabel();
		Font font = label.getFont();
		Color color = label.getBackground();

		return "font-family:" + font.getFamily() + ";" +
				"font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
				"font-size:" + font.getSize() + "pt;" +
				"background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
	}
}
