package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.UnexpectedCaptionInterruption;
import com.rainskit.smuganizer.SmugMugSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.MoveException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.ReorderException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.PasswordException;
import com.rainskit.smuganizer.tree.transfer.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public class SmugAlbum extends TreeableGalleryItem {
	private static final int NO_IMAGES_FOUND_ERROR = 15;
	
	private com.kallasoft.smugmug.api.json.entity.Album apiAlbum;
	private boolean childrenLoaded;
	private ArrayList<SmugImage> images;
	private String reName;
	private String password;

	public SmugAlbum(SmugCategory parent, com.kallasoft.smugmug.api.json.entity.Album apiAlbum) {
		super(parent);
		this.apiAlbum = apiAlbum;
	}

	public void setParent(SmugCategory newParent) {
		this.parent = newParent;
		reloadDetails();
	}

	private void reloadDetails() {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo getInfo
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo.GetInfoResponse response
			= getInfo.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID(), apiAlbum.getAlbumKey());
		if (response.isError()) {
			throw new SmugException("Error loading album details for album with ID \"" + apiAlbum.getID() + "\" in category \"" + parent.getFullPathLabel() + "\"", response.getError());
		}
		apiAlbum = response.getAlbum();
	}

	public List<? extends TreeableGalleryItem> loadChildren() {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo albumGet = new com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo.GetInfoResponse albumResponse
			= albumGet.execute(SmugMug.API_URL, 
								SmugMug.API_KEY, 
								SmugMug.sessionID, 
								apiAlbum.getID(), 
								apiAlbum.getAlbumKey());
		if (albumResponse.isError()) {
			throw new SmugException("Failed to load album details for \"" + getFullPathLabel() + "\"", albumResponse.getError());
		}
		apiAlbum = albumResponse.getAlbum();
		
		com.kallasoft.smugmug.api.json.v1_2_0.images.Get imageGet = new com.kallasoft.smugmug.api.json.v1_2_0.images.Get();
		com.kallasoft.smugmug.api.json.v1_2_0.images.Get.GetResponse imageResponse 
			= imageGet.execute(SmugMug.API_URL, 
								SmugMug.API_KEY, 
								SmugMug.sessionID, 
								apiAlbum.getID(), 
								apiAlbum.getAlbumKey(), 
								Boolean.FALSE);
		if (imageResponse.isError() && imageResponse.getError().getCode().intValue() != NO_IMAGES_FOUND_ERROR) {
			throw new SmugException("Failed to get images from album \"" + getFullPathLabel() + "\"", imageResponse.getError());
		}
		images = new ArrayList<SmugImage>();
		for (com.kallasoft.smugmug.api.json.entity.Image each : imageResponse.getImageList()) {
			images.add(new SmugImage(this, each));
		}
		childrenLoaded = true;
		return images;
	}
	
	public List<SmugImage> getImages() {
		if (!childrenLoaded) {
			loadChildren();
		}
		return images;
	}

	public String getLabel() {
		return getName();
	}

	public String getName() {
		return (reName != null) ? reName : apiAlbum.getTitle();
	}
	
	public String getCaption() {
		return null;
	}

	public boolean canBeRelabeled() {
		return true;
	}

	public void reLabel(String newName) throws RenameException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings.ChangeSettingsResponse response 
			= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID(), newName, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

		if (response.isError()) {
			throw new RenameException(this, newName, response.getError());
		}
		reName = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException  {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/gallery/" + apiAlbum.getID() + "_" + apiAlbum.getAlbumKey(), null));
	}

	public boolean canBeDeleted() {
		return true;
	}

	public void delete() throws DeleteException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete delete 
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete.DeleteResponse response 
			= delete.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID());
		if (response.isError()) {
			throw new DeleteException(this, response.getError());
		}
		parent.removeChild(this);
	}

	public void removeChild(TreeableGalleryItem image) {
		images.remove(image);
	}

	public boolean canMove(TreeableGalleryItem newChild, int childIndex) {
		return (ItemType.IMAGE == newChild.getType());
	}

	public void moveItem(TreeableGalleryItem childItem, int childIndex, TransferInterruption previousInterruption) {
		SmugImage childImage = (SmugImage)childItem;
		if (childItem.getParent() != this) {
			com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings changeSettings
				= new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings();
			com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings.ChangeSettingsResponse response
				= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, childImage.getImageID(), apiAlbum.getID());
			if (response.isError()) {
				throw new MoveException(this, response.getError());
			}
		}
		
		reorderItem(childImage, childIndex);
		
		((SmugAlbum)childImage.getParent()).removeChild(childImage);
		images.add(childIndex, childImage);
		childImage.setParent(this);
	}

	public boolean canImport(TreeableGalleryItem newItem, int childIndex) {
		return (ItemType.IMAGE == newItem.getType());
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, int childIndex, TransferInterruption previousInterruption) throws IOException, TransferInterruption {
		byte[] imageData = null;
		String caption = null;
		if (previousInterruption != null && previousInterruption instanceof UnexpectedCaptionInterruption) {
			UnexpectedCaptionInterruption uci = (UnexpectedCaptionInterruption)previousInterruption;
			imageData = uci.getImageData();
			caption = uci.getFixedCaption();
		} else {
			InputStream sourceInputStream = newItem.getDataURL().openStream();
			try {
				imageData = IOUtils.toByteArray(sourceInputStream);
			} finally {
				sourceInputStream.close();
			}
			
			caption = newItem.getCaption();
			if (caption == null) {
				try {
					String fileName = newItem.getDataURL().getFile();
					JpegImageMetadata metadata = loadMetaData(new ByteArrayInputStream(imageData), fileName);
					if (metadata != null) {
						TiffField description = metadata.findEXIFValue(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
						if (description != null) {
							String descriptionString = description.getStringValue().trim();
							if (descriptionString.length() > 0) {
								throw new UnexpectedCaptionInterruption(imageData, fileName, descriptionString);
							}
						}
					}
				} catch (ImageReadException ex) {
					Logger.getLogger(SmugAlbum.class.getName()).log(Level.SEVERE, null, ex);
				} 
			}
		}
		
		com.kallasoft.smugmug.api.json.v1_2_0.images.UploadHTTPPut uploadMethod
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.UploadHTTPPut();
		com.kallasoft.smugmug.api.json.v1_2_0.images.UploadHTTPPut.UploadHTTPPutResponse uploadResponse
			= uploadMethod.execute(SmugMug.API_UPLOAD_URL, SmugMug.sessionID, 
									apiAlbum.getID(), null, 
									newItem.getName(), new ByteArrayInputStream(imageData),
									caption, null, 
									null, null, null);
		if (uploadResponse.isError()) {
			throw new MoveException(this, uploadResponse.getError());
		}

		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo getInfo
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo.GetInfoResponse getInfoResponse
			= getInfo.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, uploadResponse.getImageID(), uploadResponse.getImageKey());
		if (getInfoResponse.isError()) {
			throw new MoveException(this, getInfoResponse.getError());
		}
		
		SmugImage newImage = new SmugImage(this, getInfoResponse.getImage());
		reorderItem(newImage, childIndex);
		images.add(childIndex, newImage);

		return newImage;
	}
	
    public static JpegImageMetadata loadMetaData(InputStream inputStream, String fileName) {
		try {
			IImageMetadata metadata = Sanselan.getMetadata(inputStream, fileName);
			if (metadata instanceof JpegImageMetadata) {
				return (JpegImageMetadata) metadata;
			}
		} catch (ImageReadException ex) {
			Logger.getLogger(SmugAlbum.class.getName()).log(Level.WARNING, "Unable to load metadata for " + fileName);
		} catch (IOException ex) {
			Logger.getLogger(SmugAlbum.class.getName()).log(Level.WARNING, "Unable to load metadata for " + fileName);
		}
		return null;
    }
	
	private void reorderItem(SmugImage childImage, int childIndex) {
		if (childImage.getParent() == this && images.contains(childImage) && childIndex > images.indexOf(childImage)) {
			childIndex--;
		}
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition changePosition 
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition();
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition.ChangePositionResponse response
			= changePosition.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, childImage.getImageID(), childIndex + 1);
		if (response.isError()) {
			throw new ReorderException(this, response.getError());
		}
	}
	
	public Integer getAlbumID() {
		return apiAlbum.getID();
	}
	
	public Integer getPosition() {
		return apiAlbum.getPosition();
	}

	public ItemType getType() {
		return ItemType.ALBUM;
	}

	public int compareTo(TreeableGalleryItem o) {
		TreeableGalleryItem other = (TreeableGalleryItem)o;
		if (SmugMugSettings.getTreeCategorySort() && ItemType.CATEGORY == other.getType()) {
			return 1;
		}
		
		if (!SmugMugSettings.getTreeSort() && ItemType.ALBUM == other.getType()) {
			Integer leftPosition = getPosition();
			Integer rightPosition = ((SmugAlbum)other).getPosition();
			if (leftPosition != null && rightPosition != null) {
				return leftPosition.compareTo(rightPosition);
			} else {
				return 0;
			}
		}

		return toString().compareToIgnoreCase(other.toString());
	}

	@Override
	public String getMetaLabel() {
		return (hasPassword() ? " [password protected]" : "");
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isHidden() {
		return false;
	}
	
	public boolean hasPassword() {
		if ("".equals(password)) {
			return false;
		}
		return (password != null || !"".equals(apiAlbum.getPassword()));
	}

	@Override
	public boolean canChangePassword(boolean newState) {
		return true;
	}

	@Override
	public void setPassword(final String password, final String passwordHint) {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings() {
			@Override
			protected void setupPostParameters(PostMethod postMethod, String[] argumentValues) {
				super.setupPostParameters(postMethod, argumentValues);
				//have to work around stupid things the API wrapper library is doing
				if (password == null) {
					postMethod.addParameter("Password", "");
				}
				if (passwordHint == null) {
					postMethod.addParameter("PasswordHint", "");
				}
			}
		};
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings.ChangeSettingsResponse response 
			= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID(),
									null, null, null, null, null, null, null, null,
									null, null, null, null, null, null, null, null,
									null, password, passwordHint, null, null, null, null, null,
									null, null, null, null, null, null, null, null,
									null, null, null, null, null, null, null, null,
									null, null, null, null, null, null);
		if (response.isError()) {
			throw new PasswordException(this, response.getError());
		}
		this.password = (password == null ? "" : password);
	}

	public URL getDataURL() throws MalformedURLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}
}
