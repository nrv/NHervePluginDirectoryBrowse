/*
 * Copyright 2011 Institut Pasteur.
 * Copyright 2012 Nicolas Hervé.
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

import icy.gui.frame.IcyFrameEvent;
import icy.gui.util.GuiUtil;
import icy.preferences.XMLPreferences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import plugins.nherve.browser.cache.CacheException;
import plugins.nherve.browser.viewer.ImageViewer;
import plugins.nherve.toolbox.NherveToolbox;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.GridPanel;
import plugins.nherve.toolbox.plugin.HeadlessReadyComponent;
import plugins.nherve.toolbox.plugin.HelpWindow;
import plugins.nherve.toolbox.plugin.PluginHelper;
import plugins.nherve.toolbox.plugin.SingletonPlugin;

public class ImageBrowser extends SingletonPlugin implements ActionListener, DocumentListener, HeadlessReadyComponent {
	private class InternalFileFilter implements FileFilter {
		private boolean recurse;
		private Pattern pattern;

		public InternalFileFilter(boolean recurse, Pattern pattern) {
			super();
			this.recurse = recurse;
			this.pattern = pattern;
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return recurse;
			} else {
				if (pattern != null) {
					Matcher matcher = pattern.matcher(f.getAbsolutePath());
					if (!matcher.matches()) {
						return false;
					}
				}
				return provider.isAbleToProvideThumbnailFor(f);
			}
		}
	}

	private final static String VERSION = "1.4.0.0";

	private final static String INPUT_PREFERENCES_NODE = "directory";
	private final static String FILTER = "filter";
	private final static String ZOOM = "zoom";
	private final static String CACHE = "cache";

	private static String HELP = "<html>"; 
	{
		HELP += "<p align=\"center\"><b>" + HelpWindow.getTagFullPluginName() + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.getDevNameHtml() + "</b></p>" + "<p align=\"center\"><a href=\"http://www.herve.name/pmwiki.php/Main/ImageBrowser\">Online help is available</a></p>" + "<p align=\"center\"><b>" + NherveToolbox.getCopyrightHtml() + "</b></p>" + "<hr/>";
		HELP += "<p>This plugin helps you to browse your directories with image thumbnails. It uses an internal cache to avoid the thumbnails computation each time a directory is displayed. It is highly recommended to keep the cache option enabled. If you find that the cache space used on your hard drive is too big, you can still clear it.</p>";
		HELP += "<p>The filter field can be used to enter a regular expression that will display only the files with a name that matches it. The match is case insensitive and performed on the full file path (thus also including the directories, which is sometime very usefull when used in combination with the recursive mode). For more informations on regular expressions, please refer to <a href=\"http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html\">this page</a>. Here are some examples :";
		HELP += "<center><table border='1'><tr><th>Filter</th><th>Description</th></tr>";
		HELP += "<tr><td>2012</td><td>files with 2012 in the name</td></tr>";
		HELP += "<tr><td>png$</td><td>files with a png extension</td></tr>";
		HELP += "<tr><td>2012(.*)png$</td><td>PNG files with 2012 in their names</td></tr>";
		HELP += "</table></center></p>";
		HELP += "<p>On any thumbnail displayed, you can either : ";
		HELP += "<ul><li>left click : open the image in Icy</li>";
		HELP += "<li>right click : open the image viewer that allows you to navigate quickly between the directory images with the mouse scroll</li></ul></p>";
		HELP += "<p>Take care when using the recursive mode, it may browse your full hard drive if launched from the root ! When activating this mode, you have to click on the refresh button.</p>";
		HELP += "<hr/>" + "<p>" + HelpWindow.getTagPluginName() + NherveToolbox.getLicenceHtml() + "</p>" + "<p>" + NherveToolbox.getLicenceHtmllink() + "</p>";
		HELP += "<hr/><p>Icons come from the Symbolize Icon Set published by <a href=\"http://dryicons.com\">DryIcons</a></p>";
		HELP += "</html>";
	}

	public final static String NAME_INPUT_DIR = "Browse";
	public final static String NAME_FILTER = "Filter";

	private JTextField tfInputDir;
	private JTextField tfFilterNames;

	private JButton btInputDir;
	private JButton btRefresh;
	private JButton btHelp;
	private JButton btDeleteFilter;

	private JCheckBox cbUseCache;
	private JCheckBox cbRecurse;
	private JButton btClearCache;
	private JLabel lbCache;

	private GridPanel<BrowsedImage> igp;
	private GridCellCollection<BrowsedImage> images;
	private File workingDirectory;

	private CacheThumbnailProvider provider;
	private ImageViewer viewer;

	public ImageBrowser() {
		super();
		provider = new CombinedThumbnailProvider(true, GridPanel.DEFAULT_CELL_LENGTH * (int) GridPanel.DEFAULT_MAX_ZOOM_FACTOR);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == null) {
			return;
		}

		if (o instanceof JButton) {
			JButton b = (JButton) o;

			if (b == btInputDir) {
				PluginHelper.fileChooserTF(JFileChooser.DIRECTORIES_ONLY, null, getPreferences().node(INPUT_PREFERENCES_NODE), "Choose directory to browse", tfInputDir, null);
				return;
			}

			if (b == btRefresh) {
				try {
					enableWaitingCursor();
					updateDirectoryView();
					lbCache.setText(provider.getCacheSizeInfo());
				} finally {
					disableWaitingCursor();
				}
				return;
			}

			if (b == btHelp) {
				openHelpWindow(HELP, 400, 500);
				return;
			}
			
			if (b == btDeleteFilter) {
				tfFilterNames.setText(null);
				return;
			}

			if (b == btClearCache) {
				try {
					enableWaitingCursor();
					provider.clearCache();
					lbCache.setText(provider.getCacheSizeInfo());
				} catch (CacheException e1) {
					e1.printStackTrace();
				} finally {
					disableWaitingCursor();
				}
				return;
			}
		}

	}

	@Override
	protected void beforeDisplayInterface(JPanel mainPanel) {
		super.beforeDisplayInterface(mainPanel);

		tfInputDir.getDocument().addDocumentListener(this);
		tfFilterNames.getDocument().addDocumentListener(this);
		updateDirectoryView();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	@Override
	public void fillInterface(JPanel mainPanel) {
		XMLPreferences preferences = getPreferences();

		boolean useZoom = preferences.getBoolean(ZOOM, false);
		boolean useCache = preferences.getBoolean(CACHE, true);
		boolean recursive = false;

		btRefresh = new JButton(NherveToolbox.diRefreshIcon);
		btRefresh.setToolTipText("Refresh");
		btRefresh.addActionListener(this);

		btInputDir = new JButton(NherveToolbox.diFolderIcon);
		btInputDir.setToolTipText("Browse directories");
		btInputDir.addActionListener(this);

		btClearCache = new JButton(NherveToolbox.diTrashIcon);
		btClearCache.setToolTipText("Clear cache");
		btClearCache.addActionListener(this);
		
		btDeleteFilter = new JButton(NherveToolbox.diDeleteIcon);
		btDeleteFilter.setToolTipText("Clear filter");
		btDeleteFilter.addActionListener(this);

		lbCache = new JLabel(provider.getCacheSizeInfo());

		cbUseCache = new JCheckBox("Cache");
		cbUseCache.setSelected(useCache);

		cbRecurse = new JCheckBox("Recursive");
		cbRecurse.setSelected(recursive);

		Dimension maxDim = new Dimension(65000, 25);
		Dimension minDim = new Dimension(75, 25);

		tfInputDir = new JTextField();
		tfInputDir.setToolTipText("Directory");
		tfInputDir.setPreferredSize(maxDim);
		tfInputDir.setMaximumSize(maxDim);
		tfInputDir.setMinimumSize(minDim);
		tfInputDir.setName(NAME_INPUT_DIR);
		String ifp = preferences.node(INPUT_PREFERENCES_NODE).get(PluginHelper.PATH, "");
		tfInputDir.setText(ifp);

		tfFilterNames = new JTextField();
		tfFilterNames.setToolTipText("Filter");
		tfFilterNames.setPreferredSize(maxDim);
		tfFilterNames.setMaximumSize(maxDim);
		tfFilterNames.setMinimumSize(minDim);
		tfFilterNames.setName(NAME_FILTER);
		String fnp = preferences.get(FILTER, "");
		tfFilterNames.setText(fnp);

		btHelp = new JButton(NherveToolbox.diInfoIcon);
		btHelp.setToolTipText("Informations");
		btHelp.addActionListener(this);

		mainPanel.add(GuiUtil.createLineBoxPanel(btClearCache, cbUseCache, lbCache, Box.createHorizontalGlue(), btRefresh, Box.createHorizontalGlue(), btInputDir, tfInputDir, cbRecurse, Box.createHorizontalGlue(), tfFilterNames, btDeleteFilter, Box.createHorizontalGlue(), btHelp));

		igp = new GridPanel<BrowsedImage>(useZoom);
		mainPanel.add(igp);
	}

	@Override
	public Dimension getDefaultFrameDimension() {
		return new Dimension(600, 400);
	}

	@Override
	public String getDefaultVersion() {
		return VERSION;
	}

	private List<File> getFiles(File root, boolean recurse, Pattern pattern) {

		File[] files = root.listFiles(new InternalFileFilter(recurse, pattern));
		ArrayList<File> result = new ArrayList<File>();

		if (files != null) {
			for (File f : files) {
				if (recurse && f.isDirectory()) {
					result.addAll(getFiles(f, recurse, pattern));
				} else {
					result.add(f);
				}
			}
		}

		return result;
	}

	@Override
	public void icyFrameClosed(IcyFrameEvent e) {
		if (e.getFrame() == viewer) {
			viewer = null;
		} else {
			super.icyFrameClosed(e);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	public void removeViewer() {
		viewer = null;
	}

	@Override
	public void sequenceHasChanged() {
	}

	@Override
	public void sequenceWillChange() {
	}

	public void showViewer(BrowsedImage current) {
		if (viewer == null) {
			viewer = new ImageViewer(images, provider, this);
			viewer.addFrameListener(this);
			viewer.startInterface(getFrame(), current);
		} else {
			viewer.jumpTo(current);
		}
	}

	@Override
	public void stopInterface() {
		XMLPreferences preferences = getPreferences();
		preferences.putBoolean(ZOOM, igp.isZoomOnFocus());
		preferences.putBoolean(CACHE, cbUseCache.isSelected());

		if (viewer != null) {
			viewer.close();
		}

		igp.setCells(null);
		igp = null;
		provider.close();
	}

	private void updateDirectoryView() {

		provider.setUseCache(cbUseCache.isSelected());

		if (cbUseCache.isSelected()) {
			lbCache.setText(provider.getCacheSizeInfo());
		}

		XMLPreferences preferences = getPreferences();

		preferences.putBoolean(ZOOM, igp.isZoomOnFocus());
		preferences.putBoolean(CACHE, cbUseCache.isSelected());

		workingDirectory = new File(tfInputDir.getText());
		if (workingDirectory.exists() && workingDirectory.isDirectory()) {
			tfInputDir.setBackground(Color.GREEN);
			preferences.node(INPUT_PREFERENCES_NODE).put(PluginHelper.PATH, workingDirectory.getAbsolutePath());
			List<File> files = null;	
			enableWaitingCursor();
			
			try {
				String filter = tfFilterNames.getText();
				Pattern pattern = null;
				if ((filter != null) && (filter.length() > 0)) {
					String regex = "";
					if (!filter.startsWith("^")) {
						regex += "(.*)";
					}
					regex += filter;
					if (!filter.endsWith("$")) {
						regex += "(.*)";
					}
					pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					preferences.put(FILTER, filter);
					tfFilterNames.setBackground(Color.GREEN);
				} else {
					tfFilterNames.setBackground(null);
				}
				files = getFiles(workingDirectory, cbRecurse.isSelected(), pattern);
			} catch (PatternSyntaxException e) {
				tfFilterNames.setBackground(Color.RED);
			}
			
			disableWaitingCursor();
			if ((files != null) && (files.size() > 0)) {
				images = new GridCellCollection<BrowsedImage>(provider);
				Collections.sort(files);
				for (File f : files) {
					BrowsedImage ig = new BrowsedImage(f, workingDirectory, this);
					images.add(ig);
				}
			} else {
				images = null;
			}
		} else {
			tfInputDir.setBackground(Color.RED);
			images = null;
		}

		igp.setCells(images);

		if (viewer != null) {
			viewer.close();
		}
	}

}
