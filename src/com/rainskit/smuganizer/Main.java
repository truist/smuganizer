package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.filesystemapiwrapper.FileGallery;
import com.rainskit.smuganizer.tree.SmugTree;
import com.rainskit.smuganizer.tree.GalleryTree;
import com.rainskit.smuganizer.menu.SmugMenu;
import com.rainskit.smuganizer.galleryapiwrapper.Gallery;
import com.rainskit.smuganizer.menu.gui.GalleryLoginDialog;
import com.rainskit.smuganizer.menu.gui.SmugMugLoginDialog;
import com.rainskit.smuganizer.settings.FileSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.SmugMug;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.FileGalleryTree;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import com.rainskit.smuganizer.waitcursoreventqueue.WaitCursorEventQueue;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class Main extends JFrame implements TreeSelectionListener, StatusCallback, MouseListener, WindowListener {
	private enum Side {LEFT, RIGHT}
	
	private enum GalleryType { 
		GALLERY, SMUGMUG, COMPUTER;

		@Override
		public String toString() {
			switch (this) {
				case GALLERY:
					return "Gallery";
				case SMUGMUG:
					return "SmugMug";
				case COMPUTER:
					return "Local Computer";
				default:
					throw new IllegalArgumentException("Impossible type: " + this.name());
			}
		}
	}
	
	private SmugTree smugTree;
	private GalleryTree galleryTree;
	private FileGalleryTree fileGalleryTree;
	
	private AsynchronousTransferManager transferManager;
	
	private ImageWindow floatingImageWindow;
	
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JLabel statusLabel;
	private String baseStatus;
	
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, IOException{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitCursorEventQueue(170));
		new Main();
    }

	private Main() throws FileNotFoundException, IOException {
		super("Smuganizer");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		TransferTableModel transferTableModel = new TransferTableModel();
		transferManager = new AsynchronousTransferManager(transferTableModel);
		
		leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(newHeaderLabel(Side.LEFT), BorderLayout.NORTH);
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(newHeaderLabel(Side.RIGHT), BorderLayout.NORTH);
		
		
		leftPanel.add(new JScrollPane(createDefaultTree()));
		rightPanel.add(new JScrollPane(createDefaultTree()));
		
		JSplitPane lrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
												true, 
												leftPanel, 
												rightPanel);
		lrSplitPane.setResizeWeight(0.5);
		
		TransferTable transferTable = new TransferTable(this, transferManager, transferTableModel, true);
		JSplitPane tbSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
												true,
												lrSplitPane,
												new JScrollPane(transferTable));
		tbSplitPane.setResizeWeight(0.8);
		
		statusLabel = new JLabel(" ");
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		statusPanel.add(statusLabel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tbSplitPane, BorderLayout.CENTER);
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		setJMenuBar(new SmugMenu(this, smugTree, galleryTree, transferTable, transferManager));
		
		pack();
		setSize(800, 600);
		setLocationRelativeTo(null);

        URL imageURL = getClass().getResource("/images/camera_icon.png");
        if (imageURL != null) {
            setIconImage(new ImageIcon(imageURL, "").getImage());
        }

		floatingImageWindow = new ImageWindow(this);
		
		setVisible(true);
	}

	private JPanel newHeaderLabel(Side side) {
		JPanel headerPanel = new JPanel(new GridBagLayout());
		JPanel componentPanel = new JPanel(new FlowLayout());
		headerPanel.add(componentPanel);
		
		JLabel connectLabel = new JLabel("Connect to: ");
		componentPanel.add(connectLabel);
		
		JComboBox connectBox = new JComboBox(GalleryType.values());
		connectBox.setFont(connectBox.getFont().deriveFont(Font.BOLD));
		connectBox.setSelectedIndex(-1);
		componentPanel.add(connectBox);
		
		connectBox.addActionListener(new ConnectionChoiceListener(side));
		
		return headerPanel;
	}

	private void connectTo(GalleryType galleryType, Side side) {
		if (GalleryType.SMUGMUG.equals(galleryType)) {
			SmugMugLoginDialog settingsDialog = new SmugMugLoginDialog(this);
			settingsDialog.setVisible(true);
			if (settingsDialog.wasClosedWithOK()) {
				setStatus("Logging in...");
				try {
					loadSmugTree(new SmugMug(), side);
				} catch (SmugException se) {
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, se);
					JOptionPane.showMessageDialog(this, se.getLocalizedMessage(), "Error connecting to SmugMug", JOptionPane.ERROR_MESSAGE);
					clearStatus();
				}
			}
		} else if (GalleryType.GALLERY.equals(galleryType)) {
			GalleryLoginDialog settingsDialog = new GalleryLoginDialog(this);
			settingsDialog.setVisible(true);
			if (settingsDialog.wasClosedWithOK()) {
				setStatus("Logging in...");
				try {
					loadGalleryTree(new Gallery(), side);
				} catch (IOException ex) {
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
					JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error contacting gallery", JOptionPane.ERROR_MESSAGE);
					clearStatus();
				}
			}
		} else if (GalleryType.COMPUTER.equals(galleryType)) {
			JFileChooser directoryChooser = new JFileChooser();
			directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            
            String prevDir = FileSettings.getPreviousDirectory();
            if (prevDir != null) {
    			directoryChooser.setCurrentDirectory(new File(prevDir));
            }
			if (directoryChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File chosen = directoryChooser.getSelectedFile();
				if (chosen.exists() && chosen.isDirectory()) {
                    FileSettings.setPreviousDirectory(chosen.getPath());
					loadFileGalleryTree(new FileGallery(chosen), side);
				}
			}
		} else {
			throw new IllegalArgumentException("Impossible type: " + galleryType.name());
		}
	}
	
	private JTree createDefaultTree() {
		String[] instructions = new String[] {"Please choose a gallery from the dropdown above"};
		JTree tree = new JTree(instructions);
		tree.setShowsRootHandles(false);
		tree.setCellRenderer(new TreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				return new JLabel(value.toString());
			}
		});
		return tree;
	}

	private void loadSmugTree(SmugMug smugMug, Side side) {
		smugTree = new SmugTree(this);
		initializeTree(smugTree, side);
		try {
			smugTree.loadTree(smugMug);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadGalleryTree(Gallery gallery, Side side) {
		galleryTree = new GalleryTree(this);
		initializeTree(galleryTree, side);
		try {
			galleryTree.loadTree(gallery);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error loading albums", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadFileGalleryTree(FileGallery fileGallery, Side side) {
		fileGalleryTree = new FileGalleryTree(this);
		initializeTree(fileGalleryTree, side);
		try {
			fileGalleryTree.loadTree(fileGallery);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error loading albums", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initializeTree(JTree tree, Side side) {
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		tree.setTransferHandler(transferManager.getTransferHandler());
		
		JPanel panel;
		if (Side.LEFT.equals(side)) {
			panel = leftPanel;
		} else {
			panel = rightPanel;
		}
		for (Component each : panel.getComponents()) {
			if (each instanceof JScrollPane) {
				panel.remove(each);
			}
		}
		panel.add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	public void showImageWindow() {
		floatingImageWindow.setVisible(true);
		floatingImageWindow.toFront();
	}
	
	public void valueChanged(TreeSelectionEvent tse) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
		try {
			if (node.getUserObject() instanceof TreeableGalleryItem && ((JTree)tse.getSource()).getSelectionCount() == 1) {
				TreeableGalleryItem item = (TreeableGalleryItem)node.getUserObject();
				floatingImageWindow.displayImage(item);
			} else {
				floatingImageWindow.displayImage(null);
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void setStatus(String status) {
		synchronized (statusLabel) {
			Logger.getLogger(Main.class.getName()).log(Level.FINE, status);
			statusLabel.setText(baseStatus = status);
		}
	}
	
	public void setSubStatus(String subStatus) {
		synchronized (statusLabel) {
			Logger.getLogger(Main.class.getName()).log(Level.FINE, baseStatus + subStatus);
			statusLabel.setText(baseStatus + subStatus);
		}
	}
	
	public void clearStatus() {
		setStatus(" ");
	}
	
	
	private class ConnectionChoiceListener implements ActionListener {
		private Side side;
		
		private ConnectionChoiceListener(Side side) {
			this.side = side;
		}
		
		public void actionPerformed(final ActionEvent ae) {
			SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    connectTo((GalleryType)((JComboBox)ae.getSource()).getSelectedItem(), side);
                }
            });

		}
	}

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2) {
			JTree tree = (JTree)me.getSource();
			TreePath clickPath = tree.getPathForLocation(me.getX(), me.getY());
			if (clickPath != null) {
				TreeableGalleryItem clickItem = (TreeableGalleryItem)((DefaultMutableTreeNode)clickPath.getLastPathComponent()).getUserObject();
				if (clickItem != null && ItemType.IMAGE == clickItem.getType()) {
					TreePath selectedPath = tree.getSelectionPath();
					if (selectedPath != null) {
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
						TreeableGalleryItem selectedItem = (TreeableGalleryItem)selectedNode.getUserObject();
						if (ItemType.IMAGE == selectedItem.getType()) {
							showImageWindow();
						}
					}
				}
			}
		}
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void windowClosing(WindowEvent e) {
		if (floatingImageWindow.isVisible()) {
			floatingImageWindow.setVisible(false);
		} else {
			dispose();
		}
	}

	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}
