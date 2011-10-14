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

package plugins.nherve.browser.cache;

import plugins.nherve.browser.cache.DBWrapper.DBType;

public class ThumbnailCacheFactory {
	private final static String CACHE_NAME = "ImageBrowser";

	private static DiskThumbnailCache diskThumbnailCache;
	private static DBThumbnailCache h2DBThumbnailCache;
	private static DBThumbnailCache derbyDBThumbnailCache;

	public static ThumbnailCache getBestCacheAvailable() throws CacheException {
		ThumbnailCache cache = null;

		try {
			cache = getH2DBCache();
		} catch (CacheException e1) {
			System.out.println("[ThumbnailCacheFactory] Unable to find H2 JDBC drivers ("+e1.getMessage()+")");
			try {
				cache = getDerbyDBCache();
			} catch (CacheException e2) {
				System.out.println("[ThumbnailCacheFactory] Unable to find Apache Derby JDBC drivers ("+e2.getMessage()+")");
				cache = getDiskCache();
			}
		}

		return cache;
	}

	public static void close(ThumbnailCache cache) throws CacheException {
		if (cache != null) {
			if (cache == diskThumbnailCache) {
				diskThumbnailCache = null;
			} else if (cache == h2DBThumbnailCache) {
				h2DBThumbnailCache = null;
			} else if (cache == derbyDBThumbnailCache) {
				derbyDBThumbnailCache = null;
			}
			cache.close();
		}
	}

	public static ThumbnailCache getDiskCache() throws CacheException {
		try {
			if (diskThumbnailCache == null) {
				diskThumbnailCache = new DiskThumbnailCache(CACHE_NAME);
				diskThumbnailCache.init();
			}
			return diskThumbnailCache;
		} catch (CacheException e) {
			diskThumbnailCache = null;
			throw e;
		}
	}

	public static ThumbnailCache getH2DBCache() throws CacheException {
		try {
			if (h2DBThumbnailCache == null) {
				h2DBThumbnailCache = new DBThumbnailCache(DBType.H2, CACHE_NAME, true);
				h2DBThumbnailCache.init();
			}
			return h2DBThumbnailCache;
		} catch (CacheException e) {
			h2DBThumbnailCache = null;
			throw e;
		}
	}

	public static ThumbnailCache getDerbyDBCache() throws CacheException {
		try {
			if (derbyDBThumbnailCache == null) {
				derbyDBThumbnailCache = new DBThumbnailCache(DBType.DERBY, CACHE_NAME, true);
				derbyDBThumbnailCache.init();
			}
			return derbyDBThumbnailCache;
		} catch (CacheException e) {
			derbyDBThumbnailCache = null;
			throw e;
		}
	}
}
