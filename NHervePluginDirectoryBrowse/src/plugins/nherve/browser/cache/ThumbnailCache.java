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

import java.awt.image.BufferedImage;

public interface ThumbnailCache {
	long MEGAOCTET = 1024 * 1024;
	void store(String s, BufferedImage bi) throws CacheException;
	BufferedImage get(String s) throws CacheException;
	void clear() throws CacheException;
	void init() throws CacheException;
	void close() throws CacheException;
	String getSizeInfo();
}
