package plugins.nherve.browser;

import java.awt.image.BufferedImage;
import java.io.File;

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.toolbox.genericgrid.GridPanel;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class CombinedThumbnailProvider implements CacheThumbnailProvider {
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


}
