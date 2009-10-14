package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.ExifHandler;
import com.rainskit.smuganizer.tree.transfer.interruptions.UnexpectedCaptionInterruption;
import com.rainskit.smuganizer.SmugMugSettings;
import com.rainskit.smuganizer.TransferSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
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
import org.apache.sanselan.ImageWriteException;

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
		images = new ArrayList<SmugImage>();
	}

	public SmugAlbum(SmugCategory parent, Integer albumID, String albumKey) throws SmugException {
		this(parent, reloadDetails(albumID, albumKey, parent.getFullPathLabel()));
		childrenLoaded = true;
	}

	public void setParent(SmugCategory newParent) throws SmugException {
		this.parent = newParent;
		apiAlbum = reloadDetails(apiAlbum.getID(), apiAlbum.getAlbumKey(), parent.getFullPathLabel());
	}

	private static com.kallasoft.smugmug.api.json.entity.Album reloadDetails(Integer albumID, String albumKey, String parentPathLabel) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo getInfo
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.GetInfo.GetInfoResponse response
			= getInfo.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, albumID, albumKey);
		if (response.isError()) {
			throw new SmugException("Error loading album details for album with ID \"" + albumID + "\" in category \"" + parentPathLabel + "\"", SmugException.convertError(response.getError()));
		}
		return response.getAlbum();
	}

	public List<? extends TreeableGalleryItem> loadChildren() throws IOException{
		apiAlbum = reloadDetails(apiAlbum.getID(), apiAlbum.getAlbumKey(), parent.getFullPathLabel());
		
		com.kallasoft.smugmug.api.json.v1_2_0.images.Get imageGet = new com.kallasoft.smugmug.api.json.v1_2_0.images.Get();
		com.kallasoft.smugmug.api.json.v1_2_0.images.Get.GetResponse imageResponse 
			= imageGet.execute(SmugMug.API_URL, 
								SmugMug.API_KEY, 
								SmugMug.sessionID, 
								apiAlbum.getID(), 
								apiAlbum.getAlbumKey(), 
								Boolean.FALSE);
		if (imageResponse.isError() && imageResponse.getError().getCode().intValue() != NO_IMAGES_FOUND_ERROR) {
			throw new SmugException("Failed to get images from album \"" + getFullPathLabel() + "\"", SmugException.convertError(imageResponse.getError()));
		}
		
		for (com.kallasoft.smugmug.api.json.entity.Image each : imageResponse.getImageList()) {
			images.add(new SmugImage(this, each));
		}
		childrenLoaded = true;
		return images;
	}
	
	public List<? extends TreeableGalleryItem> getChildren() {
		return images;
	}
	
	public List<SmugImage> getImages() throws IOException {
		if (!childrenLoaded) {
			loadChildren();
		}
		return images;
	}

	public String getLabel() {
		return getFileName();
	}

	public String getFileName() {
		return (reName != null) ? reName : apiAlbum.getTitle();
	}
	
	public String getURLName() {
		return getFileName();
	}
	
	public String getCaption() {
		return null;
	}
	
	public String getDescription() {
		return apiAlbum.getDescription();
	}

	public boolean canBeRelabeled() {
		return true;
	}

	public void reLabel(String newName) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings.ChangeSettingsResponse response 
			= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID(), newName, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

		if (response.isError()) {
			throw new SmugException("Error renaming " + getFullPathLabel() + " to " + newName, SmugException.convertError(response.getError()));
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

	public void delete() throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete delete 
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Delete.DeleteResponse response 
			= delete.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiAlbum.getID());
		if (response.isError()) {
			throw new SmugException("Error deleting " + getFullPathLabel(), SmugException.convertError(response.getError()));
		}
		parent.childRemoved(this);
	}

	public void childRemoved(TreeableGalleryItem image) {
		images.remove(image);
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return (ItemType.IMAGE == newChild.getType());
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, TransferInterruption previousInterruption) throws SmugException {
		SmugImage childImage = (SmugImage)childItem;
		if (childItem.getParent() != this) {
			com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings changeSettings
				= new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings();
			com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings.ChangeSettingsResponse response
				= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, childImage.getImageID(), apiAlbum.getID());
			if (response.isError()) {
				throw new SmugException("Error moving " + getFullPathLabel(), SmugException.convertError(response.getError()));
			}
			((SmugAlbum)childImage.getParent()).childRemoved(childImage);
			images.add(childImage);
			childImage.setParent(this);
		} 
		
		reorderItem(childImage, childIndex);
	}

	public boolean canImport(TreeableGalleryItem newItem) {
		return (ItemType.IMAGE == newItem.getType());
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, TransferInterruption previousInterruption) throws IOException, TransferInterruption {
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
					String fileName = newItem.getFileName();
					String description = ExifHandler.getExifDescription(imageData, fileName);
					if (description != null && description.length() > 0) {
						if (TransferSettings.getRemoveExifDescriptions()) {
							imageData = ExifHandler.removeExifDescription(imageData, fileName);
						} else if (!TransferSettings.getIgnoreExifDescriptions()) {
							throw new UnexpectedCaptionInterruption(imageData, fileName, description);
						}
					}
				} catch (ImageWriteException ex) {
					Logger.getLogger(SmugAlbum.class.getName()).log(Level.SEVERE, null, ex);
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
									newItem.getFileName(), new ByteArrayInputStream(imageData),
									caption, null, 
									null, null, null);
		if (uploadResponse.isError()) {
			throw new SmugException("Error moving " + getFullPathLabel(), SmugException.convertError(uploadResponse.getError()));
		}

		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo getInfo
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo.GetInfoResponse getInfoResponse
			= getInfo.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, uploadResponse.getImageID(), uploadResponse.getImageKey());
		if (getInfoResponse.isError()) {
			throw new SmugException("Error moving " + getFullPathLabel(), SmugException.convertError(getInfoResponse.getError()));
		}
		
		SmugImage newImage = new SmugImage(this, getInfoResponse.getImage());
		images.add(newImage);
		
		return newImage;
	}
	
	private void reorderItem(SmugImage childImage, int childIndex) throws SmugException {
		reorderItemInSmugMug(childImage, childIndex);
		
		boolean movingTowardEnd = (images.indexOf(childImage) < childIndex);
		images.remove(childImage);
		images.add(childIndex - (movingTowardEnd ? 1 : 0), childImage);
		
		//ok, at this point, if we just moved an image to position '0', smugmug
		//actually moved it to position '1', and there's no way around it.  so 
		//to fix smugmug, we take the image that we think is in position '1'
		//(but that smugmug actually has in position '0') and move it to
		//position '1' in smugmug, which sets everything right
		if (childIndex == 0 && images.size() > 1) {
			reorderItemInSmugMug(images.get(1), 1);
		}
	}
	
	private void reorderItemInSmugMug(SmugImage childImage, int childIndex) throws SmugException {
		boolean movingTowardEnd = (images.indexOf(childImage) < childIndex);
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition changePosition 
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition();
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangePosition.ChangePositionResponse response
			= changePosition.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, 
										childImage.getImageID(), childIndex + (movingTowardEnd ? 0 : 1));
		if (response.isError()) {
			throw new SmugException("Error changing position of " + getFullPathLabel(), SmugException.convertError(response.getError()));
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
	public void setPassword(final String password, final String passwordHint) throws SmugException {
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
			throw new SmugException("Error changing password for " + getFullPathLabel(), SmugException.convertError(response.getError()));
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
