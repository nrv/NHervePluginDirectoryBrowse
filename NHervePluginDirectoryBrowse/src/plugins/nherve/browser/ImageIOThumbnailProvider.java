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
	protected BufferedImage getFirstImage(BrowsedImage cell) throws ThumbnailException {
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
