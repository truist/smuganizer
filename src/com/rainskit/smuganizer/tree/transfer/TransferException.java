package com.rainskit.smuganizer.tree.transfer;

public abstract class TransferException extends Exception {
	public TransferException(String message) {
		super(message);
	}

	public abstract String getErrorText();
}
