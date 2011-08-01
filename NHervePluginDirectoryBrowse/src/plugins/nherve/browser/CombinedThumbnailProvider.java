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

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.genericgrid.GridPanel;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class CombinedThumbnailProvider extends Algorithm implements CacheThumbnailProvider {
	private CacheThumbnailProvider providerLoci;
	private CacheThumbnailProvider providerImageIO;
	
	public CombinedThumbnailProvider(boolean doResize, int preferedSize) {
		super();
		providerLoci = new LociThumbnailProvider(true, GridPanel.DEFAULT_CELL_LENGTH * (int)GridPanel.DEFAULT_MAX_ZOOM_FACTOR);
		providerImageIO = new ImageIOThumbnailProvider(true, GridPanel.DEFAULT_CELL_LENGTH * (int)GridPanel.DEFAULT_MAX_ZOOM_FACTOR);
	}
	


	@Override
	public void close() {
		providerImageIO.close();
		providerLoci.close();
	}


	private CacheThumbnailProvider switchCell(BrowsedImage cell) {
		if (cell.getSuffix().equalsIgnoreCase("JPG") || cell.getSuffix().equalsIgnoreCase("JPEG")) {
			return providerImageIO;
		}
		return providerLoci;
	}

	@Override
	public void createCacheFor(BrowsedImage cell) {
		switchCell(cell).createCacheFor(cell);
	}



	@Override
	public BufferedImage getThumbnail(BrowsedImage cell) throws ThumbnailException {
		return switchCell(cell).getThumbnail(cell);
	}



	@Override
	public boolean isAbleToProvideThumbnailFor(BrowsedImage cell) {
		return providerLoci.isAbleToProvideThumbnailFor(cell) || providerImageIO.isAbleToProvideThumbnailFor(cell);
	}



	@Override
	public boolean isAbleToProvideThumbnailFor(File f) {
		return providerLoci.isAbleToProvideThumbnailFor(f) || providerImageIO.isAbleToProvideThumbnailFor(f);
	}



	@Override
	public void provideThumbnailFor(BrowsedImage cell) {
		switchCell(cell).provideThumbnailFor(cell);
	}



	@Override
	public void stopCurrentWork() {
		providerLoci.stopCurrentWork();
		providerImageIO.stopCurrentWork();
	}



	public void setUseCache(boolean useCache) {
		providerLoci.setUseCache(useCache);
		providerImageIO.setUseCache(useCache);
	}



	public String getCacheSizeInfo() {
		return providerLoci.getCacheSizeInfo();
	}



	public void clearCache() throws CacheException {
		providerLoci.clearCache();
	}



	@Override
	public BufferedImage getFullSizeImage(BrowsedImage cell) throws ThumbnailException {
		return switchCell(cell).getFullSizeImage(cell);
	}



	@Override
	public void setLogEnabled(boolean log) {
		super.setLogEnabled(log);
		if (providerImageIO != null) {
			providerImageIO.setLogEnabled(log);
		}
		if (providerLoci != null) {
			providerLoci.setLogEnabled(log);
		}
	}


}
