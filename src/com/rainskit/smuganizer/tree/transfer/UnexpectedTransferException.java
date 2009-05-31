package com.rainskit.smuganizer.tree.transfer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UnexpectedTransferException extends TransferException {
	private Exception cause;
	
	public UnexpectedTransferException(Exception cause) {
		super("An unexpected error occurred: " + cause.getMessage());
		
		this.cause = cause;
	}

	@Override
	public String getErrorText() {
		String errorText = "An unexpected error occurred. The description of the original error is below, followed by a list of actions the system was attempting to take when the error occurred.\n\n";
		StringWriter stackTraceWriter = new StringWriter();
		cause.printStackTrace(new PrintWriter(stackTraceWriter));
		return errorText + stackTraceWriter.toString();
	}
}
