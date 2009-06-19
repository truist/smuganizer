package com.rainskit.smuganizer;

import com.rainskit.smuganizer.smugmugapiwrapper.SmugAlbum;
import com.rainskit.smuganizer.tree.transfer.interruptions.UnexpectedCaptionInterruption;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class ExifHandler {
	private ExifHandler() {}

	public static String getExifDescription(byte[] imageData, String fileName) throws ImageReadException {
		JpegImageMetadata metadata = loadMetaData(new ByteArrayInputStream(imageData), fileName);
		if (metadata != null) {
			TiffField description = metadata.findEXIFValue(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
			if (description != null) {
				return description.getStringValue().trim();
			}
		}
		return null;
	}
	
	public static byte[] replaceExifDescription(byte[] imageData, String fileName, String newValue) throws ImageWriteException, ImageReadException, IOException {
		JpegImageMetadata metadata = loadMetaData(new ByteArrayInputStream(imageData), fileName);
		TiffOutputSet outputSet = metadata.getExif().getOutputSet();
		outputSet.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
		TiffOutputField descField
			= new TiffOutputField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION, 
									TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
									newValue.length(), 
									newValue.getBytes());
		TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
		exifDirectory.add(descField);
		return writeOutputSet(imageData, outputSet);
	}

	public static byte[] removeExifDescription(byte[] imageData, String fileName) throws ImageWriteException, ImageReadException, IOException {
		JpegImageMetadata metadata = loadMetaData(new ByteArrayInputStream(imageData), fileName);
		TiffOutputSet outputSet = metadata.getExif().getOutputSet();
		outputSet.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
		return writeOutputSet(imageData, outputSet);
	}

	private static byte[] writeOutputSet(byte[] imageData, TiffOutputSet outputSet) throws ImageReadException, IOException, ImageWriteException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		OutputStream outputStream = new BufferedOutputStream(byteStream);
		try {
			new ExifRewriter().updateExifMetadataLossy(imageData, outputStream, outputSet);
		} finally {
			try {
				outputStream.close();
			} catch (IOException ex) {
				Logger.getLogger(ExifHandler.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return byteStream.toByteArray();
	}

	
    public static JpegImageMetadata loadMetaData(InputStream inputStream, String fileName) {
		try {
			IImageMetadata metadata = Sanselan.getMetadata(inputStream, fileName);
			if (metadata instanceof JpegImageMetadata) {
				return (JpegImageMetadata) metadata;
			}
		} catch (ImageReadException ex) {
			Logger.getLogger(ExifHandler.class.getName()).log(Level.WARNING, "Unable to load metadata for " + fileName);
		} catch (IOException ex) {
			Logger.getLogger(ExifHandler.class.getName()).log(Level.WARNING, "Unable to load metadata for " + fileName);
		}
		return null;
    }
	
    public static JpegImageMetadata loadMetaData(URL imagePath, String fileName) {
		InputStream inputStream = null;
		try {
			inputStream = imagePath.openStream();
			return loadMetaData(inputStream, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
					Logger.getLogger(ExifHandler.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return null;
    }
}
