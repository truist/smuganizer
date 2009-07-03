package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.ExifHandler;
import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import com.rainskit.smuganizer.menu.gui.TransferErrorDialog.RepairPanel;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public class UnexpectedCaptionInterruption extends TransferInterruption {
	private byte[] imageData;
	private String fileName;
	private String hiddenCaption;
	private CaptionRepairPanel repairPanel;
	
	public UnexpectedCaptionInterruption(byte[] imageData, String fileName, String hiddenCaption) {
		super("Image has a 'hidden' caption in the EXIF headers");
		
		this.imageData = imageData;
		this.fileName = fileName;
		this.hiddenCaption = hiddenCaption;
	}
	
	public String getCaption() {
		return hiddenCaption;
	}

	public String getFixedCaption() {
		return repairPanel.getFixedCaption();
	}

	public byte[] getImageData() {
		return imageData;
	}

	@Override
	public String getErrorText() {
		StringBuffer errorText = new StringBuffer();
		errorText.append("This image has metadata (EXIF tags) set, including the \"");
		errorText.append(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION.name);
		errorText.append("\" field.  ");
		errorText.append("When this field is set, and no other caption is provided by the user, ");
		errorText.append("SmugMug automatically uses this field as the caption.  Some cameras ");
		errorText.append("set this field automatically to be the name of the camera, ");
		errorText.append("which is probably not the caption you want.\n\n");
		errorText.append("The field is currently set to: ").append(hiddenCaption);
		return errorText.toString();
	}

	@Override
	public RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new CaptionRepairPanel();
		}
		return repairPanel;
	}
	
	static JTextArea makeMultiLineLabel(String text, Color background) {
		JTextArea label = new JTextArea(text);
		label.setFont(new JLabel().getFont());
		label.setEditable(false);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setBackground(background);
		return label;
	}

		
	private enum RadioChoice { FILE_NONE, FILE_REMOVE, FILE_REPLACE, SMUG_BLANK, SMUG_SET }
	
	private class CaptionRepairPanel extends RepairPanel implements ActionListener, DocumentListener {
		private ButtonGroup fileGroup;
		private ButtonGroup smugGroup;
		private JTextField fileSetField;
		private JTextField smugSetField;
		private JLabel resultLabel;
		
		public CaptionRepairPanel() {
			super(new BorderLayout());

			String header = getErrorText() + "\n\nPlease select how you would like to handle this:";
			JTextArea headerArea = makeMultiLineLabel(header, getBackground());
			
			add(headerArea, BorderLayout.NORTH);

			fileGroup = new ButtonGroup();
			JPanel fileOptionsPanel = new JPanel(new GridLayout(3, 1));
			fileOptionsPanel.setBorder(BorderFactory.createTitledBorder("File Options"));
			fileOptionsPanel.add(makeRadio("Leave the file alone", true, fileGroup, RadioChoice.FILE_NONE));
			fileOptionsPanel.add(makeRadio("Remove the caption from the file", false, fileGroup, RadioChoice.FILE_REMOVE));
			Box firstRowBox = Box.createHorizontalBox();
			firstRowBox.add(makeRadio("Replace the caption with: ", false, fileGroup, RadioChoice.FILE_REPLACE));
			fileSetField = new JTextField(hiddenCaption);
			fileSetField.getDocument().addDocumentListener(this);
			fileSetField.setEnabled(false);
			firstRowBox.add(fileSetField);
			fileOptionsPanel.add(firstRowBox);

			smugGroup = new ButtonGroup();
			JPanel smugMugOptionsPanel = new JPanel(new GridLayout(2, 1));
			smugMugOptionsPanel.setBorder(BorderFactory.createTitledBorder("SmugMug Options"));
			smugMugOptionsPanel.add(makeRadio("Leave the SmugMug caption blank", true, smugGroup, RadioChoice.SMUG_BLANK));
			Box secondRowBox = Box.createHorizontalBox();
			secondRowBox.add(makeRadio("Set a SmugMug caption: ", false, smugGroup, RadioChoice.SMUG_SET));
			smugSetField = new JTextField(hiddenCaption);
			smugSetField.getDocument().addDocumentListener(this);
			smugSetField.setEnabled(false);
			secondRowBox.add(smugSetField);
			smugMugOptionsPanel.add(secondRowBox);

			Box optionsPanel = Box.createVerticalBox();
			optionsPanel.add(fileOptionsPanel);
			optionsPanel.add(smugMugOptionsPanel);
			
			JPanel resultPanel = new JPanel(new BorderLayout());
			resultPanel.setMaximumSize(new Dimension(200, 900));
			resultPanel.add(new JLabel("Final SmugMug caption:"), BorderLayout.NORTH);
			resultLabel = new JLabel(hiddenCaption) {
				public Dimension getPreferredSize() {
					Dimension prefSize = super.getPreferredSize();
					prefSize.width = 200;
					return prefSize;
				}
				public Dimension getMaximumSize() { return getPreferredSize(); }
				public Dimension getMinimumSize() { return getPreferredSize(); }
			};
			resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD));
			resultPanel.add(resultLabel, BorderLayout.CENTER);

			Box centerPanel = Box.createHorizontalBox();
			centerPanel.add(optionsPanel);
			centerPanel.add(Box.createHorizontalStrut(5));
			centerPanel.add(resultPanel);

			add(centerPanel, BorderLayout.CENTER);
		}
	
		private JRadioButton makeRadio(String label, boolean selected, ButtonGroup buttonGroup, RadioChoice action) {
			JRadioButton newRadio = new JRadioButton(label, selected);
			newRadio.setActionCommand(action.toString());
			buttonGroup.add(newRadio);
			newRadio.addActionListener(this);
			return newRadio;
		}
		
		private void updateResultLabel() {
			String smugAction = smugGroup.getSelection().getActionCommand();
			String fileAction = fileGroup.getSelection().getActionCommand();
			if (RadioChoice.SMUG_SET.toString().equals(smugAction)) {
				resultLabel.setText(smugSetField.getText());
			} else if (RadioChoice.FILE_REPLACE.toString().equals(fileAction)) {
				resultLabel.setText(fileSetField.getText());
			} else {
				if (RadioChoice.FILE_REMOVE.toString().equals(fileAction)) {
					resultLabel.setText("(blank)");
				} else {
					resultLabel.setText(hiddenCaption);
				}
			}
		}

		public JComponent getPanel() {
			return this;
		}

		public String getDescription() {
			return fileName;
		}

		public String getUniqueKey() {
			try {
				return new String(MessageDigest.getInstance("SHA-1").digest(imageData));
			} catch (NoSuchAlgorithmException ex) {
				return toString();
			}
		}

		public void loadSettingsFrom(RepairPanel otherPanel) throws Exception {
			CaptionRepairPanel otherCaptionPanel = (CaptionRepairPanel)otherPanel;
			setSelectedItemFrom(otherCaptionPanel.fileGroup, fileGroup);
			fileSetField.setText(otherCaptionPanel.fileSetField.getText());
			setSelectedItemFrom(otherCaptionPanel.smugGroup, smugGroup);
			smugSetField.setText(otherCaptionPanel.smugSetField.getText());
		}
		
		private void setSelectedItemFrom(ButtonGroup sourceGroup, ButtonGroup targetGroup) {
			String targetActionCommand = sourceGroup.getSelection().getActionCommand();
			Enumeration fileElements = targetGroup.getElements();
			while (fileElements.hasMoreElements()) {
				AbstractButton each = (AbstractButton)fileElements.nextElement();
				if (each.getActionCommand().equals(targetActionCommand)) {
					each.setSelected(true);
					updateFieldStates(targetActionCommand);
					break;
				}
			}
		}

		public void post() throws Exception {
			String fileAction = fileGroup.getSelection().getActionCommand();
			if (RadioChoice.FILE_REPLACE.toString().equals(fileAction)) {
				imageData = ExifHandler.replaceExifDescription(imageData, fileName, fileSetField.getText());
			} else if (RadioChoice.FILE_REMOVE.toString().equals(fileAction)) {
				imageData = ExifHandler.removeExifDescription(imageData, fileName);
			}
		}
		
		public String getFixedCaption() {
			String smugAction = smugGroup.getSelection().getActionCommand();
			if (RadioChoice.SMUG_SET.toString().equals(smugAction)) {
				return smugSetField.getText();
			} else {
				return null;
			}
		}

		public void actionPerformed(ActionEvent e) {
			updateResultLabel();
			updateFieldStates(e.getActionCommand());
		}
		
		private void updateFieldStates(String touchedButton) {
			if (RadioChoice.SMUG_SET.toString().equals(touchedButton)) {
				smugSetField.setEnabled(true);
				smugSetField.requestFocusInWindow();
				smugSetField.selectAll();
			} else if (RadioChoice.FILE_REPLACE.toString().equals(touchedButton)) {
				fileSetField.setEnabled(true);
				fileSetField.requestFocusInWindow();
				fileSetField.selectAll();
			} else {
				smugSetField.setEnabled(RadioChoice.SMUG_SET.toString().equals(smugGroup.getSelection().getActionCommand()));
				fileSetField.setEnabled(RadioChoice.FILE_REPLACE.toString().equals(fileGroup.getSelection().getActionCommand()));
			}
		}

		public void insertUpdate(DocumentEvent e) {
			updateResultLabel();
		}

		public void removeUpdate(DocumentEvent e) {
			updateResultLabel();
		}

		public void changedUpdate(DocumentEvent e) {
			updateResultLabel();
		}
	}
}
