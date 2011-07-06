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

import java.awt.image.BufferedImage;
import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.MinimalTiffReader;
import loci.formats.in.TiffReader;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class LociThumbnailProvider extends DefaultCacheAndResizeThumbnailProvider {
	public LociThumbnailProvider(boolean doResize, int preferedSize) {
		super(doResize, preferedSize);
	}
	
	protected void populateSuffixes() {
		addSupportedSuffix("tif");
		addSupportedSuffix("jpg");
		addSupportedSuffix("png");
		addSupportedSuffix("avi");
		addSupportedSuffix("lsm");
		addSupportedSuffix("tiff");
		addSupportedSuffix("jpeg");
		addSupportedSuffix("stk");
		addSupportedSuffix("zvi");
	}

	@Override
	public BufferedImage getFullSizeImage(BrowsedImage cell) throws ThumbnailException {
		BufferedImageReader reader = null;
		try {
			if (cell.getSuffix().equalsIgnoreCase("TIF") || cell.getSuffix().equalsIgnoreCase("TIFF")) {
				reader = new BufferedImageReader(new MinimalTiffReader());	
			} else {
				reader = new BufferedImageReader();
			}
			reader.setId(cell.getFile().getAbsolutePath());
			return reader.openImage(0);
		} catch (FormatException e) {
			throw new ThumbnailException(e);
		} catch (IOException e) {
			throw new ThumbnailException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	@Override
	protected BufferedImage getResizedThumbnailFast(BrowsedImage cell) throws ThumbnailException {
		return null;
	}

}
