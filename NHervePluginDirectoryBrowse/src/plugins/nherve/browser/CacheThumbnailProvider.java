package plugins.nherve.browser;

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.toolbox.genericgrid.ThumbnailProvider;

public interface CacheThumbnailProvider extends ThumbnailProvider<BrowsedImage> {
	public abstract void setUseCache(boolean useCache);
	public abstract String getCacheSizeInfo();
	public abstract void clearCache() throws CacheException;
}