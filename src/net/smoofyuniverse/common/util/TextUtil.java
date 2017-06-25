package net.smoofyuniverse.common.util;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.smoofyuniverse.common.app.OperatingSystem;

import java.net.URI;
import java.net.URISyntaxException;

public class TextUtil {
	
	public static TextFlow justify(double lineLength, Node... childs) {
		TextFlow t = new TextFlow(childs);
		t.setTextAlignment(TextAlignment.JUSTIFY);
		t.setPrefWidth(lineLength);
		return t;
	}
	
	public static TextFlow justify(Node... childs) {
		TextFlow t = new TextFlow(childs);
		t.setTextAlignment(TextAlignment.JUSTIFY);
		return t;
	}
	
	public static TextFlow join(double lineLength, Node... childs) {
		TextFlow t = new TextFlow(childs);
		t.setPrefWidth(lineLength);
		return t;
	}
	
	public static TextFlow join(Node... childs) {
		return new TextFlow(childs);
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
