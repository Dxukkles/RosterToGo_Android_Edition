package com.pluszero.rostertogo.filenav;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class FileNavigator {

	public static final int MSG_OK = 0;
	public static final int MSG_NOT_ACCESSIBLE = 1;
	public static final int MSG_NOT_DIRECTORY = 2;

	private File root, actualFile;
	private File[] files;
	private List<File> listFiles;
	private TreeSet<Integer> selectedFiles;

	public FileNavigator() {
		listFiles = new ArrayList<File>();
		selectedFiles = new TreeSet<Integer>();
		root = new File("/");
		root = Environment.getExternalStorageDirectory();
		buildFileList(root);
	}

	public boolean buildFileList(File file) {

		if (file == null) {
			file = root;
		}

		// Read all files sorted into the values-array
		if (!file.canRead())
			return false;
		else {
			actualFile = file;
		}

		files = file.listFiles();
		if (files != null) {
			listFiles.clear();
			listFiles.addAll(Arrays.asList(files));
			Collections.sort(listFiles, new SortFileName());
			Collections.sort(listFiles, new SortFolder());
			return true;
		} else
			return false;
	}

	public void navigateUp() {
		buildFileList(actualFile.getParentFile());
	}

	public int navigateDown(int position) {
		File f = listFiles.get(position);
		if (f.isDirectory()) {
			if (buildFileList(f) == false)
				return MSG_NOT_ACCESSIBLE;
		} else
			return MSG_NOT_DIRECTORY;

		return MSG_OK;
	}

	public boolean createDirectory(String name) {
		File f = new File(actualFile, name);
		if (f.exists())
			return false;

		if (f.mkdir()) {
			buildFileList(actualFile);
			return true;
		}
		else
			return false;
	}

	public List<File> deleteFile() {
		// build a list holding files whom deletion has failed
		List<File> listFailed = new ArrayList<File>();

		Iterator<Integer> iterator;
		iterator = selectedFiles.descendingIterator();
		File f;
		while (iterator.hasNext()) {
			Integer integer = iterator.next();
			f = listFiles.get(integer.intValue());
			// if deletion fails, add the index to the list
			deleteRecursive(f, listFailed);
		}
		buildFileList(actualFile);
		return listFailed;
	}

	private boolean deleteRecursive(File fileOrDirectory, List<File> listFailed) {
		boolean result;
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles()) {
				result = deleteRecursive(child, listFailed);
				if (result == false) {
					listFailed.add(child);
				}
			}
		result = fileOrDirectory.delete();
		if (result == false) {
			listFailed.add(fileOrDirectory);
			return false;
		}
		return true;
	}

	public boolean addSelectedPosition(int pos) {
		return selectedFiles.add(new Integer(pos)) ? true : false;
	}

	public boolean removeSelectedPosition(int pos) {
		return selectedFiles.remove(new Integer(pos)) ? true : false;
	}

	// sorts based on the files name
	class SortFileName implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			return f1.getName().compareTo(f2.getName());
		}
	}

	// sorts based on a file or folder. folders will be listed first
	class SortFolder implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			if (f1.isDirectory() == f2.isDirectory())
				return 0;
			else if (f1.isDirectory() && !f2.isDirectory())
				return -1;
			else
				return 1;
		}
	}

	public File getActualFile() {
		return actualFile;
	}

	public void setActualFile(File actualFile) {
		this.actualFile = actualFile;
	}

	public List<File> getListFiles() {
		return listFiles;
	}
}
