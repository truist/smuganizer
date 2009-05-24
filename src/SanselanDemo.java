
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class SanselanDemo {
    
    public void readMetaData(File file) {
        IImageMetadata metadata = null;
        try {
            metadata = Sanselan.getMetadata(file);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            System.out.println("\nFile: " + file.getPath());
            printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_USER_COMMENT);
			
//			System.out.println("All EXIF items -");
//			ArrayList items = jpegMetadata.getItems();
//			for (int i = 0; i < items.size(); i++) {
//			Object item = items.get(i);
//				System.out.println("    " + item);
//			}
			
            System.out.println();
        }
    }

    private static void printTagValue(JpegImageMetadata jpegMetadata, TagInfo tagInfo) {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": " + field.getValueDescription());
        }
    }
	
	private TiffImageMetadata getEXIFData(File file) {
        try {
            IImageMetadata metadata = Sanselan.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				return ((JpegImageMetadata)metadata).getExif();
			}
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}

    private void modifyEXIF(File sourceFile) {
		TiffImageMetadata exif = getEXIFData(sourceFile);
		try {
			if (exif != null) {
				TiffField description = exif.findField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
				if (description != null && description.getStringValue().trim().length() > 0) {
					System.err.println("File has description: " + description.getStringValue().trim());
					TiffOutputSet outputSet = exif.getOutputSet();
					outputSet.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);

//					// add field 
//					String fieldData = "ImageHistory-" + System.currentTimeMillis();                
//					TiffOutputField imageHistory = new TiffOutputField(
//							   ExifTagConstants.EXIF_TAG_IMAGE_HISTORY, 
//							   TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
//							   fieldData.length(), 
//							   fieldData.getBytes());
//					TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
//					exifDirectory.add(imageHistory);

					File tempFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
					OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
					try {
						new ExifRewriter().updateExifMetadataLossless(sourceFile, outputStream, outputSet);
						FileUtils.copyFile(tempFile, sourceFile);
					} finally {
						outputStream.close();
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(SanselanDemo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ImageReadException ex) {
			Logger.getLogger(SanselanDemo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ImageWriteException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
        File testFile = new File("c:/documents and settings/narthur/Desktop/P7101782.jpg");
        SanselanDemo demo = new SanselanDemo();
        System.out.println("BEFORE update");
        demo.readMetaData(testFile);
        demo.modifyEXIF(testFile);
        System.out.println("\nAFTER update");
        demo.readMetaData(testFile);
    }
}
