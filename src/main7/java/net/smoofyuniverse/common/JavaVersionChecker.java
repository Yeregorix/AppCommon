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

package net.smoofyuniverse.common;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class JavaVersionChecker {
    private static final String JAVA_DOWNLOAD_LINK = "https://adoptium.net/temurin/releases/?package=jre";

    public static void requireVersion(int requiredVersion) {
        String version = System.getProperty("java.version");
        if (getMajorVersion(version) < requiredVersion) {
            ResourceBundle bundle = ResourceBundle.getBundle("lang/common-main");
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                        createClickableMessage(MessageFormat.format(bundle.getString("error.message.html"), version, requiredVersion, JAVA_DOWNLOAD_LINK)),
                        bundle.getString("error.title"), JOptionPane.ERROR_MESSAGE);
            }
            throw new RuntimeException(MessageFormat.format(bundle.getString("error.message"), version, requiredVersion, JAVA_DOWNLOAD_LINK));
        }
    }

    // https://github.com/SpongePowered/Sponge/blob/api-10/vanilla/src/installer/java8/org/spongepowered/vanilla/installer/JavaVersionCheckUtils.java
    private static int getMajorVersion(String version) {
        // Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        // Replace underscores with periods for easier String splitting
        version = version.replace('_', '.');
        // Split the version up into parts
        final String[] versionParts = version.split("\\.", -1);
        if (versionParts.length == 0) {
            return -1;
        }

        final int majorVersion = tryParseInt(versionParts[0]);
        if (majorVersion == 1 && versionParts.length > 1) { // legacy versions are 1.x
            return tryParseInt(versionParts[1]);
        }

        return majorVersion;
    }

    private static int tryParseInt(final String input) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException ex) {
            return -1;
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
