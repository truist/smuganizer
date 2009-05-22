package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
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

	public URL getPreviewURL() throws MalformedURLException {
		String previewFileName = imageDetails.getProperty(Gallery.RESPONSE_IMAGE_RESIZED_NAME_INDEXED + imageRefNum);
		if (previewFileName == null) {
			return getDataURL();
		} else {
			return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + previewFileName);
		}
	}
	
	public URL getDataURL() throws MalformedURLException {
		return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + getName());
	}
	
	public String getName() {
		return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_NAME_INDEXED + imageRefNum);
	}
	
	private String getUrlName() {
		String name = getName();
		name = name.replace(' ', '_');
		int period = name.lastIndexOf('.');
		if (period > 0) {
			name = name.substring(0, period);
		}
		return name;
	}

	public boolean isHidden() {
		return Gallery.ARGVAL_YES.equals(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_HIDDEN_INDEXED + imageRefNum));
	}

	public String getLabel() {
		return StringEscapeUtils.unescapeHtml(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_CAPTION_INDEXED + imageRefNum));
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(Gallery.generateUrlFor(((GalleryAlbum)parent).getName(), getUrlName()));
	}

	public String getType() {
		return IMAGE;
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
