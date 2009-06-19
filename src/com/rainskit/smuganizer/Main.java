package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.SmugTree;
import com.rainskit.smuganizer.tree.GalleryTree;
import com.rainskit.smuganizer.menu.SmugMenu;
import com.rainskit.smuganizer.galleryapiwrapper.Gallery;
import com.rainskit.smuganizer.smugmugapiwrapper.SmugMug;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import com.rainskit.smuganizer.waitcursoreventqueue.WaitCursorEventQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class Main extends JFrame implements TreeSelectionListener, StatusCallback {
	private JLabel statusLabel;
	private String baseStatus;
	
	private SmugTree smugTree;
	private GalleryTree galleryTree;
	
	private ImageWindow floatingImageWindow;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, IOException{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitCursorEventQueue(170));
		new Main();
    }

	private Main() throws FileNotFoundException, IOException {
		super("Smuganizer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TransferTableModel transferTableModel = new TransferTableModel();
		AsynchronousTransferManager transferManager = new AsynchronousTransferManager(transferTableModel);
		smugTree = new SmugTree(this, transferManager);
		smugTree.addTreeSelectionListener(this);
		smugTree.addMouseListener(new DoubleClickListener());
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JScrollPane(smugTree), BorderLayout.CENTER);
		rightPanel.add(newHeaderLabel("SmugMug"), BorderLayout.NORTH);
		
		galleryTree = new GalleryTree(this);
		galleryTree.addTreeSelectionListener(this);
		galleryTree.addMouseListener(new DoubleClickListener());
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(new JScrollPane(galleryTree), BorderLayout.CENTER);
		leftPanel.add(newHeaderLabel("Gallery"), BorderLayout.NORTH);
		
		JSplitPane lrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
												true, 
												leftPanel, 
												rightPanel);
		lrSplitPane.setResizeWeight(0.5);
		
		TransferTable transferTable = new TransferTable(transferTableModel, true);
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
		
		floatingImageWindow = new ImageWindow(this);
		
		setVisible(true);
	}

	private JLabel newHeaderLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(font.deriveFont(Font.BOLD, font.getSize() + (font.getSize() / 2)));
		label.setForeground(Color.GREEN.darker());
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		return label;
	}

	public void showImageWindow() {
		floatingImageWindow.setVisible(true);
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
			
	public boolean loadSmugTree() {
		setStatus("Logging in to SmugMug...");
		SmugMug smugMug;
		try {
			smugMug = new SmugMug();
		} catch (SmugException se) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, se);
			JOptionPane.showMessageDialog(this, se.getLocalizedMessage(), "Error connecting to SmugMug", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			clearStatus();
		}
		try {
			smugTree.loadTree(smugMug);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
		}
		return true;
	}
	
	public boolean loadGalleryTree() {
		Gallery gallery;
		setStatus("Logging in to gallery...");
		try {
			gallery = new Gallery();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error contacting gallery", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			clearStatus();
		}
		try {
			galleryTree.loadTree(gallery);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Error loading albums", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
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
	
	
	private class DoubleClickListener extends MouseAdapter {
		@Override
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
	}
}
