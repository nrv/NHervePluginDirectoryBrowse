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
	protected BufferedImage getFirstImage(BrowsedImage cell) throws ThumbnailException {
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
