package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.GallerySettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class CleanCaptionsAction extends AbstractAction {

	public CleanCaptionsAction() {
		super("Fix \"&amp;\" and remove captions that match filename when copying files");
		setSelected(GallerySettings.getCleanCaptions());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}
	
	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		GallerySettings.setCleanCaptions(selected);
	}
}
