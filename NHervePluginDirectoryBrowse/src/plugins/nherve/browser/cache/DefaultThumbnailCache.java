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

package plugins.nherve.browser.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import plugins.nherve.toolbox.HashToolbox;

public class DefaultThumbnailCache implements ThumbnailCache {
	private String name;
	private File cacheDirectory;

	public DefaultThumbnailCache(String name) {
		super();

		this.name = name;
	}

	private File cacheFile(String s) {
		return new File(cacheDirectory, HashToolbox.hashMD5(s) + ".jpg");
	}

	@Override
	public void clear() throws CacheException {
		for (File f : cacheDirectory.listFiles()) {
			f.delete();
		}
	}

	@Override
	public BufferedImage get(String s) throws CacheException {
		File f = cacheFile(s);
		if (!f.exists()) {
			return null;
		}

		try {
			return ImageIO.read(f);
		} catch (Exception e) {
			System.err.println("Unable to open cahed file for " + s + " (" + f.getName() + ") " + e.getClass().getName() + " : " + e.getMessage());
			return null;
		}
	}

	@Override
	public void init() throws CacheException {
		try {
			MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CacheException(e);
		}
		String dir = System.getProperty("java.io.tmpdir");
		cacheDirectory = new File(dir + File.separator + name + File.separator);

		cacheDirectory.mkdirs();
	}

	@Override
	public void store(String s, BufferedImage bi) throws CacheException {
		if (bi != null) {
			File f = cacheFile(s);
			try {
				ImageIO.write(bi, "JPG", f);
			} catch (Exception e) {
				System.err.println("Unable to write cached file for " + s + " (" + f.getName() + ") " + e.getClass().getName() + " : " + e.getMessage());
			}
		}
	}

	@Override
	public String getSizeInfo() {
		long s = 0;
		for (File f : cacheDirectory.listFiles()) {
			s += f.length();
		}
		int mo = (int)(s / (1024 * 1024));
		return "("+mo +" Mo)";
	}
}
