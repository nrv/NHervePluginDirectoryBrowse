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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class ImageIOThumbnailProvider extends DefaultCacheAndResizeThumbnailProvider {
	public ImageIOThumbnailProvider(boolean doResize, int preferedSize) {
		super(doResize, preferedSize);
	}

	protected void populateSuffixes() {
		for (String s : ImageIO.getReaderFileSuffixes()) {
			addSupportedSuffix(s);
		}
	}

	@Override
	public BufferedImage getFullSizeImage(BrowsedImage cell) throws ThumbnailException {
		try {
			return ImageIO.read(cell.getFile());
		} catch (IOException e) {
			throw new ThumbnailException(e);
		}
	}

	@Override
	protected BufferedImage getResizedThumbnailFast(BrowsedImage cell) throws ThumbnailException {
		// return null;
		ImageReader reader = null;
		try {
			Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(cell.getSuffix());
			if (readers.hasNext()) {
				reader = readers.next();
			} else {
				return null;
			}

			ImageInputStream imageInputStream = null;
			imageInputStream = ImageIO.createImageInputStream(cell.getFile());
			reader.setInput(imageInputStream);
			ImageReadParam p = reader.getDefaultReadParam();

//			if (reader.hasThumbnails(0)) {
//				System.out.println(cell + " - hasThumbnails");
//				return reader.readThumbnail(0, 0);
//			}

			int size = 0;
			String fmt = reader.getFormatName();
			if (fmt.equalsIgnoreCase("TIFF")) {
				int lg = (int)cell.fileLength();
				size = 2 * (int)Math.sqrt(lg / 3);
			} else {
				int iw = reader.getWidth(0);
				int ih = reader.getHeight(0);
				size = Math.max(iw, ih);
			}
			
			int ratio = Math.max(1, (int) Math.floor(size / getPreferedSize()));
			
			//System.out.println(cell + " - setSourceSubsampling("+ratio+")");
			
			p.setSourceSubsampling(ratio, ratio, 0, 0);

			return reader.read(0, p);

		} catch (IOException e) {
			e.printStackTrace();
			throw new ThumbnailException(e);
		} finally {
			if (reader != null) {
				reader.dispose();
			}
		}
	}

}
