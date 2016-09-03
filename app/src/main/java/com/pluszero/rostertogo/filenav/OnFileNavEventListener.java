package com.pluszero.rostertogo.filenav;

import java.io.File;

public interface OnFileNavEventListener {
	
	public void onFilenavItemSelected(File file);

	public void onFileNavSave(String path, String name);
}
