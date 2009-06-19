package com.rainskit.smuganizer.menu.gui;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TransferErrorDialog extends JDialog implements ActionListener {
	private enum ButtonActions { APPLY_ALL, RETRY, CANCEL }
	
	private static final String BLANK_CARD = "blank_card";
	
	private List<AbstractTransferTask> initialItems;
	private TransferTable transferTable;
	
	private boolean showRepairPanel;
	private HashMap<AbstractTransferTask, RepairPanel> repairPanels;
	private JTextArea errorText;
	private CardLayout repairCards;
	private JPanel cardPanel;
	
	private boolean closedWithRetry;
	
	public TransferErrorDialog(Main main, List<AbstractTransferTask> initialItems, boolean showRepairPanel) {
		super(main, "Transfer errors", true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		this.initialItems = initialItems;
		this.transferTable = new TransferTable(new TransferTableModel(initialItems), false);
		this.showRepairPanel = showRepairPanel;
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(new JScrollPane(transferTable), BorderLayout.CENTER);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tablePanel, BorderLayout.CENTER);
		getContentPane().add(makeButtonPanel(), BorderLayout.EAST);
		
		if (showRepairPanel) {
			repairPanels = new HashMap<AbstractTransferTask, RepairPanel>();
			repairCards = new CardLayout();
			cardPanel = new JPanel(repairCards);
			cardPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			cardPanel.add(new JPanel(), BLANK_CARD);
			for (AbstractTransferTask each : initialItems) {
				RepairPanel eachPanel = each.getInterruption().getRepairPanel();
				repairPanels.put(each, eachPanel);
				cardPanel.add(eachPanel, eachPanel.getUniqueKey());
			}
			tablePanel.add(cardPanel, BorderLayout.SOUTH);
		} else {
			errorText = new JTextArea(10, 0);
			errorText.setEditable(false);
			errorText.setLineWrap(true);
			errorText.setWrapStyleWord(true);
			errorText.setFont(errorText.getFont().deriveFont(errorText.getFont().getSize() * 0.9f));
			tablePanel.add(new JScrollPane(errorText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.SOUTH);
		}
		
		transferTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<AbstractTransferTask> selectedTasks = transferTable.getSelectedItems();
				if (selectedTasks.size() == 1) {
					AbstractTransferTask selectedTask = selectedTasks.get(0);
					if (TransferErrorDialog.this.showRepairPanel) {
						repairCards.show(cardPanel, repairPanels.get(selectedTask).getUniqueKey());
					} else {
						errorText.setText(selectedTask.getErrorText());
						errorText.setCaretPosition(0);
					}
				} else {
					if (TransferErrorDialog.this.showRepairPanel) {
						repairCards.show(cardPanel, BLANK_CARD);
					} else {
						errorText.setText("");
					}
				}
			}
		});
		
		transferTable.getSelectionModel().setSelectionInterval(0, 0);
		
		pack();
		setSize(700, 450);
		setLocationRelativeTo(main);
	}

	public boolean shouldRetryTasks() {
		return closedWithRetry;
	}

	private JComponent makeButtonPanel() {
		Box buttonPanel = Box.createVerticalBox();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JButton retryButton = new JButton("Retry");
		retryButton.setActionCommand(ButtonActions.RETRY.toString());
		retryButton.addActionListener(this);
		buttonPanel.add(retryButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(ButtonActions.CANCEL.toString());
		cancelButton.addActionListener(this);
		buttonPanel.add(Box.createVerticalStrut(5));
		buttonPanel.add(cancelButton);
		
		if (showRepairPanel) {
			JButton applyAllButton = new JButton("Apply to all");
			applyAllButton.setActionCommand(ButtonActions.APPLY_ALL.toString());
			applyAllButton.addActionListener(this);
			buttonPanel.add(Box.createVerticalGlue());
			buttonPanel.add(applyAllButton);
		}
		
		return buttonPanel;
	}
	
	public void actionPerformed(ActionEvent ae) {
		if (ButtonActions.APPLY_ALL.toString().equals(ae.getActionCommand())) {
			List<AbstractTransferTask> selectedItems = transferTable.getSelectedItems();
			if (selectedItems.size() == 1) {
				RepairPanel currentPanel = repairPanels.get(selectedItems.get(0));
				for (RepairPanel each : repairPanels.values()) {
					if (each != currentPanel) {
						String encounteredError = null;
						try {
							if (each.getClass() == currentPanel.getClass()) {
								each.loadSettingsFrom(currentPanel);
							} else {
								encounteredError = "Transfer is a different type";
							}
						} catch (Exception e) {
							Logger.getLogger(TransferErrorDialog.class.getName()).log(Level.SEVERE, "Unable to apply changes to repair panel", e);
							encounteredError = e.getLocalizedMessage();
						}
						if (encounteredError != null) {
							JOptionPane.showMessageDialog(TransferErrorDialog.this, 
								"Error: unable to apply changes to " + each.getDescription() + ": " + encounteredError, 
								"Error applying changes", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		} else {
			boolean encounteredError = false;
			if (ButtonActions.RETRY.toString().equals(ae.getActionCommand())) {
				closedWithRetry = true;
				if (showRepairPanel) {
					for (RepairPanel each : repairPanels.values()) {
						try {
							each.post();
						} catch (Exception e) {
							encounteredError = true;
							Logger.getLogger(TransferErrorDialog.class.getName()).log(Level.SEVERE, "Unable to apply changes to repair panel", e);
							JOptionPane.showMessageDialog(TransferErrorDialog.this, 
								"Error: unable to apply changes to " + each.getDescription() + ": " + e.getLocalizedMessage(), 
								"Error applying changes", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			} else {
				closedWithRetry = false;
			}
			if (!encounteredError) {
				setVisible(false);
				dispose();
			}
		}
	}
	
	
	public static abstract class RepairPanel extends JPanel {
		public RepairPanel(LayoutManager layoutManager) {
			super(layoutManager);
		}
		public abstract String getDescription();
		public abstract String getUniqueKey();
		public abstract void loadSettingsFrom(RepairPanel otherPanel) throws Exception;
		public abstract void post() throws Exception;
	}
}
