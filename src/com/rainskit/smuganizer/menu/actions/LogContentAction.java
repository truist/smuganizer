package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.settings.LogSettings;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class LogContentAction extends AbstractAction {
	public LogContentAction() {
		super("Log HTTP headers and content (including image data)");
		setSelected(LogSettings.getLogContent());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}

	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		LogSettings.setLogContent(selected);

		Logger.getLogger("httpclient.wire").setLevel(selected ? Level.FINE : null);
	}
}
