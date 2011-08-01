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
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.browser.cache.ThumbnailCache;
import plugins.nherve.browser.cache.ThumbnailCacheFactory;
import plugins.nherve.toolbox.genericgrid.DefaultThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;
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
			cache = ThumbnailCacheFactory.getBestCacheAvailable();
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
	protected void finalize() throws Throwable {
		ThumbnailCacheFactory.close(cache);
		super.finalize();
	}

	@Override
	public String getCacheSizeInfo() {
		return cache.getSizeInfo();
	}

	private BufferedImage getFullImage(BrowsedImage cell) throws ThumbnailException {
		BufferedImage bi = getFullSizeImage(cell);
		return bi;
	}

	public abstract BufferedImage getFullSizeImage(BrowsedImage cell) throws ThumbnailException;

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
		BufferedImage bi = getFullSizeImage(cell);
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
						log(cache.getClass().getName() + " is missing " + cell.getName());
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
