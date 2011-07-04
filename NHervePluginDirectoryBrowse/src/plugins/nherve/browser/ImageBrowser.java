package plugins.nherve.browser;

import icy.gui.component.ComponentUtil;
import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.util.WindowPositionSaver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.prefs.Preferences;

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
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.GridPanel;
import plugins.nherve.toolbox.plugin.PluginHelper;
import plugins.nherve.toolbox.plugin.SingletonPlugin;

public class ImageBrowser extends SingletonPlugin implements ActionListener, DocumentListener {
	private class InternalFileFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			return provider.isAbleToProvideThumbnailFor(f);
		}
	}

	private final static String PLUGIN_NAME = "ImageBrowser";

	private final static String PLUGIN_VERSION = "1.0.0";

	private final static String FULL_PLUGIN_NAME = PLUGIN_NAME + " V" + PLUGIN_VERSION;
	private final static String PREFERENCES_NODE = "icy/plugins/nherve/browser/ImageBrowser";
	private final static String INPUT_PREFERENCES_NODE = PREFERENCES_NODE + "/directory";
	public final static String NAME_INPUT_DIR = "Browse";
	private IcyFrame frame;

	private JTextField tfInputDir;

	private JButton btInputDir;
	private JButton btRefresh;
	
//  private JRadioButton rbLoci;
//	private JRadioButton rbImageIO;
	
	private JCheckBox cbUseCache;
	private JButton btClearCache;
	private JLabel lbCache;
	
	private GridPanel<BrowsedImage> igp;
	private File workingDirectory;

	private CacheThumbnailProvider provider;
	
	public ImageBrowser() {
		super();
		provider = new CombinedThumbnailProvider(true, GridPanel.DEFAULT_CELL_LENGTH * (int)GridPanel.DEFAULT_MAX_ZOOM_FACTOR);
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
				PluginHelper.fileChooser(JFileChooser.DIRECTORIES_ONLY, null, INPUT_PREFERENCES_NODE, "Choose directory to browse", tfInputDir);
			}
			
			if (b == btRefresh) {
				updateDirectoryView();
			}
			
			if (b == btClearCache) {
				try {
					provider.clearCache();
					lbCache.setText(provider.getCacheSizeInfo());
				} catch (CacheException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateDirectoryView();
	}

	@Override
	public void sequenceHasChanged() {
	}

	@Override
	public void sequenceWillChange() {
	}

	@Override
	public void startInterface() {
		JPanel mainPanel = GuiUtil.generatePanel();
		frame = GuiUtil.generateTitleFrame(FULL_PLUGIN_NAME, mainPanel, new Dimension(400, 100), true, true, true, true);
		
		addIcyFrame(frame);
		
		new WindowPositionSaver(frame, PREFERENCES_NODE, new Point(0, 0), new Dimension(400, 400));

		
//		ButtonGroup bg = new ButtonGroup();
//		rbLoci = new JRadioButton("Loci");
//		bg.add(rbLoci);
//		rbImageIO = new JRadioButton("ImageIO");
//		bg.add(rbImageIO);
//		rbLoci.setSelected(true);
		
		btRefresh = new JButton("Refresh");
		btRefresh.addActionListener(this);
		
		btInputDir = new JButton(NAME_INPUT_DIR);
		btInputDir.addActionListener(this);
		
		btClearCache = new JButton("Clear cache");
		btClearCache.addActionListener(this);
		
		lbCache = new JLabel(provider.getCacheSizeInfo());
		
		cbUseCache = new JCheckBox("Use cache");
		cbUseCache.setSelected(true);

		tfInputDir = new JTextField();
		tfInputDir.setName(NAME_INPUT_DIR);
		String ifp = Preferences.userRoot().node(INPUT_PREFERENCES_NODE).get(PluginHelper.PATH, "");
		tfInputDir.setText(ifp);
		ComponentUtil.setFixedHeight(tfInputDir, 25);
		mainPanel.add(GuiUtil.createLineBoxPanel(/*rbLoci, rbImageIO, Box.createHorizontalGlue(), */cbUseCache, lbCache, btClearCache, Box.createHorizontalGlue(), btRefresh, Box.createHorizontalGlue(), btInputDir, tfInputDir));

		igp = new GridPanel<BrowsedImage>();
		mainPanel.add(igp);

		frame.setVisible(true);
		frame.addFrameListener(this);
		frame.pack();
		

		tfInputDir.getDocument().addDocumentListener(this);
		updateDirectoryView();

		frame.requestFocus();
	}

	@Override
	public void stopInterface() {
		frame.removeAll();
		frame = null;
		igp.setCells(null);
		igp = null;
//		providerImageIO.close();
//		providerLoci.close();
		provider.close();
	}

	private void updateDirectoryView() {
//		if (rbLoci.isSelected()) {
//			provider = providerLoci;
//		} else {
//			provider = providerImageIO;
//		}
		provider.setUseCache(cbUseCache.isSelected());
		
		if (cbUseCache.isSelected()) {
			lbCache.setText(provider.getCacheSizeInfo());
		}
		
		workingDirectory = new File(tfInputDir.getText());
		if (workingDirectory.exists() && workingDirectory.isDirectory()) {
			tfInputDir.setBackground(Color.GREEN);
			Preferences.userRoot().node(INPUT_PREFERENCES_NODE).put(PluginHelper.PATH, workingDirectory.getAbsolutePath());
			File[] files = workingDirectory.listFiles(new InternalFileFilter());
			if (files.length > 0) {
				GridCellCollection<BrowsedImage> images = new GridCellCollection<BrowsedImage>(provider);
				Arrays.sort(files);
				for (File f : files) {
					BrowsedImage ig = new BrowsedImage(f);
					images.add(ig);
				}

				igp.setCells(images);
			} else {
				igp.setCells(null);
			}
		} else {
			tfInputDir.setBackground(Color.RED);
			igp.setCells(null);
		}
	}



}
