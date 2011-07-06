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

package plugins.nherve.browser.viewer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import plugins.nherve.browser.BrowsedImage;
import plugins.nherve.browser.CacheThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class ImagePreFetcher {
	private class Fetcher implements Runnable {
		private Thread t;
		private boolean running;
		private BlockingQueue<BrowsedImage> queue;

		public Fetcher() {
			super();

			queue = new LinkedBlockingQueue<BrowsedImage>();
		}

		@Override
		public void run() {
			System.out.println("Fetcher started !");
			while (running) {
				try {
					BrowsedImage image = queue.take();
					preFetch(image);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			System.out.println("Fetcher stoped !");
		}

		private void preFetch(BrowsedImage image) {
			System.out.println("preFetch(" + image + ")");
			try {
				BufferedImage bi = provider.getFullSizeImage(image);
				cache.put(image, bi);
			} catch (ThumbnailException e) {
				image.setError(true);
				e.printStackTrace();
			}
			notifyListeners(image);
		}

		public void startFetcher() {
			running = true;
			t = new Thread(this);
			t.start();
		}

		public void stopFetcher() {
			running = false;
			if (t != null) {
				t.interrupt();
			}
			t = null;
		}

		public boolean addToQueue(BrowsedImage e) {
			return queue.add(e);
		}

		public void clearQueue() {
			queue.clear();
		}
	}

	private GridCellCollection<BrowsedImage> images;
	private int preFetchSize;
	private int currentPos;
	private int firstPos;
	private int lastPos;
	private int fetchBeginPos;
	private int fetchEndPos;

	private Map<BrowsedImage, BufferedImage> cache;
	private final CacheThumbnailProvider provider;
	private Fetcher fetcher;
	private List<ImagePreFetcherListener> listeners;

	public ImagePreFetcher(GridCellCollection<BrowsedImage> images, int preFetchSize, CacheThumbnailProvider provider) {
		super();

		listeners = new ArrayList<ImagePreFetcherListener>();
		cache = Collections.synchronizedMap(new HashMap<BrowsedImage, BufferedImage>());

		fetcher = new Fetcher();
		fetcher.startFetcher();

		this.provider = provider;
		this.preFetchSize = preFetchSize;
		this.images = images;

		firstPos = 0;
		lastPos = images.size() - 1;
	}

	private synchronized void updatePosition(boolean forward) {
		fetcher.clearQueue();

		Map<BrowsedImage, Boolean> toFetchMap = new HashMap<BrowsedImage, Boolean>();
		List<BrowsedImage> toFetchList = new ArrayList<BrowsedImage>();

		if (forward) {
			fetchBeginPos = Math.max(firstPos, currentPos - 1);
			fetchEndPos = Math.min(lastPos, fetchBeginPos + preFetchSize);
			fetchBeginPos = Math.max(firstPos, fetchEndPos - preFetchSize - 1);

			for (int i = currentPos; i <= fetchEndPos; i++) {
				BrowsedImage img = images.get(i);
				toFetchMap.put(img, true);
				toFetchList.add(img);
			}
			for (int i = currentPos - 1; i >= fetchBeginPos; i--) {
				BrowsedImage img = images.get(i);
				toFetchMap.put(img, true);
				toFetchList.add(img);
			}
		} else {
			fetchBeginPos = Math.max(firstPos, currentPos - preFetchSize);
			fetchEndPos = Math.min(lastPos, fetchBeginPos + preFetchSize + 1);
			fetchBeginPos = Math.max(firstPos, fetchEndPos - preFetchSize - 1);

			for (int i = currentPos; i >= fetchBeginPos; i--) {
				BrowsedImage img = images.get(i);
				toFetchMap.put(img, true);
				toFetchList.add(img);
			}
			for (int i = currentPos + 1; i <= fetchEndPos; i++) {
				BrowsedImage img = images.get(i);
				toFetchMap.put(img, true);
				toFetchList.add(img);
			}
		}

		List<BrowsedImage> toRemoveFromCache = new ArrayList<BrowsedImage>();
		for (BrowsedImage i : cache.keySet()) {
			if (toFetchMap.containsKey(i)) {
				toFetchMap.put(i, false);
			} else {
				toRemoveFromCache.add(i);
			}
		}
		
		for (BrowsedImage i : toRemoveFromCache) {
			cache.remove(i);
		}

		for (BrowsedImage i : toFetchList) {
			if (toFetchMap.get(i)) {
				fetcher.addToQueue(i);
			}
		}
	}

	public void start(BrowsedImage first) {
		currentPos = images.indexOf(first);

		updatePosition(true);
	}

	public void moveForward() {
		System.out.println("moveForward()");
		if (currentPos < lastPos) {
			currentPos++;
			updatePosition(true);
		}
	}

	public void moveBackward() {
		System.out.println("moveBackward()");
		if (currentPos > firstPos) {
			currentPos--;
			updatePosition(false);
		}
	}

	private void notifyListeners(BrowsedImage image) {
		for (ImagePreFetcherListener l : listeners) {
			l.notifyImageFetched(image);
		}
	}

	public boolean addListener(ImagePreFetcherListener e) {
		return listeners.add(e);
	}

	public boolean removeListener(ImagePreFetcherListener o) {
		return listeners.remove(o);
	}

	public BufferedImage get(BrowsedImage i) {
		return cache.get(i);
	}

	public boolean isCurrent(BrowsedImage i) {
		return currentPos == images.indexOf(i);
	}

	public BrowsedImage getCurrent() {
		return images.get(currentPos);
	}

	public void stop() {
		if (fetcher != null) {
			fetcher.stopFetcher();
		}
	}
}
