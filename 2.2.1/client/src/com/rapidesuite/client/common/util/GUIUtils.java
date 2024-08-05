/**************************************************
 * $Revision: 57981 $:
 * $Author: olivier.deruelle $:
 * $Date: 2016-09-05 18:30:13 +0700 (Mon, 05 Sep 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/GUIUtils.java $:
 * $Id: GUIUtils.java 57981 2016-09-05 11:30:13Z olivier.deruelle $:
 */

package com.rapidesuite.client.common.util;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.springframework.util.Assert;

import com.rapidesuite.build.gui.panels.SwiftBuildHtmlValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;

public class GUIUtils
{
	
	public static final Font PLAIN_SYSTEM_FONT=new Font(null, Font.PLAIN, 13);
	public static final Font BOLD_SYSTEM_FONT=new Font(null, Font.BOLD, 13);
	
	public static File copyScreenshotFileAndSaveAsJpg(final File originalFile, final String newFileName) throws IOException {
		Assert.notNull(originalFile);
		Assert.isTrue(originalFile.isFile(), "you entered something which is not file");
		Assert.notNull(newFileName);
		
		final File directory = originalFile.getParentFile();
		Assert.isTrue(directory.isDirectory());
		
		final BufferedImage img = ImageIO.read(originalFile);
		String fileNameToUse = newFileName;
		if (!fileNameToUse.matches("^.*\\.(?i)jp(e)?g$")) {
			fileNameToUse += ".jpg";
		}
		final File output = new File(directory, fileNameToUse);
		ImageIO.write(img, "jpg", output);
		return output;
	}
	
	public static File captureScreenshotAndSaveAsJpg(final File directory, final String fileName) throws AWTException, IOException {
		String fileNameToUse = fileName;
		if (!fileNameToUse.matches("^.*\\.(?i)jp(e)?g$")) {
			fileNameToUse += ".jpg";
		}
		final BufferedImage screenshot = captureScreenshot();
		final File output = new File(directory, fileNameToUse);
		ImageIO.write(screenshot, "jpg", output);
		return output;
	}
	
	public static BufferedImage captureScreenshot() throws AWTException {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		Rectangle allScreenBounds = new Rectangle();
		for (GraphicsDevice screen : screens) {
			Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
			allScreenBounds.width += screenBounds.width;
			allScreenBounds.height = Math.max(allScreenBounds.height, screenBounds.height);
		}
		Robot robot = new Robot();
		BufferedImage screenShot = robot.createScreenCapture(allScreenBounds);
		return screenShot;
	}
	
	public static JLabel getLabelWithImage(Class<?> classObject,String imageName) {
		ImageIcon ii=getImageIcon(classObject,imageName);
		return new JLabel(ii);
	}

	public static ImageIcon getImageIcon(Class<?> classObject,String imageName) {
		if (imageName==null) return null;
		try
		{
			URL iconURL = classObject.getResource(imageName);
			return new ImageIcon(iconURL);
		}
		catch (Exception e)
		{
			return new ImageIcon(imageName);
		}
	}

	public static JButton getButton(Class<?> classObject,String label,String imageName) {
		ImageIcon ii=getImageIcon(classObject,imageName);
		return new JButton(label, ii);
	}

	public static JLabel getLabel(String labelName,boolean isVisible) {
		JLabel label=new JLabel(labelName);
		label.setFont(PLAIN_SYSTEM_FONT);
		label.setVisible(isVisible);
		return label;
	}
	
	public static JTextField getInputField(boolean isVisible,boolean isEditable) {
		return GUIUtils.getInputField(isVisible, isEditable, true);
	}

	public static JTextField getInputField(boolean isVisible,boolean isEditable, boolean initializeColumn) {
		JTextField inputField=initializeColumn ? new JTextField(30) : new JTextField();
		inputField.setVisible(isVisible);
		inputField.setEditable(isEditable);
		return inputField;
	}

	public static JPanel addToPanel(JPanel panel,JComponent label,JComponent firstComponent,
			JComponent secondComponent,
			JComponent thirdComponent,
			Dimension middleRigidAreaDimension,
			Dimension rightSideRigidAreaDimension) {
		JPanel tempPanel= new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));  
		panel.add(tempPanel);
		tempPanel.add(label);
		if (middleRigidAreaDimension!=null) {
			tempPanel.add(Box.createRigidArea(middleRigidAreaDimension));
		}
		if (firstComponent!=null) {
			tempPanel.add(firstComponent);
		}

		if (secondComponent!=null) {
			tempPanel.add(Box.createRigidArea(new Dimension(5,0)));
			tempPanel.add(secondComponent);
		}
		if (thirdComponent!=null) {
			tempPanel.add(Box.createRigidArea(new Dimension(5,0)));
			tempPanel.add(thirdComponent);
		}
		if (rightSideRigidAreaDimension!=null) {
			tempPanel.add(Box.createRigidArea(rightSideRigidAreaDimension));
		}
		return tempPanel;
	}

	public static void resetLabel(final JLabel label) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (label==null) {
					return;
				}
				label.setIcon(null);
				label.setText("");
			}
		});
	}

	public static void showInProgressMessage(JLabel label,String message) {
		showMessage(label,label.getClass(),message,GUIUtils.PROPERTY_INDICATOR_STATUS_IMAGE_NAME,Color.BLACK);
	}
	
	public static void showErrorMessage(JLabel label,String message) {
		showMessage(label,label.getClass(),message,GUIUtils.PROPERTY_INVALID_STATUS_IMAGE_NAME,Color.RED);
	}
	
	public static void showSuccessMessage(JLabel label,String message) {
		showMessage(label,label.getClass(),message,GUIUtils.PROPERTY_VALID_STATUS_IMAGE_NAME,new Color(102,166,83));
	}
	
	public static void showStandardMessage(JLabel label,String message) {
		showMessage(label,label.getClass(),message,null,Color.BLACK);
	}
	
	public static void showWarningMessage(JLabel label,String message) {
		showMessage(label,label.getClass(),message,GUIUtils.PROPERTY_WARNING_STATUS_IMAGE_NAME,new Color(170,170,0));
	}
	
	private static void showMessage(final JLabel label,final Class<?> classObject,final String message,final String image,final Color foregroundColor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ImageIcon ii=null;
				if (image!=null) {
					ii=getImageIcon(classObject,image);
				}
				label.setIcon(ii);
				label.setText("<html>"+message+"</html>");
				label.setForeground(foregroundColor);
			}
		});
	}
	
	public static void showEmptyMessageAndDisableNextButton(EnvironmentValidationPanel current, SwiftBuildHtmlValidationPanel next){
		GUIUtils.showStandardMessage(current.getMessageLabel(), "");
		next.setNextButton(false);
		next.getBuildMain().setRunSqlScriptItemEnabled(false);
		current.getNextButtonWrapper().simulateClickingNext();
		
	}
	
	public static void popupErrorMessage(Component parentComponent,String message)
	{
		JOptionPane.showMessageDialog(parentComponent,message,"Error",JOptionPane.ERROR_MESSAGE);
	}
	
	public static void popupErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(null,message,"Error",JOptionPane.ERROR_MESSAGE);
	}
	
	public static void popupWarningMessage(String message)
	{
		JOptionPane.showMessageDialog(null,message,"Warning",JOptionPane.WARNING_MESSAGE);
	}

	public static void popupInformationMessage(String message)
	{
		JOptionPane.showMessageDialog(null,	message,"Information",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void popupInformationMessage(Component parentComponent,String message)
	{
		JOptionPane.showMessageDialog(parentComponent,	message,"Information",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static JWindow makeWindow()
	{
		JWindow win = new JWindow();
		Container wcp = win.getContentPane();
		JPanel pan = new JPanel();
		pan.setBackground(Color.white);
		pan.setBorder(new LineBorder(Color.black));
		wcp.add(pan,"Center");
		wcp.setBackground(Color.white);
		win.setSize(400,50);
		win.setLocation(300,300);
		pan.add(new JLabel("REVERSE, Loading...",SwingConstants.CENTER));

		return win;
	}
			
	public static void deleteAllRows(JTable table) {
		int numRows = table.getRowCount();
		DefaultTableModel model=(DefaultTableModel) table.getModel();
		for(int i = numRows - 1; i >=0; i--) {
			model.removeRow(i);
		}
	}
	
	public static JWindow getSplashWindow(Frame f,String text)
    {
		JWindow window=new JWindow(f);
		window.setSize(300,100);
        JLabel l = new JLabel(text);
        l.setFont(BOLD_SYSTEM_FONT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel tempPanel= new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tempPanel.add(l);
        window.getContentPane().add(tempPanel, BorderLayout.CENTER);
        Dimension screenSize =
          Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        window.setLocation(screenSize.width/2 - (labelSize.width/2),
                    screenSize.height/2 - (labelSize.height/2));
        return window;
    }
	
	public static void setComponentDimension(JComponent component,int width,int height) {
		component.setPreferredSize(new Dimension(width,height));
		component.setMinimumSize(new Dimension(width,height));
		component.setMaximumSize(new Dimension(width,height));
	}
	
	public static void setGridBagConstraints(GridBagConstraints gbc,int row, int column, int width, int height, int fill, int anchor) {
		gbc.anchor = anchor;
		GUIUtils.setGridBagConstraints(gbc,row, column, width, height, fill);
	}	
	
	public static void setGridBagConstraints(GridBagConstraints gbc,int row, int column, int width, int height, int fill) {
		gbc.gridy = row;
		gbc.gridx = column;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.fill = fill;  // GridBagConstraints.NONE .HORIZONTAL .VERTICAL .BOTH
		// leave other fields (eg, anchor) unchanged.
	}
		
	public static void addToUserPanel(JPanel panel,JComponent topLeftComponent,JComponent rightComponent,
			JRadioButton includeButton,JRadioButton excludeButton) {
		
		if (includeButton!=null && excludeButton!=null) {
			ButtonGroup group = new ButtonGroup();
			group.add(includeButton);
			group.add(excludeButton);
		}
		panel.setLayout(new BorderLayout());
		if (topLeftComponent!=null || rightComponent!=null) {
			JPanel northPanel= new JPanel();
			northPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));  
			if (topLeftComponent!=null) {
				northPanel.add(topLeftComponent);
			}
			if (rightComponent!=null) {
				northPanel.add(Box.createGlue());
				northPanel.add(rightComponent);
			}
			panel.add(northPanel, BorderLayout.NORTH);
		}
		if (includeButton!=null && excludeButton!=null) {
			JPanel southPanel= new JPanel();
			southPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
			southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));  
			southPanel.add(includeButton);
			southPanel.add(excludeButton);
			southPanel.add(Box.createRigidArea(new Dimension(30, 0)));
			panel.add(southPanel, BorderLayout.SOUTH);
		}
	}
	
	public static void setSelectedCheckBox(List<JCheckBox> list,boolean isSelected)
	{
		for (JCheckBox checkBox:list) {
			checkBox.setSelected(isSelected);
			if (isSelected) {
				checkBox.setBackground(UtilsConstants.BACKGROUND_COLOR_SELECTION);
			}
			else {
				checkBox.setBackground(Color.white);
			}
		}
	}
	
	public static void unselectRadioButton(List<JRadioButton> list)
	{
		for (JRadioButton item:list) {
			item.setSelected(false);
			item.setBackground(Color.white);
		}
	}
	
	public static JCheckBox getSelectionAllCheckBox(boolean isSelected)
	{
		final JCheckBox checkBox= new JCheckBox("Select/ Unselect all");
		checkBox.setSelected(isSelected);
		return checkBox;
	}
	
	public static List<JCheckBox> getCheckBoxesList(Map<Integer,String> map,Dimension componentDimension)
	{
		return getCheckBoxesList(map.values(), componentDimension);
	}
	
	public static List<JCheckBox> getCheckBoxesListLong(Map<Long,String> map,Dimension componentDimension)
	{
		return getCheckBoxesList(map.values(), componentDimension);
	}
	
	public static List<JCheckBox> getCheckBoxesList(Collection<String> names,Dimension componentDimension)
	{
		List<JToggleButton> temp=getJToggleButtonsList(names,componentDimension,true);
		List<JCheckBox> toReturn=new ArrayList<JCheckBox>();
		for (JToggleButton toggleButton:temp) {
			toReturn.add((JCheckBox)toggleButton);
		}
		return toReturn;
	}	
	
	public static List<JRadioButton> getRadioButtonsList(Map<Integer,String> map,Dimension componentDimension)
	{
		List<JToggleButton> temp=getJToggleButtonsList(map.values(),componentDimension,false);
		List<JRadioButton> toReturn=new ArrayList<JRadioButton>();
		for (JToggleButton toggleButton:temp) {
			toReturn.add((JRadioButton)toggleButton);
		}
		return toReturn;
	}
	
	public static List<JToggleButton> getJToggleButtonsList(Collection<String> names,Dimension componentDimension,boolean isJCheckBox)
	{
		List<JToggleButton> toReturn=new ArrayList<JToggleButton>();
		
		// Sort by name:
		TreeSet<String> namesSorted = new TreeSet<String>(names);
		Iterator<String> iter=namesSorted.iterator();
		while (iter.hasNext()) {
			String key=iter.next();
			
			JToggleButton component=null;
			if (isJCheckBox) {
				component=new JCheckBox(key);
			}
			else {
				component=new JRadioButton(key);
			}
			if (componentDimension!=null) {
				GUIUtils.setComponentDimension(component, componentDimension.width,componentDimension.height);
			}
			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					AbstractButton abstractButton = (AbstractButton) itemEvent.getSource();
					int state = itemEvent.getStateChange();
					if (state == ItemEvent.SELECTED) {
						abstractButton.setBackground(UtilsConstants.BACKGROUND_COLOR_SELECTION);
					}
					else {
						abstractButton.setBackground(Color.WHITE);
					}
				}
			};
			component.addItemListener(itemListener);
			component.setBackground(Color.WHITE);
			toReturn.add(component);
		}
		return toReturn;
	}
	
	public static JPanel createJRadioButtonsVerticalListPanel(List<JRadioButton> list,ButtonGroup group)
	{
		JPanel toReturn=createJComponentVerticalListPanel();
		for (JRadioButton radioButton:list) {
			toReturn.add(radioButton);
			group.add(radioButton);
		}
		return toReturn;
	}
	
	public static JPanel createJCheckBoxesVerticalListPanelWithHtmlTitle(final String titleHtml, List<JCheckBox> list)
	{
		JPanel toReturn=createJComponentVerticalListPanel();
		final JLabel titleLabel = new JLabel("<html><span>"+titleHtml+"</span></html>");
		titleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		toReturn.add(titleLabel);
		for (JCheckBox checkBox:list) {
			toReturn.add(checkBox);
		}
		return toReturn;
	}	
	
	public static JPanel createJCheckBoxesVerticalListPanel(List<JCheckBox> list)
	{
		JPanel toReturn=createJComponentVerticalListPanel();
		for (JCheckBox checkBox:list) {
			toReturn.add(checkBox);
		}
		return toReturn;
	}
	
	public static JPanel createJComponentVerticalListPanel()
	{
		JPanel toReturn=new JPanel();
		toReturn.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));  
		toReturn.setBackground(Color.WHITE);
		toReturn.setOpaque(true);
		return toReturn;
	}
	
	public static JScrollPane createJScrollPane(JPanel panel,int width,int height)
	{
		JScrollPane toReturn = new JScrollPane(panel);
		if (width!=-1 && height!=-1) {
			toReturn.setPreferredSize(new Dimension(width,height));
		}
		return toReturn;
	}
	
	public static List<Long> getSelectedIdsLong(List<JCheckBox> list,Map<Long,String> map) {
		List<Long> toReturn=new ArrayList<Long>();
		if (list==null) {
			return toReturn;
		}
		Map<String,Long> invertedMap=getInvertedMap(map);
		for (JCheckBox checkBox:list) {
			if (checkBox.isSelected() ) {
				String text=checkBox.getText();
				Long id=invertedMap.get(text);
				if (id!=null) {
					toReturn.add(id);
				}
			}
		}
		return toReturn;
	}
	
	public static List<Integer> getSelectedIds(List<JCheckBox> list,Map<Integer,String> map) {
		List<Integer> toReturn=new ArrayList<Integer>();
		if (list==null || map==null) {
			return toReturn;
		}
		Map<String,Integer> invertedMap=getInvertedMap(map);
		for (JCheckBox checkBox:list) {
			if (checkBox.isSelected() ) {
				String text=checkBox.getText();
				Integer id=invertedMap.get(text);
				if (id!=null) {
					toReturn.add(id);
				}
			}
		}
		return toReturn;
	}
	
	public static Integer getSelectedId(List<JRadioButton> list,Map<Integer,String> map) {
		if (list==null) {
			return null;
		}
		Map<String,Integer> invertedMap=getInvertedMap(map);
		for (JRadioButton radioButton:list) {
			if (radioButton.isSelected() ) {
				String text=radioButton.getText();
				return invertedMap.get(text);
			}
		}
		return null;
	}
	
	public static void setSelectedCheckBoxes(List<JCheckBox> list,Map<Integer,String> map) {
		if (list==null) {
			return;
		}
		Map<String,Integer> invertedMap=getInvertedMap(map);
		for (JCheckBox checkBox:list) {
			String text=checkBox.getText();
			Integer id=invertedMap.get(text);
			checkBox.setSelected(id!=null);
		}
	}
	
	public static void setSelectedCheckBoxesLong(List<JCheckBox> list,Map<Long,String> map) {
		if (list==null) {
			return;
		}
		Map<String,Long> invertedMap=getInvertedMap(map);
		for (JCheckBox checkBox:list) {
			String text=checkBox.getText();
			Long id=invertedMap.get(text);
			checkBox.setSelected(id!=null);
		}
	}
	
	public static void setSelectedRadioButtons(List<JRadioButton> list,Map<Integer,String> map) {
		if (list==null) {
			return;
		}
		Map<String,Integer> invertedMap=getInvertedMap(map);
		for (JRadioButton radioButton:list) {
			String text=radioButton.getText();
			Integer id=invertedMap.get(text);
			radioButton.setSelected(id!=null);
		}
	}
	
	public static <A, B> Map<B,A> getInvertedMap(Map<A,B> map) {
		Iterator<A> iter=map.keySet().iterator();
		Map<B,A> toReturn=new HashMap<B,A>();
		while (iter.hasNext()) {
			A key=iter.next();
			toReturn.put(map.get(key),key);
		}
		return toReturn;
	}

	public static void setEnabledOnComponents(List<Component> list,boolean isEnabled) {
		for (Component component:list) {
			if (component!=null) {
				component.setEnabled(isEnabled);
			}
		}
	}
	
	public static JLabel getHyperlinkComponent(String text) {
		JLabel link=new JLabel("<html><a href=\"\">"+text+"</a></html>");
		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
		link.setCursor(cursor);
		return link;
	}

    public static final String PROPERTY_VALID_STATUS_IMAGE_NAME="/images/bulletOKgreen.gif";
    public static final String PROPERTY_INVALID_STATUS_IMAGE_NAME="/images/error.gif";
    public static final String PROPERTY_WARNING_STATUS_IMAGE_NAME="/images/warning.gif";
    public static final String PROPERTY_INDICATOR_STATUS_IMAGE_NAME="/images/indicator.gif";
    
	public static void addCopyPasteByRightClickCapabilityToJTextField(final JTextField textField, final boolean allowCopyingAndCutting) {
		textField.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 1) {
					if (SwingUtilities.isRightMouseButton(e)) {
						
						JPopupMenu onRightClickMenu = new JPopupMenu();
						
						final JMenuItem cutItem = new JMenuItem("Cut");
						cutItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									Robot robot = new Robot();
									robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
									robot.keyPress(java.awt.event.KeyEvent.VK_X);
									robot.keyRelease(java.awt.event.KeyEvent.VK_X);		
									robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
								} catch (AWTException e1) {
									throw new Error(e1);
								}
							}

						});
						cutItem.setEnabled(allowCopyingAndCutting && textField.getSelectedText() != null && !textField.getSelectedText().isEmpty());
						onRightClickMenu.add(cutItem);						

						final JMenuItem copyItem = new JMenuItem("Copy");
						copyItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									Robot robot = new Robot();
									robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
									robot.keyPress(java.awt.event.KeyEvent.VK_C);
									robot.keyRelease(java.awt.event.KeyEvent.VK_C);		
									robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
								} catch (AWTException e1) {
									throw new Error(e1);
								}
							}

						});
						copyItem.setEnabled(allowCopyingAndCutting && textField.getSelectedText() != null && !textField.getSelectedText().isEmpty());
						onRightClickMenu.add(copyItem);	

						final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						final Transferable clipboardContents = clipboard.getContents(null);
						final boolean hasTransferableText = clipboardContents != null && clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor);						
						
						final JMenuItem pasteItem = new JMenuItem("Paste");
						pasteItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									Robot robot = new Robot();
									robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
									robot.keyPress(java.awt.event.KeyEvent.VK_V);
									robot.keyRelease(java.awt.event.KeyEvent.VK_V);		
									robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);									
								} catch (AWTException e1) {
									throw new Error(e1);
								}
							}

						});
						pasteItem.setEnabled(hasTransferableText);
						onRightClickMenu.add(pasteItem);
						
						final JMenuItem deleteItem = new JMenuItem("Delete");
						deleteItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									Robot robot = new Robot();
									robot.keyPress(java.awt.event.KeyEvent.VK_DELETE);
									robot.keyRelease(java.awt.event.KeyEvent.VK_DELETE);		
								} catch (AWTException e1) {
									throw new Error(e1);
								}
							}

						});
						deleteItem.setEnabled(textField.getSelectedText() != null && !textField.getSelectedText().isEmpty());
						onRightClickMenu.add(deleteItem);			
						
						final JMenuItem selectAllItem = new JMenuItem("Select All");
						selectAllItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									Robot robot = new Robot();
									robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
									robot.keyPress(java.awt.event.KeyEvent.VK_A);
									robot.keyRelease(java.awt.event.KeyEvent.VK_A);		
									robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);		
								} catch (AWTException e1) {
									throw new Error(e1);
								}
							}

						});
						onRightClickMenu.add(selectAllItem);
						onRightClickMenu.show(e.getComponent(), e.getX(), e.getY());

					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}    
	
	public static void changeCursorOnGlassPane(Container glassPane, Cursor cursorToChange) {
        glassPane.setCursor(cursorToChange);
	}
	
}