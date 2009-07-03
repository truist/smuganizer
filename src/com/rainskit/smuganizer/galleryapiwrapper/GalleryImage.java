package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.GallerySettings;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

public class GalleryImage extends AbstractGalleryTreeable {
	private Properties imageDetails;
	private int imageRefNum;
	
	public GalleryImage(GalleryAlbum parent, Properties imageDetails, int imageRefNum) {
		super(parent);
		
		this.imageDetails = imageDetails;
		this.imageRefNum = imageRefNum;
	}

	public List<? extends TreeableGalleryItem> loadChildren() {
		return null;
	}

	public List<? extends TreeableGalleryItem> getChildren() {
		return null;
	}
	
	public URL getPreviewURL() throws MalformedURLException {
		String previewFileName = imageDetails.getProperty(Gallery.RESPONSE_IMAGE_RESIZED_NAME_INDEXED + imageRefNum);
		if (previewFileName == null) {
			return getDataURL();
		} else {
			return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + previewFileName);
		}
	}
	
	public URL getDataURL() throws MalformedURLException {
		return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + getURLName());
	}
	
	public String getURLName() {
		return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_NAME_INDEXED + imageRefNum).replace(' ', '_');
	}
	
	private String getLaunchURLName() {
		String urlName = getURLName();
		int period = urlName.lastIndexOf('.');
		if (period > 0) {
			urlName = urlName.substring(0, period);
		}
		return urlName;
	}

	public boolean isHidden() {
		return Gallery.ARGVAL_YES.equals(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_HIDDEN_INDEXED + imageRefNum));
	}

	public String getLabel() {
		return StringEscapeUtils.unescapeHtml(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_CAPTION_INDEXED + imageRefNum));
	}

	public String getCaption() {
		String rawCaption = imageDetails.getProperty(Gallery.RESPONSE_IMAGE_CAPTION_INDEXED + imageRefNum);
		if (GallerySettings.getCleanCaptions()) {
			return generateCleanCaption(getFileName(), rawCaption);
		} else {
			return rawCaption;
		}
	}
	
	public String getDescription() {
		return null;
	}

	static String generateCleanCaption(String rawName, String rawCaption) {
		String name = StringEscapeUtils.unescapeHtml(rawName);
		String caption = StringEscapeUtils.unescapeHtml(rawCaption);
		
		int lastPeriod = name.lastIndexOf('.');
		if (name.equalsIgnoreCase(caption) || name.substring(0, lastPeriod).equalsIgnoreCase(caption)) {
			if (name.contains(" ")) {
				return caption.substring(0, lastPeriod);
			} else {
				int lastUnderscore = name.substring(0, lastPeriod).lastIndexOf('_');
				if (lastUnderscore > 0 && !Pattern.matches("^\\d+$", name.substring(lastUnderscore + 1, lastPeriod))) {
					return caption.substring(0, lastPeriod).replace('_', ' ');
				} else {
					return null;
				}
			}
		} else {
			return caption;
		}
	}
	
	public String getFileName() {
		if (Gallery.galleryVersion > 1) {
			return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_TITLE_INDEXED + imageRefNum);
		} else {
			return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_NAME_INDEXED + imageRefNum);
		}
	}
	
	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(Gallery.generateUrlFor(((GalleryAlbum)parent).getURLName(), getLaunchURLName()));
	}

	public ItemType getType() {
		return ItemType.IMAGE;
	}

	public int compareTo(TreeableGalleryItem other) {
		if (other instanceof GalleryImage) {
			return Integer.valueOf(imageRefNum).compareTo(Integer.valueOf(((GalleryImage)other).imageRefNum));
		} else {
			return 0;
		}
	}

	public String getMetaLabel() {
		return (isHidden() ? " [hidden]" : "");
	}

	@Override
	public boolean hasPassword() {
		return false;
	}
}

class ImageNameConverterTest {
	private static int passes;
	private static int failures;
	
	public static void main(String[] args) {
		testCase("AA123456.JPG", "AA123456", "", "caption is just filename without extension");
		testCase("AA123456.JPG", "AA123456.jpg", "", "caption is just filename with different case");
		testCase("AA123456.JPG", "AA12", "AA12", "caption is a significant subset of the filename, so assume it was explicitly set");
		testCase("AA123456.JPG", "Hello There", "Hello There", "caption has been explicitly set");
		testCase("AA123456.JPG", "Test.dat", "Test.dat", "caption has been explicitly set, even though it has an extension");
		testCase("Hello-There.jpg", "Hello There", "Hello There", "caption has a space, so it's probably a real caption");
		testCase("Hello There.jpg", "Hello There.jpg", "Hello There", "file name has a space, so it's probably meant to be the caption, but we don't want the file extension");
		testCase("Hello_1234.jpg", "Hello_1234.jpg", "", "underscores aren't special if they are followed by numbers");
		testCase("Hello_There.jpg", "Hello_There.jpg", "Hello There", "underscores are special if they are followed by letters");
		
		testCase("Hello_&_There.jpg", "Hello & There", "Hello & There", "caption is different, ampersand makes it through");
		testCase("Hello_&_There.jpg", "Hello &amp; There", "Hello & There", "caption is different, and ampersand is corrected");
		testCase("AA&12345.jpg", "AA&amp;12345", "", "caption is just subset of filename, once ampersand is corrected");
		testCase("Hello_&amp;_There.jpg", "Hello &amp; There", "Hello & There", "caption has spaces, and ampersand is corrected");
		testCase("Hello_&amp;_There.jpg", "Hello & There", "Hello & There", "caption has spaces, ampersand makes it through");
		testCase("AA&amp;12345.jpg", "AA&12345", "", "caption is just subset of filename, once ampersand is corrected");
		testCase("Hello_&amp;_1234.jpg", "Hello_&_1234", "", "underscores aren't special if they are followed by numbers");
		
		System.err.println("" + passes + " passed, " + failures + " failed.");
	}
	
	private static int testCase(String rawName, String rawCaption, String expectedResult, String explanation) {
		if (!expectedResult.equals(GalleryImage.generateCleanCaption(rawName, rawCaption))) {
			System.err.println("Failed: " + explanation + " - rawName=\"" + rawName + "\" rawCaption=\"" + rawCaption + "\" expectedResult=\"" + expectedResult + "\"");
			return failures++;
		} else {
			return passes++;
		}
	}
	
}