package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.settings.SmugMugSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class SettingsSortCategoryAction extends AbstractAction {
	public SettingsSortCategoryAction() {
		super("Sort subcategories above albums");
		setSelected(SmugMugSettings.getTreeCategorySort());
	}
	
	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}
	
	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		SmugMugSettings.setTreeCategorySort(selected);
	}

	public void updateStateForTreeSort(boolean treeSort) {
		setEnabled(treeSort);
	}

}
