package plugins.nherve.browser;

import icy.file.Loader;

import java.awt.event.MouseEvent;
import java.io.File;

import plugins.nherve.toolbox.genericgrid.GridCell;


public class BrowsedImage extends GridCell {
	private static final long serialVersionUID = 2173763087509426592L;

	private File file;
	private String suffix;
	
	public BrowsedImage(File f) {
		super(f.getName());
		file = f;
		suffix = getSuffix(file);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Loader.load(file);
	}

	public File getFile() {
		return file;
	}
	
	public String getHashKey() {
		return file.getAbsolutePath() + "$$$" + Long.toString(file.lastModified());
	}
	
	public static String getSuffix(File file) {
		String n = file.getName();
		n = n.substring(n.lastIndexOf(".") + 1);
		return n;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean fileExists() {
		return file.exists();
	}

	public boolean fileIsFile() {
		return file.isFile();
	}

	public boolean fileIsHidden() {
		return file.isHidden();
	}

	public long fileLength() {
		return file.length();
	}

	@Override
	public String toString() {
		return "BrowsedImage("+getName()+")";
	}
}
