package plugins.nherve.browser.cache;

import java.awt.image.BufferedImage;

public interface ThumbnailCache {
	public void store(String s, BufferedImage bi) throws CacheException;
	public BufferedImage get(String s) throws CacheException;
	public void clear() throws CacheException;
	public void init() throws CacheException;
	public String getSizeInfo();
}
