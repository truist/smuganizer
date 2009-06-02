package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.TransferSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public class AlwaysRemoveFileCaptionAction extends AbstractAction {
	public AlwaysRemoveFileCaptionAction() {
		super("Always remove EXIF \"" + TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION.name + "\" headers");
		setSelected(TransferSettings.getRemoveExifDescriptions());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}
	
	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		TransferSettings.setRemoveExifDescriptions(selected);
	}
}
