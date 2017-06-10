package net.smoofyuniverse.common.util;

import javafx.scene.Node;
import javafx.scene.control.Tab;

public class TabUtil {
	public static Tab createTab(String text, Node content) {
		Tab tab = new Tab(text, content);
		tab.setClosable(false);
		return tab;
	}
}
