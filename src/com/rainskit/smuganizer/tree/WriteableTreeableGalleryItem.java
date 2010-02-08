package com.rainskit.smuganizer.tree;

import java.io.IOException;

public interface WriteableTreeableGalleryItem {
	public boolean canChangeHiddenStatus(boolean newState) throws IOException;
	public void setHidden(boolean hidden) throws IOException;

	public boolean canChangePassword(boolean newState) throws IOException;
	public void setPassword(String password, String passwordHint) throws IOException;
	
	public boolean canBeDeleted() throws IOException;
	public void delete() throws IOException;

	public boolean canBeRelabeled() throws IOException;
	public void reLabel(String answer) throws IOException;
}
