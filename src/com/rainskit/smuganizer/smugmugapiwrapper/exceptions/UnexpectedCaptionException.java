package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.tree.transfer.TransferException;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

public class UnexpectedCaptionException extends TransferException {
	private byte[] imageData;
	private String hiddenCaption;
	
	public UnexpectedCaptionException(byte[] imageData, String hiddenCaption) {
		super("Image has a 'hidden' caption in the EXIF headers");
		
		this.imageData = imageData;
		this.hiddenCaption = hiddenCaption;
	}

	@Override
	public String getErrorText() {
		StringBuffer errorText = new StringBuffer();
		errorText.append("This image has metadata (EXIF tags) set, including the \"");
		errorText.append(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION.name);
		errorText.append("\" field.");
		errorText.append("When this field is set, and no other caption is provided by the user, ");
		errorText.append("SmugMug automatically uses this field as the caption.  Some cameras ");
		errorText.append("set this field automatically to be the name of the camera, ");
		errorText.append("which is probably not the caption you want.\n\n");
		errorText.append("The field is currently set to: ").append(hiddenCaption);
		return errorText.toString();
	}
}
