/*
 * Copyright 2011 Institut Pasteur.
 * 
 * This file is part of Image Browser, which is an ICY plugin.
 * 
 * Image Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Image Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Image Browser. If not, see <http://www.gnu.org/licenses/>.
 */

package plugins.nherve.browser;

import icy.file.Loader;

import java.awt.event.MouseEvent;
import java.io.File;

import plugins.nherve.toolbox.genericgrid.GridCell;

public class BrowsedImage extends GridCell {
	private static final long serialVersionUID = 2173763087509426592L;

	private File file;
	private String suffix;
	private ImageBrowser browser;

	public BrowsedImage(File f, File p, ImageBrowser browser) {
		super(f.getAbsolutePath().substring(p.getAbsolutePath().length() + 1));
		this.browser = browser;
		this.file = f;
		this.suffix = getSuffix(file);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			Loader.load(file);
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			browser.showViewer(this);
			return;
		}
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
		return "BrowsedImage(" + getName() + ")";
	}
}
