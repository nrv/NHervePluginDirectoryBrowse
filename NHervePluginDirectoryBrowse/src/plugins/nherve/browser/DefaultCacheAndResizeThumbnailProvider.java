package plugins.nherve.browser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.browser.cache.DefaultThumbnailCache;
import plugins.nherve.browser.cache.ThumbnailCache;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;
import plugins.nherve.toolbox.genericgrid.DefaultThumbnailProvider;
import plugins.nherve.toolbox.image.toolboxes.SomeImageTools;

public abstract class DefaultCacheAndResizeThumbnailProvider extends DefaultThumbnailProvider<BrowsedImage> implements CacheThumbnailProvider {
	private boolean doResize;
	private int preferedSize;
	private boolean cacheReady;
	private boolean useCache;
	private ThumbnailCache cache;
	private Map<String, String> suffixes;

	public DefaultCacheAndResizeThumbnailProvider(boolean doResize, int preferedSize) {
		super();
		this.doResize = doResize;
		this.preferedSize = preferedSize;
		cacheReady = true;
		suffixes = new HashMap<String, String>();
		populateSuffixes();

		try {
			cache = new DefaultThumbnailCache("ImageBrowser");
			cache.init();
		} catch (CacheException e) {
			cacheReady = false;
			e.printStackTrace();
		}
	}

	protected void addSupportedSuffix(String s) {
		suffixes.put(s.toUpperCase(), s);
	}

	@Override
	public void clearCache() throws CacheException {
		if (useCache && cacheReady) {
			cache.clear();
		}
	}

	@Override
	public String getCacheSizeInfo() {
		return cache.getSizeInfo();
	}

	protected abstract BufferedImage getFirstImage(BrowsedImage cell) throws ThumbnailException;

	private BufferedImage getFullImage(BrowsedImage cell) throws ThumbnailException {
		BufferedImage bi = getFirstImage(cell);
		return bi;
	}

	public int getPreferedSize() {
		return preferedSize;
	}

	private BufferedImage getResizedThumbnail(BrowsedImage cell) throws ThumbnailException {
		BufferedImage bi = getResizedThumbnailFast(cell);
		if (bi == null) {
			bi = getResizedThumbnailSlow(cell);
		}
		return bi;
	}

	protected abstract BufferedImage getResizedThumbnailFast(BrowsedImage cell) throws ThumbnailException;

	private BufferedImage getResizedThumbnailSlow(BrowsedImage cell) throws ThumbnailException {
		BufferedImage bi = getFirstImage(cell);
		if (bi != null) {
			double ratio = Math.min(preferedSize / bi.getWidth(), preferedSize / bi.getHeight());
			if (ratio < 0.8) {
				bi = SomeImageTools.resize(bi, preferedSize, preferedSize);
			}
		}
		return bi;
	}

	@Override
	public BufferedImage getThumbnail(BrowsedImage cell) throws ThumbnailException {
		if (isDoResize()) {
			BufferedImage bi = null;

			if (useCache && cacheReady) {
				try {
					bi = cache.get(cell.getHashKey());
					if (bi == null) {
						bi = getResizedThumbnail(cell);
						cache.store(cell.getHashKey(), bi);
					}
				} catch (CacheException e) {
					// ignored
				}
			}

			if (bi == null) {
				bi = getResizedThumbnail(cell);
			}

			return bi;
		} else {
			return getFullImage(cell);
		}
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(BrowsedImage cell) {
		return cell.fileIsFile() && cell.fileExists() && !cell.fileIsHidden() && suffixes.containsKey(cell.getSuffix().toUpperCase());
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(File f) {
		return f.isFile() && f.exists() && !f.isHidden() && suffixes.containsKey(BrowsedImage.getSuffix(f).toUpperCase());
	}

	public boolean isDoResize() {
		return doResize;
	}

	public boolean isUseCache() {
		return useCache;
	}

	protected abstract void populateSuffixes();

	public void setDoResize(boolean doResize) {
		this.doResize = doResize;
	}

	public void setPreferedSize(int preferedSize) {
		this.preferedSize = preferedSize;
	}

	@Override
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

}
