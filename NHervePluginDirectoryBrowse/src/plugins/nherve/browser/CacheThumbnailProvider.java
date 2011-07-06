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

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.toolbox.genericgrid.ThumbnailProvider;

public interface CacheThumbnailProvider extends ThumbnailProvider<BrowsedImage> {
	public abstract void setUseCache(boolean useCache);
	public abstract String getCacheSizeInfo();
	public abstract void clearCache() throws CacheException;
}