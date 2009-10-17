package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;

public interface WriteableTreeableGalleryItem {
	public boolean canChangeHiddenStatus(boolean newState);
	public void setHidden(boolean hidden) throws SmugException;

	public boolean canChangePassword(boolean newState);
	public void setPassword(String password, String passwordHint) throws SmugException;
	
	public boolean canBeDeleted();
	public void delete() throws SmugException;

	public boolean canBeRelabeled();
	public void reLabel(String answer) throws SmugException;
}
