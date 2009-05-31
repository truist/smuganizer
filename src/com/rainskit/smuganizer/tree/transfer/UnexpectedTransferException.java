package com.rainskit.smuganizer.tree.transfer;

public class UnexpectedTransferException extends TransferException {
	private Exception cause;
	
	public UnexpectedTransferException(Exception cause) {
		super(cause.getLocalizedMessage());
		
		this.cause = cause;
	}

}
