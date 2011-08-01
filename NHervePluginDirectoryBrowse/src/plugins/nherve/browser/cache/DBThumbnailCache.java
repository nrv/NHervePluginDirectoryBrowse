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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;


import plugins.nherve.browser.cache.DBWrapper.DBType;
import plugins.nherve.toolbox.HashToolbox;

public class DBThumbnailCache implements ThumbnailCache {

	private DBWrapper wrap;

	@Override
	public void store(String s, BufferedImage bi) throws CacheException {
		try {
			wrap.insert(HashToolbox.hashMD5(s), bi);
		} catch (SQLException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public BufferedImage get(String s) throws CacheException {
		try {
			return wrap.select(s);
		} catch (SQLException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void clear() throws CacheException {
		try {
			wrap.tableClear();
		} catch (SQLException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void init() throws CacheException {
		try {
			MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CacheException(e);
		}

		try {
			wrap = DBWrapper.create(DBType.DERBY);
			wrap.setLogEnabled(true);
			wrap.connect();
		} catch (SQLException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String getSizeInfo() {
		try {
			int mo = (int)(wrap.tableSize() / MEGAOCTET);
			return "("+mo +" Mo)";
		} catch (SQLException e) {
			return "(not available)";
		}
	}

	@Override
	public void close() throws CacheException {
		try {
			if (wrap != null) {
				wrap.disconnect();
				wrap = null;
			}
		} catch (SQLException e) {
			throw new CacheException(e);
		}
	}

}
