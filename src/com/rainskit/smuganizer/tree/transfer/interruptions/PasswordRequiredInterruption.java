package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog.RepairPanel;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PasswordRequiredInterruption extends TransferInterruption {
	private TreeableGalleryItem item;
	private PasswordRepairPanel repairPanel;
	
	public PasswordRequiredInterruption(TreeableGalleryItem item) {
		super("Please specify a password");
		this.item = item;
	}
	
	public String getPassword() {
		return repairPanel.passwordField.getText();
	}
	
	@Override
	public String getErrorText() {
		return "When transferring this item, the source item was protected in some way, " 
			+ "usually by being marked \"hidden\" or by being protected by a password. "
			+ "In order to preserve that protection, you must specify a password that will "
			+ "be used in the new system (i.e. SmugMug) to only allow access to the people "
			+ "who know the password.";
	}

	@Override
	public RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new PasswordRepairPanel();
		}
		return repairPanel;
	}
	
	
	private class PasswordRepairPanel extends RepairPanel {
		JTextField passwordField;
		
		public PasswordRepairPanel() {
			super(new GridBagLayout());
			
			JPanel inputPanel = new JPanel(new BorderLayout());
			inputPanel.add(new JLabel("Please enter a password:"), BorderLayout.NORTH);
			inputPanel.add(passwordField = new JTextField());
			
			this.add(inputPanel);
		}
		
		@Override
		public String getDescription() {
			return item.getFullPathLabel();
		}

		@Override
		public String getUniqueKey() {
			return item.getFullPathLabel();
		}

		@Override
		public void loadSettingsFrom(RepairPanel otherPanel) throws Exception {
			passwordField.setText(((PasswordRepairPanel)otherPanel).passwordField.getText());
		}

		@Override
		public void post() throws Exception {
			//nothing needs to be done here
		}

	}
}
