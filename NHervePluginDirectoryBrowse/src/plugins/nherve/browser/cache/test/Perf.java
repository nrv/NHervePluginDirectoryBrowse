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

package plugins.nherve.browser.cache.test;

import icy.system.profile.CPUMonitor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import plugins.nherve.browser.CacheThumbnailProvider;
import plugins.nherve.browser.ImageIOThumbnailProvider;
import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.browser.cache.DBThumbnailCache;
import plugins.nherve.browser.cache.DiskThumbnailCache;
import plugins.nherve.browser.cache.ThumbnailCache;
import plugins.nherve.browser.cache.DBWrapper.DBType;
import plugins.nherve.browser.cache.ThumbnailCacheFactory;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class Perf {
	public static void main(String[] args) {
		//File dir = new File("D:/install/jpg1.tar/full/");
		File dir = new File("/Users/nherve/Travail/subsets/3/");
		
		Perf perf = new Perf();
		//perf.createDataset(dir);
		perf.start(dir);

		System.exit(0); // AWT threads
	}

	private void start(File dir) {
		ThumbnailCache cache = null;
		try {
			//cache = getDefaultThumbnailCache();
			cache = getDBThumbnailCache();

			log("Working with " + cache.getClass().getName());
			log("Size info : " + cache.getSizeInfo());

			for (int run = 1; run <= 10; run++) {
				long t = test(cache, dir);
				log("Run " + run + " : " + t);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}

		} catch (CacheException e) {
			e.printStackTrace();
		}
		try {
			if (cache != null) {
				cache.close();
			}
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}

	private ThumbnailCache getDefaultThumbnailCache() throws CacheException {
		ThumbnailCache cache = ThumbnailCacheFactory.getDiskCache();
		cache.init();
		return cache;
	}

	private ThumbnailCache getDBThumbnailCache() throws CacheException {
		ThumbnailCache cache = new DBThumbnailCache(DBType.H2, "Perf", true);
		cache.init();
		return cache;
	}

	private long test(ThumbnailCache cache, File dir) {
		CPUMonitor cpu = new CPUMonitor(CPUMonitor.MONITOR_ALL_THREAD_FINELY);
		cpu.start();
		try {
			for (File f : getFiles(dir)) {
				PerfBrowsedImage img = new PerfBrowsedImage(f, dir);
				BufferedImage bi = cache.get(img.getHashKey());
			}
		} catch (CacheException e) {
			e.printStackTrace();
		}
		cpu.stop();
		return cpu.getUserElapsedTimeMilli();
	}

	private File[] getFiles(File dir) {
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toUpperCase().endsWith(".JPG");
			}
		});
	}

	private void log(String msg) {
		System.out.println("[Perf] " + msg);
	}

	private void createDataset(File dir) {
		ThumbnailCache cache1 = null;
		ThumbnailCache cache2 = null;
		try {
			cache1 = getDefaultThumbnailCache();
			cache1.clear();

			cache2 = getDBThumbnailCache();
			cache2.clear();

			CacheThumbnailProvider thumbProv = new ImageIOThumbnailProvider(true, 300);
			File[] files = getFiles(dir);
			int cnt = 0;
			int sz = files.length;
			for (File f : getFiles(dir)) {
				cnt++;
				PerfBrowsedImage img = new PerfBrowsedImage(f, dir);
				BufferedImage bi = thumbProv.getThumbnail(img);
				cache1.store(img.getHashKey(), bi);
				cache2.store(img.getHashKey(), bi);
				log("File stored ("+cnt+" / "+sz+") : " + img.getName());
				
			}
		} catch (CacheException e) {
			e.printStackTrace();
		} catch (ThumbnailException e) {
			e.printStackTrace();
		} finally {
			try {
				if (cache1 != null) {
					cache1.close();
				}
			} catch (CacheException e) {
				e.printStackTrace();
			}
			try {
				if (cache2 != null) {
					cache2.close();
				}
			} catch (CacheException e) {
				e.printStackTrace();
			}
		}
	}
}
