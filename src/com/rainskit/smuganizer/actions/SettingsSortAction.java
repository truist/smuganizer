package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.SmugMugSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

class SettingsSortAction extends AbstractAction {
	private SettingsSortCategoryAction sortAlbumsAction;
	
	public SettingsSortAction(SettingsSortCategoryAction sortAlbumsAction) {
		super("Sort categories and albums by name");
		this.sortAlbumsAction = sortAlbumsAction;
		
		setSelected(SmugMugSettings.getTreeSort());
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}

	private void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		SmugMugSettings.setTreeSort(selected);
		if (!selected) {
			sortAlbumsAction.setSelected(true);
		}
		sortAlbumsAction.updateStateForTreeSort(selected);
	}
}
