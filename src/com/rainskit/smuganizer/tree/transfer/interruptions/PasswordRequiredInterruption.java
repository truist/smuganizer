package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog.RepairPanel;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
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

	public String getHint() {
		return repairPanel.hintField.getText();
	}
	
	@Override
	public String getErrorText() {
		return "When transferring this item, the source item was protected in some way, " 
			+ "usually by being marked \"hidden\" or by being protected by a password. "
			+ "In order to preserve that protection, you must specify a password "
			+ "to only allow access to the people who know the password.  You can "
			+ "also specify a password hint, if desired, that guests will see.";
	}

	@Override
	public RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new PasswordRepairPanel();
		}
		return repairPanel;
	}
	
	
	private class PasswordRepairPanel extends RepairPanel {
		private JTextField passwordField;
		private JTextField hintField;
		
		public PasswordRepairPanel() {
			super(new BorderLayout());
			
			add(makeMultiLineLabel(getErrorText(), getBackground()), BorderLayout.NORTH);
			
			JPanel passwordPanel = new JPanel(new BorderLayout());
			passwordPanel.add(new JLabel("Password:"), BorderLayout.NORTH);
			passwordPanel.add(passwordField = new JTextField(20), BorderLayout.CENTER);
			
			JPanel hintPanel = new JPanel(new BorderLayout());
			hintPanel.add(new JLabel("Hint:"), BorderLayout.NORTH);
			hintPanel.add(hintField = new JTextField(20), BorderLayout.CENTER);
			
			JPanel inputPanel = new JPanel(new GridLayout(1, 2));
			inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			inputPanel.add(passwordPanel);
			inputPanel.add(hintPanel);
			
			JPanel centeringPanel = new JPanel(new GridBagLayout());
			centeringPanel.add(inputPanel);
			
			add(centeringPanel, BorderLayout.CENTER);
		}
		
		@Override
		public String getDescription() {
			try {
				return item.getFullPathLabel();
			} catch (SmugException ex) {
				Logger.getLogger(PasswordRequiredInterruption.class.getName()).log(Level.SEVERE, null, ex);
				return "ERROR: " + ex.getMessage();
			}
		}

		@Override
		public String getUniqueKey() {
			try {
				return item.getFullPathLabel();
			} catch (SmugException ex) {
				Logger.getLogger(PasswordRequiredInterruption.class.getName()).log(Level.SEVERE, null, ex);
				return "ERROR: " + ex.getMessage();
			}
		}

		@Override
		public void loadSettingsFrom(RepairPanel otherPanel) throws Exception {
			PasswordRepairPanel passwordPanel = (PasswordRepairPanel)otherPanel;
			passwordField.setText(passwordPanel.passwordField.getText());
			hintField.setText(passwordPanel.hintField.getText());
		}

		@Override
		public void post() throws Exception {
			//nothing needs to be done here
		}

	}
}
