/*
 * Created on 18-Jun-2008
 * Copyright (C) 2008 by Andrea Vacondio.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; 
 * either version 2 of the License.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.pdfsam.guiclient.commons.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
import org.pdfsam.guiclient.business.listeners.EnterDoClickListener;
import org.pdfsam.guiclient.commons.business.PagesWorker;
import org.pdfsam.guiclient.commons.business.listeners.VisualPdfSelectionActionListener;
import org.pdfsam.guiclient.commons.business.listeners.adapters.VisualPdfSelectionKeyAdapter;
import org.pdfsam.guiclient.commons.business.listeners.adapters.VisualPdfSelectionMouseAdapter;
import org.pdfsam.guiclient.commons.business.listeners.mediators.PagesActionsMediator;
import org.pdfsam.guiclient.commons.business.loaders.PdfThumbnailsLoader;
import org.pdfsam.guiclient.commons.components.JVisualSelectionList;
import org.pdfsam.guiclient.commons.dnd.droppers.JVisualSelectionListDropper;
import org.pdfsam.guiclient.commons.dnd.handlers.VisualSelectionListTransferHandler;
import org.pdfsam.guiclient.commons.models.VisualListModel;
import org.pdfsam.guiclient.commons.renderers.VisualListRenderer;
import org.pdfsam.guiclient.configuration.Configuration;
import org.pdfsam.guiclient.dto.VisualPageListItem;
import org.pdfsam.i18n.GettextResource;
/**
 * Customizable panel for a visual page selection
 * @author Andrea Vacondio
 *
 */
/**
 * @author Andrea Vacondio
 *
 */
public class JVisualPdfPageSelectionPanel extends JPanel {

	private static final long serialVersionUID = -9119425032984495971L;

	private static final Logger log = Logger.getLogger(JVisualPdfPageSelectionPanel.class.getPackage().getName());
	
	public static final int HORIZONTAL_ORIENTATION = 1;
	public static final int VERTICAL_ORIENTATION = 2;
	

	
	private int orientation = HORIZONTAL_ORIENTATION;
	private File selectedPdfDocument = null;
	private String selectedPdfDocumentPassword = null;
	private boolean showButtonPanel = true;
	private boolean showTopPanel = true;
	private boolean minimalTopPanel = false;
	private boolean acceptDropFromDifferentComponents = true;
	private boolean showContextMenu = true;
	
	/**
	 * if true deleted items appear with a red cross over 
	 */
	private boolean drawDeletedItems = true;
	//if the JList uses wrap
	private boolean wrap = false;
	
	
	private Configuration config;
	private PagesWorker pagesWorker;
    //menu
	private final JMenuBar optionsMenu = new JMenuBar();
    private final JMenu menuOptions = new JMenu();
	private final JMenuItem loadDocItem = new JMenuItem();
	private final JMenuItem clearItem = new JMenuItem();
	private final JMenuItem zoomInItem = new JMenuItem();
	private final JMenuItem zoomOutItem = new JMenuItem();
	private final JButton loadFileButton = new JButton();
	
    private final JLabel documentProperties = new JLabel();    
    private final JVisualSelectionList thumbnailList = new JVisualSelectionList();
    private DropTarget scrollPanelDropTarget;
    private PdfThumbnailsLoader pdfLoader;
    private VisualPdfSelectionActionListener pdfSelectionActionListener;
    private PagesActionsMediator pageActionListener;
	private final JPopupMenu popupMenu = new JPopupMenu();
	private final JPanel topPanel = new JPanel();
	
	//button panel
	private JPanel buttonPanel;
	private JButton undeleteButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    /**
     * default constructor
     */
	public JVisualPdfPageSelectionPanel() {
		this(HORIZONTAL_ORIENTATION);
	}
	/**
	 * draw deleted items default value (true)
	 * show button panel default value (true)
	 * @param orientation panel orientation
	 */
	public JVisualPdfPageSelectionPanel(int orientation){
		this(orientation, true, true);
	}
	
	/**
	 * @param orientation panel orientation
	 * @param drawDeletedItems if true deleted items appear with a red cross over 
	 * @param showButtonPanel true=shows button panel
	 */
	public JVisualPdfPageSelectionPanel(int orientation, boolean drawDeletedItems, boolean showButtonPanel){
		this(orientation, drawDeletedItems, showButtonPanel, false, true, true, false);
	}
	
	/**
	 * @param orientation panel orientation
	 * @param drawDeletedItems if true deleted items appear with a red cross over 
	 * @param showButtonPanel true=shows button panel
	 * @param acceptDropFromDifferentComponents if true accepts dropping items from other components
	 */
	public JVisualPdfPageSelectionPanel(int orientation, boolean drawDeletedItems, boolean showButtonPanel, boolean acceptDropFromDifferentComponents, boolean showContextMenu, boolean showTopPanel, boolean minimalTopPanel){
		this.orientation = orientation;
		this.config = Configuration.getInstance();
		this.pdfLoader = new PdfThumbnailsLoader(this);
		this.drawDeletedItems = drawDeletedItems;
		this.showButtonPanel = showButtonPanel;
		this.showContextMenu = showContextMenu;
		this.acceptDropFromDifferentComponents = acceptDropFromDifferentComponents;
		this.showTopPanel = showTopPanel;
		this.minimalTopPanel = minimalTopPanel;
		init();		
	}
    
	/**
	 * panel initialization
	 */
	private void init(){
		setLayout(new GridBagLayout());
		
		thumbnailList.setDrawDeletedItems(drawDeletedItems);
		thumbnailList.setTransferHandler(new VisualSelectionListTransferHandler(pdfLoader, acceptDropFromDifferentComponents));
		thumbnailList.setDragEnabled(true);
		pagesWorker = new PagesWorker(thumbnailList);
		thumbnailList.addKeyListener(new VisualPdfSelectionKeyAdapter(pagesWorker));
		
		if(showButtonPanel){
			initButtonPanel(pagesWorker);
		}
		
		//JList orientation
		if(HORIZONTAL_ORIENTATION == orientation){
			thumbnailList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		}else{
			if(wrap){
				thumbnailList.setLayoutOrientation(JList.VERTICAL_WRAP);
			}
		}
				
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.setPreferredSize(new Dimension(400,30));
		
	    pdfSelectionActionListener = new VisualPdfSelectionActionListener(this, pdfLoader);
		if(!minimalTopPanel){
		    //load button
			loadFileButton.setMargin(new Insets(2, 2, 2, 2));
			loadFileButton.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Open"));
			loadFileButton.setPreferredSize(new Dimension(100,30));
			loadFileButton.setToolTipText(GettextResource.gettext(config.getI18nResourceBundle(),"Load a pdf document"));
			loadFileButton.setIcon(new ImageIcon(this.getClass().getResource("/images/add.png")));
			loadFileButton.addKeyListener(new EnterDoClickListener(loadFileButton));
			loadFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			loadFileButton.setAlignmentY(Component.CENTER_ALIGNMENT);
			loadFileButton.setActionCommand(VisualPdfSelectionActionListener.ADD);
			loadFileButton.addActionListener(pdfSelectionActionListener);		
		}
		documentProperties.setIcon(new ImageIcon(this.getClass().getResource("/images/info.png")));
		documentProperties.setVisible(false);
		
		
		//menu
		menuOptions.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Options"));
		menuOptions.setMnemonic(KeyEvent.VK_O);
		optionsMenu.setMargin(new Insets(2, 2, 2, 2));
		optionsMenu.add(menuOptions);
		optionsMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		//if minimal these items are not shown
		if(!minimalTopPanel){
			loadDocItem.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Open"));
			loadDocItem.setIcon(new ImageIcon(this.getClass().getResource("/images/add.png")));
			loadDocItem.addMouseListener(new MouseAdapter() {
	            public void mouseReleased(MouseEvent e) {               
	            	loadFileButton.doClick();
	            }
	        });
			menuOptions.add(loadDocItem);
			
			clearItem.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Clear"));
			clearItem.setIcon(new ImageIcon(this.getClass().getResource("/images/clear.png")));
			clearItem.addMouseListener(new MouseAdapter() {
	            public void mouseReleased(MouseEvent e) {               
	               	resetPanel();
	            }
	        });
			menuOptions.add(clearItem);
		}
		zoomInItem.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Zoom in"));
		zoomInItem.setIcon(new ImageIcon(this.getClass().getResource("/images/zoomin.png")));
		zoomInItem.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {               
                try{
                	thumbnailList.incZoomLevel();
            		zoomOutItem.setEnabled(true);
                	if(thumbnailList.getCurrentZoomLevel() >= JVisualSelectionList.MAX_ZOOM_LEVEL){
                		zoomInItem.setEnabled(false);
                	} 
                	((VisualListModel)thumbnailList.getModel()).elementsChanged();
                }
                catch (Exception ex){
                    log.error(GettextResource.gettext(config.getI18nResourceBundle(),"Error setting zoom level."), ex); 
                }                
            }
        });
		menuOptions.add(zoomInItem);

		zoomOutItem.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Zoom out"));
		zoomOutItem.setIcon(new ImageIcon(this.getClass().getResource("/images/zoomout.png")));
		zoomOutItem.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {               
                try{
                	thumbnailList.deincZoomLevel();
            		zoomInItem.setEnabled(true);
                	if(thumbnailList.getCurrentZoomLevel() <= JVisualSelectionList.MIN_ZOOM_LEVEL){
                		zoomOutItem.setEnabled(false);
                	} 
                	((VisualListModel)thumbnailList.getModel()).elementsChanged();
                }
                catch (Exception ex){
                    log.error(GettextResource.gettext(config.getI18nResourceBundle(),"Error setting zoom level."), ex); 
                }                
            }
        });
		menuOptions.add(zoomOutItem);

		thumbnailList.setModel(new VisualListModel());
		thumbnailList.setCellRenderer(new VisualListRenderer());
		thumbnailList.setVisibleRowCount(-1);
		thumbnailList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);		
		JScrollPane listScroller = new JScrollPane(thumbnailList);		
		
		JVisualSelectionListDropper dropper = new JVisualSelectionListDropper(pdfLoader);
		scrollPanelDropTarget = new DropTarget(listScroller,dropper);
		
		if(showContextMenu){
			//popup
			final JMenuItem menuItemMoveUp = new JMenuItem();
			menuItemMoveUp.setIcon(new ImageIcon(this.getClass().getResource("/images/up.png")));
			menuItemMoveUp.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Move Up"));
			menuItemMoveUp.addMouseListener(new VisualPdfSelectionMouseAdapter(PagesWorker.MOVE_UP, pagesWorker));
			popupMenu.add(menuItemMoveUp);
			
			final JMenuItem menuItemMoveDown = new JMenuItem();
			menuItemMoveDown.setIcon(new ImageIcon(this.getClass().getResource("/images/down.png")));
			menuItemMoveDown.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Move Down"));
			menuItemMoveDown.addMouseListener(new VisualPdfSelectionMouseAdapter(PagesWorker.MOVE_DOWN, pagesWorker));
			popupMenu.add(menuItemMoveDown);
			
			final JMenuItem menuItemRemove = new JMenuItem();
			menuItemRemove.setIcon(new ImageIcon(this.getClass().getResource("/images/remove.png")));
			menuItemRemove.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Delete"));
			menuItemRemove.addMouseListener(new VisualPdfSelectionMouseAdapter(PagesWorker.REMOVE, pagesWorker));
			popupMenu.add(menuItemRemove);
			
			final JMenuItem menuItemUndelete = new JMenuItem();
			menuItemUndelete.setIcon(new ImageIcon(this.getClass().getResource("/images/remove.png")));
			menuItemUndelete.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Undelete"));
			menuItemUndelete.addMouseListener(new VisualPdfSelectionMouseAdapter(PagesWorker.UNDELETE, pagesWorker));
			popupMenu.add(menuItemUndelete);
			
			//shyow popup
			thumbnailList.addMouseListener(new MouseAdapter() {
	            public void mousePressed(MouseEvent e) {
	                if (e.isPopupTrigger()) {
						showMenu(e);
					}
	            }
	            public void mouseReleased(MouseEvent e) {
	                if (e.isPopupTrigger()) {
						showMenu(e);
					}
	            }
	            private void showMenu(MouseEvent e) {
	            	int[] selection = thumbnailList.getSelectedIndices();
	            	if(!(selection!=null && selection.length>1)){
	            		thumbnailList.setSelectedIndex(thumbnailList.locationToIndex(e.getPoint()) );
	            		selection = thumbnailList.getSelectedIndices();
	            	}
	            	//if elements are physically deleted i don't need this item
	            	if(drawDeletedItems){
		            	menuItemUndelete.setEnabled(true);	            	
	            	}else{
	            		menuItemUndelete.setEnabled(false);
	            	}
	            	popupMenu.show(thumbnailList, e.getX(), e.getY() );
	            }
	        });
		}
		
		if(!minimalTopPanel){
			topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			topPanel.add(loadFileButton);
		}
		topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		topPanel.add(documentProperties);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(optionsMenu);
		
		GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.fill = GridBagConstraints.BOTH  ;
		topConstraints.gridx=0;
		topConstraints.gridy=0;
		topConstraints.gridwidth=3;
		topConstraints.gridheight=1;
		topConstraints.insets = new Insets(5,5,5,5);
		topConstraints.weightx=1.0;
		topConstraints.weighty=0.0;		
		if(showTopPanel){
			add(topPanel, topConstraints);
		}

		GridBagConstraints thumbConstraints = new GridBagConstraints();
		thumbConstraints.fill = GridBagConstraints.BOTH;
		thumbConstraints.gridx=0;
		thumbConstraints.gridy=1;
		thumbConstraints.gridwidth=(showButtonPanel?2:3);
		thumbConstraints.gridheight=2;
		thumbConstraints.insets = new Insets(5,5,5,5);
		thumbConstraints.weightx=1.0;
		thumbConstraints.weighty=1.0;		
		add(listScroller, thumbConstraints);
		
		if(showButtonPanel){
			GridBagConstraints buttonsConstraints = new GridBagConstraints();
			buttonsConstraints.fill = GridBagConstraints.BOTH;
			buttonsConstraints.gridx=2;
			buttonsConstraints.gridy=1;
			buttonsConstraints.gridwidth=1;
			buttonsConstraints.gridheight=2;
			buttonsConstraints.insets = new Insets(5,5,5,5);
			buttonsConstraints.weightx=0.0;
			buttonsConstraints.weighty=1.0;		
			add(buttonPanel, buttonsConstraints);
		}
	}
	
	
	/**
     * adds a button to the button panel
     * @param button
     */
    private void addButtonToButtonPanel(JButton button){
    	button.setMinimumSize(new Dimension(90, 25));
    	button.setMaximumSize(new Dimension(100, 25));
    	button.setPreferredSize(new Dimension(95, 25));
    	buttonPanel.add(button);
		buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
    }
    
	private void initButtonPanel(PagesWorker pagesWorker){
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		
	    pageActionListener = new PagesActionsMediator(pagesWorker);

	    //move up button
		moveUpButton = new JButton();
		moveUpButton.setMargin(new Insets(2, 2, 2, 2));
		moveUpButton.addActionListener(pageActionListener);        
		moveUpButton.setIcon(new ImageIcon(this.getClass().getResource("/images/up.png")));
		moveUpButton.setActionCommand(PagesWorker.MOVE_UP);
		moveUpButton.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Move Up"));
		moveUpButton.setToolTipText(GettextResource.gettext(config.getI18nResourceBundle(),"Move up selected pages")+" "+GettextResource.gettext(config.getI18nResourceBundle(),"(Alt+ArrowUp)"));
		moveUpButton.addKeyListener(new EnterDoClickListener(moveUpButton));
		moveUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButtonToButtonPanel(moveUpButton);
		
    	//move down button
		moveDownButton = new JButton();
		moveDownButton.addActionListener(pageActionListener);
		moveDownButton.setIcon(new ImageIcon(this.getClass().getResource("/images/down.png")));
		moveDownButton.setActionCommand(PagesWorker.MOVE_DOWN);
		moveDownButton.setMargin(new Insets(2, 2, 2, 2));
		moveDownButton.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Move Down"));
		moveDownButton.setToolTipText(GettextResource.gettext(config.getI18nResourceBundle(),"Move down selected pages")+" "+GettextResource.gettext(config.getI18nResourceBundle(),"(Alt+ArrowDown)"));
		moveDownButton.addKeyListener(new EnterDoClickListener(moveDownButton));
		moveDownButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButtonToButtonPanel(moveDownButton);
		
		//delete button
		removeButton = new JButton();
		removeButton.addActionListener(pageActionListener);
		removeButton.setIcon(new ImageIcon(this.getClass().getResource("/images/remove.png")));
		removeButton.setActionCommand(PagesWorker.REMOVE);
		removeButton.setMargin(new Insets(2, 2, 2, 2));
		removeButton.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Delete"));
		removeButton.setToolTipText(GettextResource.gettext(config.getI18nResourceBundle(),"Delete selected pages")+" "+GettextResource.gettext(config.getI18nResourceBundle(),"(Canc)"));
		removeButton.addKeyListener(new EnterDoClickListener(removeButton));
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButtonToButtonPanel(removeButton);
		
		//undelete button
		if(drawDeletedItems){
			undeleteButton = new JButton();
			undeleteButton.addActionListener(pageActionListener);
			undeleteButton.setIcon(new ImageIcon(this.getClass().getResource("/images/remove.png")));
			undeleteButton.setActionCommand(PagesWorker.UNDELETE);
			undeleteButton.setMargin(new Insets(2, 2, 2, 2));
			undeleteButton.setText(GettextResource.gettext(config.getI18nResourceBundle(),"Undelete"));
			undeleteButton.setToolTipText(GettextResource.gettext(config.getI18nResourceBundle(),"Undelete selected pages")+" "+GettextResource.gettext(config.getI18nResourceBundle(),"(Ctrl+Z)"));
			undeleteButton.addKeyListener(new EnterDoClickListener(undeleteButton));
			undeleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			addButtonToButtonPanel(undeleteButton);
		}
	}
	/**
	 * reset the panel
	 */
	public void resetPanel(){
		thumbnailList.setCurrentZoomLevel(JVisualSelectionList.DEFAULT_ZOOM_LEVEL);
		zoomInItem.setEnabled(true);
		zoomOutItem.setEnabled(true);
		((VisualListModel)thumbnailList.getModel()).clearData();
		selectedPdfDocument = null;
		selectedPdfDocumentPassword = "";
		setDocumentPropertiesVisible(false);
		
	}
	/**
	 * Set the visible the label that shows document properties
	 * @param visible
	 */
	public void setDocumentPropertiesVisible(boolean visible){
		documentProperties.setVisible(visible);
	}
	
	/**
	 * Set the document properties to be shown as a tooltip of the documentProperties JLabel
	 * @param properties
	 */
	public void setDocumentProperties(String properties){
		documentProperties.setToolTipText(properties);
	}
	
	/**
	 *  Sets the document properties
	 * @param fileName
	 * @param pages
	 * @param version
	 * @param title
	 * @param author
	 * @param creator
	 * @param producer
	 */
	public void setDocumentProperties(String fileName, String pages, String version, String title, String author, String creator, String producer, boolean isEncrypted){
		String encrypted = GettextResource.gettext(config.getI18nResourceBundle(),"No");
		if(isEncrypted){
			encrypted = GettextResource.gettext(config.getI18nResourceBundle(),"Yes");
		}
		setDocumentProperties( 
	    		"<html><body><b><p>"+GettextResource.gettext(config.getI18nResourceBundle(),"File: ")+"</b>"+fileName+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Pages: ")+"</b>"+pages+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Pdf version: ")+"</b>"+version+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Title: ")+"</b>"+title+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Author: ")+"</b>"+author+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Creator: ")+"</b>"+creator+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Producer: ")+"</b>"+producer+"</p>"+
	    		"<p><b>"+GettextResource.gettext(config.getI18nResourceBundle(),"Encrypted: ")+"</b>"+encrypted+"</p>"+
	    		"</body></html>");
	}
	
	/**
	 * @return the orientation
	 */
	public int getOrientation() {
		return orientation;
	}
	/**
	 * @return the selectedPdfDocument
	 */
	public File getSelectedPdfDocument() {
		return selectedPdfDocument;
	}
	/**
	 * @return the wrap
	 */
	public boolean isWrap() {
		return wrap;
	}
	/**
	 * @return the thumbnailList
	 */
	public JVisualSelectionList getThumbnailList() {
		return thumbnailList;
	}

	/**
	 * @param selectedPdfDocument the selectedPdfDocument to set
	 */
	public void setSelectedPdfDocument(File selectedPdfDocument) {
		this.selectedPdfDocument = selectedPdfDocument;
	}

	/**
	 * @return the scrollPanelDropTarget
	 */
	public DropTarget getScrollPanelDropTarget() {
		return scrollPanelDropTarget;
	}
	/**
	 * @return the drawDeletedItems
	 */
	public boolean isDrawDeletedItems() {
		return drawDeletedItems;
	}
	/**
	 * @param drawDeletedItems the drawDeletedItems to set
	 */
	public void setDrawDeletedItems(boolean drawDeletedItems) {
		this.drawDeletedItems = drawDeletedItems;
	}
	/**
	 * @return the pdfLoader
	 */
	public PdfThumbnailsLoader getPdfLoader() {
		return pdfLoader;
	}
	/**
	 * @return the selectedPdfDocumentPassword
	 */
	public String getSelectedPdfDocumentPassword() {
		return selectedPdfDocumentPassword;
	}
	/**
	 * @param selectedPdfDocumentPassword the selectedPdfDocumentPassword to set
	 */
	public void setSelectedPdfDocumentPassword(String selectedPdfDocumentPassword) {
		this.selectedPdfDocumentPassword = selectedPdfDocumentPassword;
	}
	/**
	 * @return the topPanel
	 */
	public JPanel getTopPanel() {
		return topPanel;
	}
	/**
	 * 
	 * @return valid elements as a String that can be used as "-u" parameter for the concat console command. Empty String if no valid elements.
	 */
	public String getValidElementsString(){
		String retVal = "";
		Collection validElements = ((VisualListModel)thumbnailList.getModel()).getValidElements();
		if(validElements!=null && validElements.size()>0){
			StringBuffer buffer = new StringBuffer();
			VisualPageListItem startElement = null;
			VisualPageListItem endElement = null;
			for(Iterator iter = validElements.iterator(); iter.hasNext();){
				VisualPageListItem currentElement = (VisualPageListItem)iter.next();
				//time to start a new section
				if(startElement==null){					
					startElement = currentElement;
					endElement = currentElement;
				}else{
					//let's check if it's the next number or not
					if(currentElement.getPageNumber() == (endElement.getPageNumber()+1)){
						endElement = currentElement;
					}else{
						if (buffer.length()>0){
							buffer = buffer.append(',');
						}
						if(startElement.getPageNumber() == endElement.getPageNumber()){
							//just the page number
							buffer = buffer.append(startElement.getPageNumber());
						}else{
							//page range
							buffer = buffer.append(startElement.getPageNumber()).append('-').append(endElement.getPageNumber());
						}
						startElement = currentElement;
						endElement = currentElement;
					}
				}
			}
			//check the last elements
			if (buffer.length()>0){
				buffer = buffer.append(',');
			}
			if(startElement.getPageNumber() == endElement.getPageNumber()){
				buffer = buffer.append(startElement.getPageNumber());
			}else{
				buffer = buffer.append(startElement.getPageNumber()).append('-').append(endElement.getPageNumber());
			}
			retVal = buffer.append(':').toString();
		}
		return retVal;
	}
	
}
