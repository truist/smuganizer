package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.tree.*;
import com.rainskit.smuganizer.settings.SettingsListener;
import com.rainskit.smuganizer.settings.SmugMugSettings;
import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import com.rainskit.smuganizer.*;
import java.io.IOException;
import javax.swing.DropMode;

public class SmugTree extends TransferTree implements SettingsListener {
	
	public SmugTree(Main main) {
		super(main, SmugAPIMethod.httpClient);
		setDropMode(DropMode.INSERT);
	}

	public void settingChanged(String settingName) {
		if (SmugMugSettings.TREE_SORT.equals(settingName) || SmugMugSettings.TREE_CATEGORY_SORT.equals(settingName)) {
			sortTree(rootNode, true);
			model.nodeStructureChanged(rootNode);
		}
	}

	public void loadTree(TreeableGalleryItem root) throws IOException {
		SmugMugSettings.setSettingsListener(this);
		loadTreeImpl(root, SmugMugSettings.getTreeSort());
	}
	
	@Override
	public int getSourceActions() {
		return SmugTransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean supportsImport() {
		return true;
	}

	@Override
	public boolean supportsInsertAtSpecificLocation() {
		return true;
	}
}
