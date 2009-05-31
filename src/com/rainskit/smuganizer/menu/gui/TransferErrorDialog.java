package com.rainskit.smuganizer.menu.gui;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TransferErrorDialog extends JDialog {
	public enum ErrorAction { 
		RETRY, CANCEL, CLOSE;

		@Override
		public String toString() {
			switch (this) {
				case RETRY: return "Retry";
				case CANCEL: return "Cancel";
				case CLOSE: return "Close";
				default: throw new IllegalArgumentException("Illegal ErrorAction: " + this.name());
			}
		}
	}
	
	private List<AbstractTransferTask> initialItems;
	private TransferTable transferTable;
	
	private ErrorAction chosenAction;
	private List<AbstractTransferTask> chosenItems;
	
	public TransferErrorDialog(Main main, List<AbstractTransferTask> initialItems) {
		super(main, "Transfer errors", true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		this.initialItems = initialItems;
		this.transferTable = new TransferTable(new TransferTableModel(initialItems));
		
		final JTextArea errorText = new JTextArea(10, 0);
		errorText.setEditable(false);
		errorText.setLineWrap(true);
		errorText.setWrapStyleWord(true);
		errorText.setFont(errorText.getFont().deriveFont(errorText.getFont().getSize() * 0.9f));
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(new JScrollPane(transferTable), BorderLayout.CENTER);
		tablePanel.add(new JScrollPane(errorText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(new DialogButton(ErrorAction.RETRY, transferTable.getSelectionModel()));
		buttonPanel.add(new DialogButton(ErrorAction.RETRY, null));
		buttonPanel.add(new DialogButton(ErrorAction.CANCEL, transferTable.getSelectionModel()));
		buttonPanel.add(new DialogButton(ErrorAction.CANCEL, null));
		buttonPanel.add(new DialogButton(ErrorAction.CLOSE, null));
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tablePanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		transferTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<AbstractTransferTask> selectedTasks = transferTable.getSelectedItems();
				if (selectedTasks.size() == 1) {
					errorText.setText(selectedTasks.get(0).getErrorText());
					errorText.setCaretPosition(0);
				} else {
					errorText.setText("");
				}
			}
		});
		
		transferTable.getSelectionModel().setSelectionInterval(0, 0);
		
		pack();
		setSize(750, 550);
		setLocationRelativeTo(main);
	}
	
	public ErrorAction getChosenAction() {
		return chosenAction;
	}
	
	public List<AbstractTransferTask> getActionTasks() {
		return chosenItems;
	}
	
	
	private class DialogButton extends JButton implements ListSelectionListener, ActionListener {
		private ErrorAction action;
		private boolean onlySelected;
		
		public DialogButton(ErrorAction action, ListSelectionModel selectionModel) {
			super(action.toString() + (selectionModel != null ? " Selected" : " All"));
			this.action = action;
			this.onlySelected = (selectionModel != null);
			if (onlySelected) {
				selectionModel.addListSelectionListener(this);
			}
			addActionListener(this);
		}
		
		public void valueChanged(ListSelectionEvent lse) {
			setEnabled(transferTable.getSelectedItems().size() == 1);
		}
		
		public void actionPerformed(ActionEvent ae) {
			chosenAction = action;
			chosenItems = (onlySelected ? transferTable.getSelectedItems() : initialItems);
			TransferErrorDialog.this.setVisible(false);
		}
	}
}
