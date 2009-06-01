package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import com.rainskit.smuganizer.smugmugapiwrapper.SmugAlbum;
import com.rainskit.smuganizer.tree.transfer.TransferInterruption;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class UnexpectedCaptionInterruption extends TransferInterruption {
	private byte[] imageData;
	private String fileName;
	private String hiddenCaption;
	private RepairPanel repairPanel;
	
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
	public TransferErrorDialog.RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new RepairPanel();
		}
		return repairPanel;
	}
	
	
	private enum RadioChoice { FILE_NONE, FILE_REMOVE, FILE_REPLACE, SMUG_BLANK, SMUG_SET }
	
	private class RepairPanel extends JPanel implements TransferErrorDialog.RepairPanel, ActionListener, DocumentListener {
		private ButtonGroup fileGroup;
		private ButtonGroup smugGroup;
		private JTextField fileSetField;
		private JTextField smugSetField;
		private JLabel resultLabel;
		
		public RepairPanel() {
			super(new BorderLayout());

			String header = getErrorText() + "\n\nPlease select how you would like to handle this:";
			JTextArea headerArea = makeMultiLineLabel(header);
			
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
		
		private JTextArea makeMultiLineLabel(String text) {
			JTextArea label = new JTextArea(text);
			label.setFont(new JLabel().getFont());
			label.setEditable(false);
			label.setLineWrap(true);
			label.setWrapStyleWord(true);
			label.setBackground(getBackground());
			return label;
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

		public void loadSettingsFrom(TransferErrorDialog.RepairPanel otherPanel) throws Exception {
			RepairPanel otherCaptionPanel = (RepairPanel)otherPanel;
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
				replaceFileCaption(fileSetField.getText());
			} else if (RadioChoice.FILE_REMOVE.toString().equals(fileAction)) {
				removeFileCaption();
			}
		}
		
		private void replaceFileCaption(String newValue) throws Exception {
			JpegImageMetadata metadata = SmugAlbum.loadMetaData(new ByteArrayInputStream(imageData), fileName);
			TiffOutputSet outputSet = metadata.getExif().getOutputSet();
			outputSet.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
			TiffOutputField descField
				= new TiffOutputField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION, 
										TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
										newValue.length(), 
										newValue.getBytes());
			TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
			exifDirectory.add(descField);
			writeOutputSet(outputSet);
		}
		
		private void removeFileCaption() throws Exception {
			JpegImageMetadata metadata = SmugAlbum.loadMetaData(new ByteArrayInputStream(imageData), fileName);
			TiffOutputSet outputSet = metadata.getExif().getOutputSet();
			outputSet.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
			writeOutputSet(outputSet);
		}

		private void writeOutputSet(TiffOutputSet outputSet) throws Exception {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			OutputStream outputStream = new BufferedOutputStream(byteStream);
			try {
				new ExifRewriter().updateExifMetadataLossy(imageData, outputStream, outputSet);
			} finally {
				try {
					outputStream.close();
				} catch (IOException ex) {
					Logger.getLogger(UnexpectedCaptionInterruption.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			imageData = byteStream.toByteArray();
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
