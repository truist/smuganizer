package com.rainskit.smuganizer.menu.gui;

import com.rainskit.smuganizer.ExifHandler;
import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public class ExifBox extends JDialog {
	public ExifBox(Smuganizer main, TreeableGalleryItem image) throws IOException {
		super(main, "EXIF Tags", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JpegImageMetadata metadata = ExifHandler.loadMetaData(image.getDataURL(), image.getFileName());
		if (metadata == null) {
			throw new IOException("Unable to load EXIF data from " + image.getDataURL().toExternalForm());
		}
		
		getContentPane().setLayout(new BorderLayout());
		
		JLabel nameLabel = new JLabel(image.getFullPathLabel());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		getContentPane().add(nameLabel, BorderLayout.NORTH);
		
		JTable standardTable = new JTable(getStandardItems(metadata), new String[]{"Tag", "Value"});
		JPanel standardPanel = new JPanel(new BorderLayout());
		standardPanel.add(standardTable);
		standardPanel.setBorder(BorderFactory.createTitledBorder("Standard Tags"));
		
		JTable allTable = new JTable(convertToArray(metadata.getItems()), new String[]{"Tag", "Value"});
		JPanel allPanel = new JPanel(new BorderLayout());
		allPanel.add(new JScrollPane(allTable));
		allPanel.setBorder(BorderFactory.createTitledBorder("All Tags"));
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(standardPanel, BorderLayout.NORTH);
		tablePanel.add(allPanel, BorderLayout.CENTER);
		getContentPane().add(tablePanel, BorderLayout.CENTER);
		
		pack();
		setSize(500, 500);
		setLocationRelativeTo(main);
	}
	
	private String[][] convertToArray(ArrayList standardItems) {
		String[][] table = new String[standardItems.size()][2];
		for (int i = 0; i < standardItems.size(); i++) {
			ImageMetadata.Item each = (ImageMetadata.Item)standardItems.get(i);
			table[i][0] = each.getKeyword();
			table[i][1] = each.getText();
		}
		return table;
	}
	
	private String[][] getStandardItems(JpegImageMetadata metadata) {
		String[][] table = new String[5][2];
		setTableValues(table, 0, TiffConstants.EXIF_TAG_CREATE_DATE, metadata);
		setTableValues(table, 1, TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION, metadata);
		setTableValues(table, 2, TiffConstants.EXIF_TAG_USER_COMMENT, metadata);
		setTableValues(table, 3, TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH, metadata);
		setTableValues(table, 4, TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH, metadata);
		return table;
	}

	private void setTableValues(String[][] table, int row, TagInfo tag, JpegImageMetadata metadata) {
		table[row][0] = tag.name;
		TiffField matchedItem = metadata.findEXIFValue(tag);
		table[row][1] = (matchedItem != null ? matchedItem.getValueDescription() : "");
	}
}
