package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.settings.LogSettings;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class LogHeadersAction extends AbstractAction {
	public LogHeadersAction() {
		super("Log HTTP headers");
		setSelected(LogSettings.getLogHeaders());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}

	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		LogSettings.setLogHeaders(selected);

		Logger.getLogger("httpclient.wire.header").setLevel(selected ? Level.FINE : null);
	}
}
