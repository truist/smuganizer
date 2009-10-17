package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog.RepairPanel;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.HandleDuplicate;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class DuplicateFileNameInterruption extends TransferInterruption {
	
	private TreeableGalleryItem item;
	private String caption;
	private boolean galleryAllowsDuplicates;
	private DuplicateRepairPanel repairPanel;
	
	public DuplicateFileNameInterruption(TreeableGalleryItem item, String caption, boolean galleryAllowsDuplicates) {
		super("File has same name as destination");
		this.item = item;
		this.caption = caption;
		this.galleryAllowsDuplicates = galleryAllowsDuplicates;
	}

	public HandleDuplicate getChoice() {
		String choice = repairPanel.buttonGroup.getSelection().getActionCommand();
		for (HandleDuplicate each : HandleDuplicate.values()) {
			if (each.toString().equals(choice)) {
				return each;
			}
		}
		throw new RuntimeException("Impossible choice");
	}

	@Override
	public String getErrorText() {
		String itemLabel;
		try {
			itemLabel = item.getFullPathLabel();
		} catch (SmugException ex) {
			Logger.getLogger(DuplicateFileNameInterruption.class.getName()).log(Level.SEVERE, null, ex);
			itemLabel = "ERROR: " + ex.getMessage();
		}
		return "The item you are trying to transfer ("
			+ itemLabel + ") has the same file name as a file "
			+ "that is already in the destination album.  The source file's name is \""
			+ item.getFileName() + "\" and its caption is\"" + caption + "\".";
	}

	@Override
	public RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new DuplicateRepairPanel();
		}
		return repairPanel;
	}
	
	
	private class DuplicateRepairPanel extends RepairPanel {
		private ButtonGroup buttonGroup;
		
		public DuplicateRepairPanel() {
			super(new BorderLayout());
			
			add(makeMultiLineLabel(getErrorText() + "\n\nHow would you like to handle this?", getBackground()), BorderLayout.NORTH);
			
			JPanel choicesPanel = new JPanel(new GridLayout(4, 1));
			buttonGroup = new ButtonGroup();
			
			JRadioButton duplicateRadio = makeRadio("Create a duplicate (this gallery supports duplicates)", galleryAllowsDuplicates, buttonGroup, HandleDuplicate.DUPLICATE);
			choicesPanel.add(duplicateRadio);
			JRadioButton overwriteRadio = makeRadio("Overwrite existing file (this gallery does not support duplicates)", !galleryAllowsDuplicates, buttonGroup, HandleDuplicate.OVERWRITE);
			choicesPanel.add(overwriteRadio);
			if (galleryAllowsDuplicates) {
				overwriteRadio.setEnabled(false);
			} else {
				duplicateRadio.setEnabled(false);
			}
			choicesPanel.add(makeRadio("Rename file", false, buttonGroup, HandleDuplicate.RENAME));
			choicesPanel.add(new JLabel("To cancel this operation, simply cancel this entire task"));
			
			JPanel centeringPanel = new JPanel(new GridBagLayout());
			centeringPanel.add(choicesPanel);
			
			add(centeringPanel, BorderLayout.CENTER);
		}
		
		private JRadioButton makeRadio(String label, boolean selected, ButtonGroup buttonGroup, HandleDuplicate action) {
			JRadioButton newRadio = new JRadioButton(label, selected);
			newRadio.setActionCommand(action.toString());
			buttonGroup.add(newRadio);
			return newRadio;
		}
		
		@Override
		public String getDescription() {
			try {
				return item.getFullPathLabel();
			} catch (SmugException ex) {
				Logger.getLogger(DuplicateFileNameInterruption.class.getName()).log(Level.SEVERE, null, ex);
				return "ERROR: " + ex.getLocalizedMessage();
			}
		}

		@Override
		public String getUniqueKey() {
			try {
				return item.getFullPathLabel();
			} catch (SmugException ex) {
				Logger.getLogger(DuplicateFileNameInterruption.class.getName()).log(Level.SEVERE, null, ex);
				return "ERROR: " + ex.getLocalizedMessage();
			}
		}

		@Override
		public void loadSettingsFrom(RepairPanel otherPanel) throws Exception {
			DuplicateRepairPanel duplicatePanel = (DuplicateRepairPanel)otherPanel;
			String targetActionCommand = duplicatePanel.buttonGroup.getSelection().getActionCommand();
			Enumeration fileElements = buttonGroup.getElements();
			while (fileElements.hasMoreElements()) {
				AbstractButton each = (AbstractButton)fileElements.nextElement();
				if (each.getActionCommand().equals(targetActionCommand)) {
					each.setSelected(true);
					break;
				}
			}
		}

		@Override
		public void post() throws Exception {
			//nothing to do
		}
		
	}
}
