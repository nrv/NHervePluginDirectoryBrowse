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

import icy.file.Loader;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;
import icy.gui.util.WindowPositionSaver;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import plugins.nherve.browser.BrowsedImage;
import plugins.nherve.browser.CacheThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.SomeStandardThumbnails;
import plugins.nherve.toolbox.image.toolboxes.SomeImageTools;

public class ImageViewer extends IcyFrame implements IcyFrameListener, ImagePreFetcherListener, MouseWheelListener, MouseListener, ComponentListener {
	private class View extends JComponent {
		private static final long serialVersionUID = 5470900740150032907L;

		BrowsedImage cell;
		BufferedImage image;
		BufferedImage cache;
		boolean needCacheRedraw;

		@Override
		public void paint(Graphics g) {
			if (cell != null) {
				Graphics2D g2 = (Graphics2D) g;
				if (cell.isError()) {
					SomeStandardThumbnails.paintError(g2, this);
				} else if (needCacheRedraw && (image != null)) {
					if ((image.getWidth() <= getWidth()) && (image.getHeight() <= getHeight())) {
						cache = image;
					} else {
						cache = SomeImageTools.resize(image, getWidth(), getHeight());
					}
					needCacheRedraw = false;
				}

				if (cache != null) {
					g2.drawImage(cache, (getWidth() - cache.getWidth()) / 2, (getHeight() - cache.getHeight()) / 2, null);
				}
			}
		}
	}

	private GridCellCollection<BrowsedImage> images;
	private View view;
	private ImagePreFetcher fetcher;

	public ImageViewer(GridCellCollection<BrowsedImage> images, CacheThumbnailProvider provider) {
		super();

		this.images = images;
		this.fetcher = new ImagePreFetcher(images, 5, provider);
	}

	public void startInterface(IcyFrame parentFrame, BrowsedImage first) {
		parentFrame.addFrameListener(this);

		new WindowPositionSaver(this, getClass().getName(), new Point(0, 0), new Dimension(400, 400));

		view = new View();
		view.addMouseWheelListener(this);
		view.addMouseListener(this);
		add(view);

		addFrameListener(this);
		addComponentListener(this);

		setTitle("ImageViewer");
		setResizable(true);
		setClosable(true);
		setVisible(true);
		center();
		addToMainDesktopPane();
		requestFocus();

		fetcher.addListener(this);
		fetcher.start(first);
	}

	@Override
	public void icyFrameOpened(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosing(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosed(IcyFrameEvent e) {
		if (e.getFrame() == this) {
			fetcher.stop();
		} else {
			close();
		}
	}

	@Override
	public void icyFrameIconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeiconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameActivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeactivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameInternalized(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameExternalized(IcyFrameEvent e) {
	}

	@Override
	public void notifyImageFetched(BrowsedImage image) {
		if (!image.isError() && fetcher.isCurrent(image)) {
			show(image);
		}
	}

	private void show(BrowsedImage image) {
		setTitle(image.getName());
		view.cell = image;
		view.image = fetcher.get(image);
		view.cache = null;
		view.needCacheRedraw = true;
		view.repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) {
			fetcher.moveBackward();
		} else {
			fetcher.moveForward();
		}
		show(fetcher.getCurrent());
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (view.cell != null) {
			Loader.load(view.cell.getFile());
		}
		close();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void componentHidden(ComponentEvent arg0) {

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		view.needCacheRedraw = true;
		view.repaint();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}

}
