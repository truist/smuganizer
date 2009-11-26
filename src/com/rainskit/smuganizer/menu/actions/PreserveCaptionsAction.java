package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.settings.FileSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class PreserveCaptionsAction extends AbstractAction {
	public PreserveCaptionsAction() {
		super("Use caption as filename, if available");
		setSelected(FileSettings.getPreserveCaptions());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}

	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		FileSettings.setPreserveCaptions(selected);
	}
}
