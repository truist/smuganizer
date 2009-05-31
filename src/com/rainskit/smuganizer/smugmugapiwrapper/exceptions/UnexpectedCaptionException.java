package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.tree.transfer.TransferException;

public class UnexpectedCaptionException extends TransferException {
	private byte[] imageData;
	
	public UnexpectedCaptionException(byte[] imageData) {
		super("Image has a 'hidden' caption in the EXIF headers");
		this.imageData = imageData;
	}

}
