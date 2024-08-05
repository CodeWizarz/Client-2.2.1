package com.rapidesuite.inject;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.erapidsuite.configurator.navigation0005.DefineVariableType;
import com.erapidsuite.configurator.navigation0005.FusionNavigationType;
import com.erapidsuite.configurator.navigation0005.FusionWebServiceNavigationType;
import com.erapidsuite.configurator.navigation0005.Navigation;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.rapidesuite.build.gui.apigrid.APIDataGridOptionsFrame;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.importdata.XmlDataParser;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.fusionData0000.FusionDataDocument;
import com.rapidesuite.core.fusionData0000.FusionDataType;
import com.rapidesuite.core.fusionData0000.InventoryMapType;
import com.rapidesuite.core.fusionData0000.LType;
import com.rapidesuite.core.fusionData0000.MType;
import com.rapidesuite.core.fusionScripts0000.FusionScriptType;
import com.rapidesuite.core.fusionScripts0000.FusionScriptsDocument;
import com.rapidesuite.core.fusionScripts0000.ScriptType;
import com.rapidesuite.core.injectionPackageInformation.InjectionPackageInformationDocument;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.inject.gui.ApplicationInfoPanel;
import com.rapidesuite.inject.gui.DesktopApi;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.InjectionPackageLoadDialog;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;

public class InjectUtils {


	public static void encryptToFile(String text,File destinationFile) throws Exception {
		InputStream in=null;
		CipherOutputStream cout=null;
		try{
			in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
			Cipher cipher = Encryption.getEncryptedCipherInstance();
			cout = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)), cipher);
			IOUtils.copy(in, cout);
		}
		finally
		{
			IOUtils.closeQuietly(cout);
			IOUtils.closeQuietly(in);
		}
	}

	public static String decryptFromFile(File sourceFile) throws Exception {
		OutputStream out=null;
		CipherInputStream cin=null;
		try{
			Cipher cipher = Encryption.getDecryptedCipherInstance();
			cin = new CipherInputStream(new FileInputStream(sourceFile), cipher);
			out =  new ByteArrayOutputStream();
			IOUtils.copy(cin, out);

			return out.toString();
		}
		finally
		{
			IOUtils.closeQuietly(cin);
			IOUtils.closeQuietly(out);
		}
	}

	public static Properties loadProperties(File file) throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			prop.load(input);
			return prop;
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					FileUtils.printStackTrace(e);
				}
			}
		}

	}

	public static String getInjectionEnvironmentNextToken(String text,StringBuffer remainingText)
	{
		int indexOf=text.indexOf(ApplicationInfoPanel.SEPARATOR);
		if (indexOf!=-1) {
			String value=text.substring(0,indexOf);
			remainingText.append(text.substring(indexOf+ApplicationInfoPanel.SEPARATOR.length()));
			return value;
		}
		return null;
	}


	public static void addTab(JTabbedPane jtp,String tabName,JPanel panel,int tabIndex,int tabWidth,int tabHeight,boolean isEnabled) {
		JLabel labelTab = new JLabel(tabName, SwingConstants.CENTER);
		labelTab.setOpaque(true);
		labelTab.setForeground(Color.decode("#FFFFFF") );
		labelTab.setBackground(Color.decode("#343836"));
		labelTab.setFont( new Font( "Arial", Font.BOLD, ExecutionPanel.FONT_SIZE_BIG ) );
		labelTab.setPreferredSize(new Dimension(tabWidth, tabHeight));
		jtp.addTab(tabName,panel);
		jtp.setTabComponentAt(tabIndex, labelTab);
		jtp.setBackgroundAt(tabIndex,Color.decode("#343836"));
		jtp.setEnabledAt(tabIndex, isEnabled);
	}

	public static void addLogoTab(JTabbedPane jtp,int tabIndex) {
		String tabName="LOGO";
		jtp.addTab(tabName,new JPanel());
		URL iconURL = jtp.getClass().getResource("/images/Logo_rapid.png");
		ImageIcon imageIconLogo=null;
		try{ imageIconLogo=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		int logoWidth=105;
		int logoHeight=48;
		JLabel logoLabel = new JLabel(imageIconLogo);
		logoLabel.setMinimumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setMaximumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setPreferredSize(new Dimension(logoWidth,logoHeight));
		logoLabel.setHorizontalAlignment(JLabel.RIGHT);
		jtp.setTabComponentAt(tabIndex, logoLabel);
		jtp.setBackgroundAt(tabIndex,Color.decode("#343836"));
		jtp.setEnabledAt(tabIndex, false);
	}
	
	public static void addLogo(final JTabbedPane jtp,final SwiftGUIMain swiftGUIMain) {
		Container glassPane = (Container) swiftGUIMain.getRootFrame().getRootPane().getGlassPane();
		glassPane.setVisible(true);
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 15);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		URL iconURL = jtp.getClass().getResource("/images/Logo_rapid.png");
		ImageIcon imageIconLogo=null;
		try{ imageIconLogo=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		int logoWidth=105;
		int logoHeight=48;
		JLabel logoLabel = new JLabel(imageIconLogo);
		logoLabel.setMinimumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setMaximumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setPreferredSize(new Dimension(logoWidth,logoHeight));
		logoLabel.setHorizontalAlignment(JLabel.RIGHT);
		glassPane.add(logoLabel, gbc); 
	}

	public static JButton addMenuTab(final JTabbedPane jtp,final SwiftGUIMain swiftGUIMain,final String productName,int tabIndex,int tabHeight) {
		ImageIcon ii=null;
		URL iconURL =null;
		
		JButton menuButton =new JButton();		
		iconURL = swiftGUIMain.getClass().getResource("/images/snapshot/icon_menu.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		menuButton.setIcon(ii);
		menuButton.setRolloverEnabled(true);
		iconURL = swiftGUIMain.getClass().getResource("/images/snapshot/icon_menu_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		menuButton.setRolloverIcon(new RolloverIcon(ii));
		menuButton.setOpaque(false);
		Dimension dim=new Dimension(50,50);
		menuButton.setSize(dim);
		menuButton.setFocusPainted(true);
		menuButton.setBorderPainted(false);
		menuButton.setContentAreaFilled(false);
		
		int menuItemsHeight=160;

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(BorderFactory.createLineBorder(ExecutionPanel.BLUE_BACKGROUND_COLOR,1));

		JMenuItem supportTicketMenuItem = new JMenuItem("Raise a Support Ticket");
		swiftGUIMain.addSupportTicketListener(supportTicketMenuItem,SnapshotMain.SUPPORT_TICKET_R4C_URL);
		supportTicketMenuItem.setOpaque(true);
		supportTicketMenuItem.setPreferredSize(new Dimension(200, tabHeight));
		supportTicketMenuItem.setForeground(Color.decode("#FFFFFF") );
		supportTicketMenuItem.setBackground(Color.decode("#343836"));
		supportTicketMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );

		JMenuItem registerMenuItem = new JMenuItem();
		registerMenuItem.setText("Register");
		registerMenuItem.setOpaque(true);
		registerMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		registerMenuItem.setForeground(Color.decode("#FFFFFF") );
		registerMenuItem.setBackground(Color.decode("#343836"));
		registerMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		registerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionOpenRegistrationWindow(swiftGUIMain,productName);
			}
		});

		JMenuItem userGuideMenuItem = new JMenuItem("User Guide");
		userGuideMenuItem.setOpaque(true);
		userGuideMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		userGuideMenuItem.setForeground(Color.decode("#FFFFFF") );
		userGuideMenuItem.setBackground(Color.decode("#343836"));
		userGuideMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		userGuideMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionOpenUserGuide(productName);
			}
		});

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setOpaque(true);
		exitMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		exitMenuItem.setForeground(Color.decode("#FFFFFF") );
		exitMenuItem.setBackground(Color.decode("#343836"));
		exitMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swiftGUIMain.close();
			}
		});

		popupMenu.add(supportTicketMenuItem);
		popupMenu.add(registerMenuItem);
		popupMenu.add(userGuideMenuItem);
		popupMenu.add(exitMenuItem);

		menuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Component source = (Component)evt.getSource(); 
				Dimension size = source.getSize();
				int xPos = source.getX()-13;//((size.width - popupMenu.getPreferredSize().width) / 2); 
				int yPos = size.height;
				popupMenu.show(source, xPos, yPos);
			}
		});
		
		JPanel panel=new JPanel();
		panel.setOpaque(true);
		panel.add(menuButton);	
		jtp.addTab("",new JPanel());
		jtp.setTabComponentAt(tabIndex, menuButton);
		jtp.setEnabledAt(tabIndex,false);
				
		return menuButton;
	}
	
	protected static void processActionOpenUserGuide(String applicationName) {
		try{
			File file=new File("docs"+File.separator+"rapid"+applicationName+File.separator+"Rapid"+applicationName+" - User Guide.pdf");
			Desktop.getDesktop().open(file);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}
	
	protected static void processActionOpenRegistrationWindow(SwiftGUIMain swiftGUIMain,String applicationName) {
		ModelUtils.createRegistrationWindow(swiftGUIMain.getRootFrame(),applicationName,true,true,
			null,swiftGUIMain.getApplicationIconPath());
	}

	public static void writeToFile(File file,String content) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(content);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(out != null){
				out.close();
			}
		} 
	}

	public static void assignArialPlainFont(JComponent component,int fontSize){
		assignFont(component,"Arial",Font.PLAIN,fontSize);
	}

	public static void assignArialBoldFont(JComponent component,int fontSize){
		assignFont(component,"Arial",Font.BOLD,fontSize);
	}

	public static void assignFont(JComponent component,String fontName,int fontStyle,int fontSize){
		Font boldFont = new Font(fontName, fontStyle,fontSize);
		component.setFont(boldFont);
	}

	public static JToolTip getCustomToolTip(JToolTip tip ) {
		tip.setForeground(Color.decode("#FFFFD9"));
		tip.setBackground(Color.decode("#707070"));
		tip.setFont(new Font("Arial", Font.PLAIN,InjectMain.FONT_SIZE_BIG));
		return tip;
	}
	
	public static Set<String> getAnyChildLevelInventoryNameSet(Map<String, Inventory> inventoryMap,Inventory sourceInventory) {
		Iterator<String> iterator=inventoryMap.keySet().iterator();
		Set<String> toReturn=new HashSet<String>();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			if (sourceInventory.getName().equals(inventoryName)) {
				continue;
			}
			Inventory inventory=inventoryMap.get(inventoryName);
			boolean isChild=isChildOf(inventoryMap,sourceInventory,inventory);
			if (isChild) {
				toReturn.add(inventory.getName());
			}
		}
		return toReturn;
	}
	
	public static boolean isChildOf(Map<String, Inventory> inventoryMap,Inventory sourceInventory,Inventory targetInventory) {
		String parentName=targetInventory.getParentName();
		if (parentName==null || parentName.isEmpty()) {
			return false;
		}
		if (parentName.equals(sourceInventory.getName())) {
			return true;
		}
		Inventory parentInventory=inventoryMap.get(parentName);
		return isChildOf(inventoryMap,sourceInventory,parentInventory);
	}
	
	public static JPanel getXPanel(float alignment) {
		JPanel tempPanel = new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tempPanel.setAlignmentX(alignment);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		return tempPanel;
	}
	
	public static JPanel getYPanel(float alignment) {
		JPanel tempPanel = new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tempPanel.setAlignmentX(alignment);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		return tempPanel;
	}

	public static void setSize(JComponent component,int width,int height) {
		component.setMinimumSize(new Dimension(width,height));
		component.setPreferredSize(new Dimension(width, height));
		component.setMaximumSize(new Dimension(width, height));
	}
	
	public static FusionDataType getFusionDataType(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		File scriptFolder=new File(tempOutputFolder,scriptLocation);
		File dataLinksFile=new File(scriptFolder,"dataLinkage.xml");
		FusionDataDocument fusionDataDocument=validateDataLinkageDocument(dataLinksFile);
		FusionDataType fusionDataType=fusionDataDocument.getFusionData();
		return fusionDataType;
	}
	
	public static List<Inventory> getChildren(InjectMain injectMain,ScriptGridTracker scriptGridTracker,String parentInventoryName) throws Exception {
		Iterator<String> iterator=injectMain.getExecutionPanelUI().getExecutionTabPanel().getInventoryMap(scriptGridTracker).keySet().iterator();
		List<Inventory> toReturn=new ArrayList<Inventory>();
		while (iterator.hasNext()) {
			String inventoryName=(String) iterator.next();
			Inventory inventoryTemp=injectMain.getExecutionPanelUI().getExecutionTabPanel().getInventoryMap(scriptGridTracker).get(inventoryName);
			String parentName=inventoryTemp.getParentName();
			if (parentName!=null && parentName.equals(parentInventoryName)) {
				toReturn.add(inventoryTemp);
			}
		}
		return toReturn;
	}
	
	public static List<String[]> parseXMLDataFile(File file) throws Exception {
		XmlDataParser xmlDataParser = new XmlDataParser(null,false);
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		FileInputStream inputStream=null;
		try	{
			inputStream = new FileInputStream(file);
			parser.parse(new InputSource(new InputStreamReader(inputStream, CoreConstants.CHARACTER_SET_ENCODING)),xmlDataParser);
			return xmlDataParser.getRows();
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}
		
	public static String getValueFromDataRow(Inventory inventory,String columnName,String[] dataRow) throws Exception {
		int index=inventory.getFieldIndex(columnName);
		if (index==-1) {
			throw new Exception("Cannot find the column name: '"+columnName+"' in the inventory: '"+inventory.getName()+"'");
		}
		return dataRow[index];
	}
	
	public static String getConcatenatedTextNodes(XmlObject xmlObject) throws Exception {
		XmlCursor newCursor = xmlObject.newCursor();
		String nodeName=xmlObject.getDomNode().getNodeName();
		StringBuffer toReturn=new StringBuffer("");
		while (!newCursor.toNextToken().isNone()) {
			Node node=newCursor.getDomNode();
			String currentNodeName=node.getNodeName();
			//System.out.println("Token type: #"+newCursor.currentTokenType()+"# getNodeName:"+currentNodeName);
			if (newCursor.currentTokenType().equals(TokenType.TEXT)) {
				String textValue=newCursor.getTextValue();
				//System.out.println("Token type: " + newCursor.currentTokenType() +" / " + newCursor.xmlText()+" getTextValue():"+textValue);
				toReturn.append(textValue);
			}			
			if (newCursor.currentTokenType().equals(TokenType.END) && currentNodeName.equals(nodeName)) {
				break;
			}
		}
		return toReturn.toString();
	}
	
	public static String replaceNodeValueInventoryAndColumnNamesByValue(BatchInjectionTracker batchInjectionTracker,
			XmlObject xmlObject,String nodeValue,boolean isReplaceSingleQuotes) throws Exception {		
		XmlCursor newCursor = xmlObject.newCursor();
		String nodeName=xmlObject.getDomNode().getNodeName();
		StringBuffer toReturn=new StringBuffer("");
		boolean hasReferenceToValueTags=false;
		boolean isIncludeMixedText=false;
		
		if (nodeName.equalsIgnoreCase("findElement") || nodeName.equalsIgnoreCase("findElements") || 
				nodeName.equalsIgnoreCase("parameter")
				|| nodeName.equalsIgnoreCase("evaluateXPATHBoolean")
				|| nodeName.equalsIgnoreCase("templateStartSection")
				 ) {
			isIncludeMixedText=true;
		}
		//batchInjectionTracker.getWorker().println("@@@@@@@@@@@ isIncludeMixedText:"+isIncludeMixedText+" hasReferenceToValueTags:"+hasReferenceToValueTags);
		String specialStr="#RES#";
		while (!newCursor.toNextToken().isNone()) {
			Node node=newCursor.getDomNode();
			String currentNodeName=node.getNodeName();
			//batchInjectionTracker.getWorker().println("Token type: #"+newCursor.currentTokenType()+"# getNodeName:"+currentNodeName);
			
			//batchInjectionTracker.getWorker().println("applyParameters, isIncludeMixedText:"+isIncludeMixedText);
			if (isIncludeMixedText && newCursor.currentTokenType().equals(TokenType.TEXT)) {
				String textValue=newCursor.getTextValue();
				//batchInjectionTracker.getWorker().println("textValue: '"+textValue+"'");

				if (batchInjectionTracker.getWorker() instanceof SeleniumWorker) {
					SeleniumWorker seleniumWorker=(SeleniumWorker)batchInjectionTracker.getWorker();
					textValue=SeleniumUtils.applyParameters(batchInjectionTracker.getWorker() ,textValue,seleniumWorker.getParameterNameToValueMap());
				}
				toReturn.append(textValue);
				//batchInjectionTracker.getWorker().println("Appending: '"+textValue+"' toReturn: '"+toReturn+"'");
			}
			
			if (newCursor.currentTokenType().equals(TokenType.START) && currentNodeName.equalsIgnoreCase("VALUETEXT")) {
				String textValue=newCursor.getTextValue();
				//batchInjectionTracker.getWorker().println("VALUETEXT textValue(): '"+textValue+"'");
				toReturn.append(textValue);
				//batchInjectionTracker.getWorker().println("Appending: '"+textValue+"' toReturn: '"+toReturn+"'");
				hasReferenceToValueTags=true;
			}
			
			if (newCursor.currentTokenType().equals(TokenType.START) && currentNodeName.equalsIgnoreCase("VALUEVARIABLE")) {
				if (batchInjectionTracker.getWorker() instanceof SeleniumWorker) {
					SeleniumWorker seleniumWorker=(SeleniumWorker)batchInjectionTracker.getWorker();
					Map<String, Object> variableNameToValueMap=seleniumWorker.getVariableNameToValueMap();
					Map<String, DefineVariableType.Type.Enum> variableNameToTypeMap=seleniumWorker.getVariableNameToTypeMap();
					Element e = (Element)node;
					String variableName = e.getAttribute("name");
					//System.out.println("VALUEVARIABLE -  variableName:" + variableName);
					Object value=variableNameToValueMap.get(variableName);
					//System.out.println("VALUEVARIABLE -  value:" + value);
					DefineVariableType.Type.Enum type=variableNameToTypeMap.get(variableName);
					if (type==DefineVariableType.Type.TEXT) {
						String valueString=(String)value;
						toReturn.append(valueString);
						hasReferenceToValueTags=true;
						//System.out.println("Appending: #"+valueString+"#");
					}
				}
			}
			
			if (newCursor.currentTokenType().equals(TokenType.START) && currentNodeName.equalsIgnoreCase("VALUEKB")) {
				Element e = (Element)node;
				String inventoryName = e.getAttribute("inventoryName");
				if (batchInjectionTracker.getWorker() instanceof SeleniumWorker) {
					SeleniumWorker seleniumWorker=(SeleniumWorker)batchInjectionTracker.getWorker();
					inventoryName=SeleniumUtils.applyParameters(batchInjectionTracker.getWorker() ,inventoryName,seleniumWorker.getParameterNameToValueMap());
				}
				String columnName = e.getAttribute("columnName");
				InjectMain injectMain=batchInjectionTracker.getScriptGridTracker().getInjectMain();
				if (batchInjectionTracker.getWorker() instanceof SeleniumWorker) {
					SeleniumWorker seleniumWorker=(SeleniumWorker)batchInjectionTracker.getWorker();
					columnName=SeleniumUtils.applyParameters(batchInjectionTracker.getWorker() ,columnName,seleniumWorker.getParameterNameToValueMap());
				}
				String value=getDataValue(injectMain,batchInjectionTracker,inventoryName,columnName);
				String removeAllSpaces = e.getAttribute("removeAllSpaces");
				if ( removeAllSpaces!=null && removeAllSpaces.equalsIgnoreCase("true")) {
					value= value.replaceAll("\\s","");
				}
				String toLowerCase = e.getAttribute("toLowerCase");
				if ( toLowerCase!=null && toLowerCase.equalsIgnoreCase("true")) {
					value= value.toLowerCase();
				}
				String toUpperCase = e.getAttribute("toUpperCase");
				if ( toUpperCase!=null && toUpperCase.equalsIgnoreCase("true")) {
					value= value.toUpperCase();
				}
				String toNumber = e.getAttribute("toNumber");
				if ( toUpperCase!=null && toNumber.equalsIgnoreCase("true")) {
					try{
						int valInt = Integer.parseInt(value);
						DecimalFormat formatter = new DecimalFormat("#,###");
						value=formatter.format(valInt);
					}
					catch(NumberFormatException ex) {
						batchInjectionTracker.getWorker().println("WARNING!!! Unable to convert text to number: '"+value+"'");
					}
				}

				//batchInjectionTracker.getWorker().println("BEFORE value:%"+value+"%");
				int indexOf=value.indexOf("'");
				if (indexOf!=-1 && isReplaceSingleQuotes) {
					// http://www.seleniumtests.com/2010/08/xpath-and-single-quotes.html
					String tmp=value;
					StringBuffer concatenatedString=new StringBuffer("");
					
					concatenatedString.append(specialStr);
					concatenatedString.append("concat(''");
					while (tmp.indexOf("'")!=-1) {
						indexOf=tmp.indexOf("'");
						String prefix=tmp.substring(0,indexOf);
						concatenatedString.append(",'"+prefix+"'");
						concatenatedString.append(",\"'\"");
						tmp=tmp.substring(indexOf+1);
					}
					concatenatedString.append(",'"+tmp+"')");
					concatenatedString.append(specialStr);
					value=concatenatedString.toString();
				}
				//batchInjectionTracker.getWorker().println("AFTER value:%"+value+"%");
				
				//System.out.println("value:"+value);
				toReturn.append(value);
				hasReferenceToValueTags=true;
			}
			
			if (newCursor.currentTokenType().equals(TokenType.END) && currentNodeName.equals(nodeName)) {
				break;
			}
		}
		
				
		if (hasReferenceToValueTags) {
			String result=toReturn.toString();
			// We must remove the first and last single quotes before/ after CONCAT if CONCAT was used:
			result=result.replaceAll("'"+specialStr,"");
			result=result.replaceAll(specialStr+"'","");
			//System.out.println("result:"+result);
			//batchInjectionTracker.getWorker().println("######replaceNodeValueInventoryAndColumnNamesByValue, toReturn: '"+toReturn.toString()+"'");
			return result;
		}
		if (batchInjectionTracker.getWorker() instanceof SeleniumWorker) {
			SeleniumWorker seleniumWorker=(SeleniumWorker)batchInjectionTracker.getWorker();
			nodeValue=SeleniumUtils.applyParameters(batchInjectionTracker.getWorker() ,nodeValue,seleniumWorker.getParameterNameToValueMap());
			//batchInjectionTracker.getWorker().println("######replaceNodeValueInventoryAndColumnNamesByValue, nodeValue: '"+nodeValue+"'");
		}
		return nodeValue;
	}
	
	public static String getKBColumnName(XmlObject xmlObject) throws Exception {		
		XmlCursor newCursor = xmlObject.newCursor();
		String nodeName=xmlObject.getDomNode().getNodeName();
		
		while (!newCursor.toNextToken().isNone()) {
			Node node=newCursor.getDomNode();
			String currentNodeName=node.getNodeName();
					
			if (newCursor.currentTokenType().equals(TokenType.START) && currentNodeName.equalsIgnoreCase("VALUEKB")) {
				Element e = (Element)node;
				String columnName = e.getAttribute("columnName");
				return columnName;
			}
			
			if (newCursor.currentTokenType().equals(TokenType.END) && currentNodeName.equals(nodeName)) {
				break;
			}
		}
		return null;
	}

	public static String getDataValue(InjectMain injectMain,BatchInjectionTracker batchInjectionTracker,String inventoryName,String columnName) throws Exception {		
		List<String[]> dataRows=injectMain.getExecutionPanelUI().getExecutionTabPanel().getDataRows(batchInjectionTracker.getScriptGridTracker(), inventoryName);
		int currentRowIndex=batchInjectionTracker.getCurrentDataRowAbsoluteIndex(inventoryName);
		String[] dataRow=dataRows.get(currentRowIndex);

		Inventory inventory=injectMain.getExecutionPanelUI().getExecutionTabPanel().getInventory(batchInjectionTracker.getScriptGridTracker(),inventoryName);
		String value=getValueFromDataRow(inventory,columnName,dataRow);

		return value;
	}
	
	private static File createTempInjectionPackageFolder(InjectMain injectMain,File archiveFile) throws Exception {
		File tempOutputFolder = new File(injectMain.getInjectTempFolder(),ScriptManager.FUSIONS_ZIP_TEMP_FOLDER);
		tempOutputFolder.mkdirs();
		tempOutputFolder = new File(tempOutputFolder,archiveFile.getName());
		tempOutputFolder.mkdirs();
		
		return tempOutputFolder;
	}

	private static File getTempInjectionPackageFolder(InjectMain injectMain,File archiveFile) throws Exception {
		File tempOutputFolder = new File(injectMain.getInjectTempFolder(),ScriptManager.FUSIONS_ZIP_TEMP_FOLDER);
		tempOutputFolder = new File(tempOutputFolder,archiveFile.getName());
		tempOutputFolder = new File(tempOutputFolder,"fusionArchive");
	
		return tempOutputFolder;
	}

	public static void unpackInjectionPackage(InjectMain injectMain,File archiveFile,InjectionPackageLoadDialog injectionPackageLoadDialog) throws Exception {
		File tempOutputFolder = createTempInjectionPackageFolder(injectMain,archiveFile);
		//SevenZipUtils.unpackFrom7zFile(archiveFile,tempOutputFolder);
		//APIDataGridOptionsFrame.unzipBWPFile(archiveFile,tempOutputFolder);
		File targetFile=archiveFile;
		if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(archiveFile) )
		{
			targetFile=new File(tempOutputFolder,archiveFile.getName());
			APIDataGridOptionsFrame.decryptFile(archiveFile,targetFile);
		}
		unpackFromZIPFile(targetFile,tempOutputFolder,injectionPackageLoadDialog);
	}
	
	@SuppressWarnings("unchecked")
	public static void unpackFromZIPFile(File archiveFile,File destinationFolder,InjectionPackageLoadDialog injectionPackageLoadDialog)
			throws Exception
	{
		ZipFile zipFile=null;
		try{
			zipFile=new ZipFile(archiveFile);
			Enumeration<ZipEntry> enumeration=(Enumeration<ZipEntry>) zipFile.entries();
			int counter=0;
			injectionPackageLoadDialog.setTotalSteps(zipFile.size()+10);
			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry=enumeration.nextElement();
				counter++;
				if (counter % 100 == 0) {
					String text="Unpacking, in progress: "+Utils.formatNumberWithComma(counter)+" / "+
							Utils.formatNumberWithComma(zipFile.size())+" ...";
					injectionPackageLoadDialog.updateExecutionLabels(text,counter);
				}
				if (zipEntry.isDirectory()) {
					continue;
				}
				File destFile = new File(destinationFolder,zipEntry.getName());
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();

				InputStream is = null;
				OutputStream os = null;
				try
				{
					is = zipFile.getInputStream(zipEntry);
					os = new BufferedOutputStream(new FileOutputStream(destFile));
					org.apache.commons.io.IOUtils.copy(is, os);
				}
				finally
				{
					IOUtils.closeQuietly(is);
					IOUtils.closeQuietly(os);
				}

			}
		}
		finally {
			if (zipFile!=null) {
				zipFile.close();
			}
		}
	}
	
	public static List<String> getInventoryNames(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		List<String> toReturn=new ArrayList<String>();
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			String inventoryName=inventoryMapType.getName();
			toReturn.add(inventoryName);
		}
		return toReturn;
	}
	
	public static Map<String, Inventory> getAllInventoryNameToInventoryMap(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		Map<String,Inventory> toReturn=new HashMap<String,Inventory> ();
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			String inventoryName=inventoryMapType.getName();
			File inventoryFile=new File(tempOutputFolder,scriptLocation);
			inventoryFile=new File(inventoryFile,"inventory"+File.separator+inventoryName+".xml");
			Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
			
			toReturn.put(inventory.getName(),inventory);
		}
		return toReturn;
	}
	
	public static Inventory getRootInventory(ScriptGridTracker scriptGridTracker,Map<String, Inventory> inventoryNameToInventoryMap) throws Exception {
		Iterator<String> invIterator=inventoryNameToInventoryMap.keySet().iterator();
		while ( invIterator.hasNext()) {
			String inventoryName=invIterator.next();
			Inventory inventory=inventoryNameToInventoryMap.get(inventoryName);
			if (inventory.getParentName()==null || inventory.getParentName().isEmpty()) {
				return inventory;
			}
		}
		throw new Exception("Invalid Package: cannot find root inventory for script: '"+scriptGridTracker.getScript().getName()+"'");
	}
	
	public static List<Inventory> getRootInventories(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		List<Inventory> toReturn=new ArrayList<Inventory>();
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			if (inventoryMapType.getIsRootInventory()) {
				String inventoryName=inventoryMapType.getName();
				File inventoryFile=new File(tempOutputFolder,scriptLocation);
				inventoryFile=new File(inventoryFile,"inventory"+File.separator+inventoryName+".xml");
				Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
				toReturn.add(inventory);
			}
		}
		if (toReturn.isEmpty()) {
			throw new Exception("Unable to find the root inventory for the script: '"+script.getName()+"'");
		}
		return toReturn;
	}
	
	public static Inventory getInventory(InjectMain injectMain,File archiveFile,ScriptType script,String inventoryName) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			String inventoryNameTemp=inventoryMapType.getName();
			if (inventoryNameTemp.equals(inventoryName)) {
				File inventoryFile=new File(tempOutputFolder,scriptLocation);
				inventoryFile=new File(inventoryFile,"inventory"+File.separator+inventoryName+".xml");
				Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
				return inventory;
			}
		}
		throw new Exception("Unable to find the inventory: '"+inventoryName+"' for the script: '"+script.getName()+"'");
	}
	
	public static int  getTotalDataRows(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		int totalCount=0;
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			totalCount=totalCount+inventoryMapType.getRecordsCount().intValue();
		}
		return totalCount;
	}
	
	public static List<String[]>  getDataRows(InjectMain injectMain,File archiveFile,ScriptType script,Inventory inventory) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			String inventoryNameTemp=inventoryMapType.getName();
			if (inventoryNameTemp.equalsIgnoreCase(inventory.getName())) {
				File dataFile=new File(tempOutputFolder,scriptLocation);
				dataFile=new File(dataFile,"data"+File.separator+inventoryNameTemp+".xml");
				List<String[]> dataRows=parseXMLDataFile(dataFile);
				return dataRows;
			}
		}
		throw new Exception("Unable to find the data file for the inventory: '"+inventory.getName()+"' for the script: '"+script.getName()+"'");
	}
	
	public static Map<String,List<String[]>>  getAllInventoryNameToDataRowsMap(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		
		Map<String, List<String[]>> toReturn=new HashMap<String, List<String[]>> ();
		for (InventoryMapType inventoryMapType:inventoryMapTypeList) {
			String inventoryName=inventoryMapType.getName();
		
			File dataFile=new File(tempOutputFolder,scriptLocation);
			dataFile=new File(dataFile,"data"+File.separator+inventoryName+".xml");
			List<String[]> dataRows=parseXMLDataFile(dataFile);
			
			toReturn.put(inventoryName,dataRows);
		}
		return toReturn;
	}
	
	public static File getNavigationFile(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		File toReturn=new File(tempOutputFolder,scriptLocation);
		toReturn=new File(toReturn,"navigation"+File.separator+script.getName()+".xml");
		
		return toReturn;
	}
	
	public static FusionScriptsDocument validateScriptsDocument(File fusionScriptFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = getXmlOptions(validationErrors);
		FusionScriptsDocument fusionScriptsDocument =FusionScriptsDocument.Factory.parse(fusionScriptFile,xmlOptions);
		validateXMLBeanDocument(fusionScriptsDocument,xmlOptions,validationErrors);
		return fusionScriptsDocument;
	}
		
	public static FusionScriptType getFusionScriptType(InjectMain injectMain,File archiveFile) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		File scriptFile=new File(tempOutputFolder,"scripts.xml");
		FusionScriptsDocument fusionScriptsDocument=validateScriptsDocument(scriptFile);
		FusionScriptType fusionScriptType=fusionScriptsDocument.getFusionScripts();
		
		return fusionScriptType;
	}
	
	public static InjectionPackageInformationDocument getInjectionPackageInformationDocument(InjectMain injectMain,File archiveFile)   {
		File tempOutputFolder;
		InjectionPackageInformationDocument doc = null;
		try {
			tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
			File inectionPackageInfoFile=new File(tempOutputFolder,"injectionPackageInfo.xml");
			doc = InjectionPackageInformationDocument.Factory.parse(inectionPackageInfoFile);
		} catch (Exception e) {
		}
		return doc;
	}
	
	public static void log(File logFile,Throwable tr,String message,boolean append)
	{
		try {
			log(logFile,tr,message+"\n",append,true);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(File logFile,Throwable tr,String message,boolean append,boolean isDatePrinting) throws IOException
	{
		log(tr, message, append, isDatePrinting, logFile);
	}

	public static String encrypt(String str) throws Exception {
		//TODO: doesn't work
		//return  new String(Encryption.encrypt(str.getBytes()));
		return  str;
	}

	public static String decrypt(String str) throws Exception {
		//	TODO: doesn't work
		//return new String(Encryption.decrypt(str.getBytes()));
		return  str;
	}
	
	private static void log(Throwable tr,String message,boolean append,boolean isDatePrinting, File outputFile) throws IOException
	{
		PrintWriter printWriter=null;
		FileWriter fileWriter=null;
		try {
			fileWriter=new FileWriter(outputFile,append);
			printWriter = new PrintWriter(fileWriter);
			log(tr,message,isDatePrinting, printWriter);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (fileWriter!=null) {
				fileWriter.close();
			}
			if (printWriter!=null) {
				printWriter.close();
			}
		}
	}

	private static void log(Throwable tr,String message,boolean isDatePrinting, PrintWriter writer)
	{
		synchronized(writer)
		{
			try{
				String heading="";
				if (isDatePrinting) {
					String t=CoreConstants.DATE_FORMAT_PATTERN.STANDARD_TIMEZONE.getDateFormat().format(new Date());
					heading=t+": ";
				}
				writer.write(heading);

				if (tr!=null) {
					tr.printStackTrace(writer);
				}
				else {
					writer.write(message);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public static File getScriptLogFolder(ScriptGridTracker scriptGridTracker) {
		File logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(scriptGridTracker.getInjectMain().getApplicationInfoPanel().getInjectionPackage().getName());
		String relativeLogFolderName=scriptGridTracker.getLogFolderName();
		File scriptFolder=new File(logsFolder,relativeLogFolderName);
		return scriptFolder;
	}
	
	public static File getScriptLogFile(ScriptGridTracker scriptGridTracker,String uniqueID,boolean isFriendlyLog){
		File scriptFolder=getScriptLogFolder(scriptGridTracker);
		String logFileName=SeleniumWorker.LOG_FILE_PREFIX+uniqueID+".txt";
		if (isFriendlyLog) {
			logFileName=SeleniumWorker.LOG_FRIENDLY_FILE_PREFIX+uniqueID+".txt";
		}		
		File logFile=new File(scriptFolder,logFileName);
		return logFile;
	}
	
	public static FusionDataDocument validateDataLinkageDocument(File fusionFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = getXmlOptions(validationErrors);
		FusionDataDocument fusionDataDocument =FusionDataDocument.Factory.parse(fusionFile,xmlOptions);
		validateXMLBeanDocument(fusionDataDocument,xmlOptions,validationErrors);
		return fusionDataDocument;
	}
	
	public static List<InventoryMapType> getInventoryMapTypeList(InjectMain injectMain,File archiveFile,ScriptType script) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,archiveFile);
		String scriptLocation=script.getLocation();
		File scriptFolder=new File(tempOutputFolder,scriptLocation);
		File dataLinksFile=new File(scriptFolder,"dataLinkage.xml");
		FusionDataDocument fusionDataDocument=validateDataLinkageDocument(dataLinksFile);
		FusionDataType fusionDataType=fusionDataDocument.getFusionData();
		InventoryMapType[] inventoryMapArray=fusionDataType.getInventoryMapArray();
		
		return Arrays.asList(inventoryMapArray) ;
	}
	
	/*
	 * Example:
	 * If I1 has 2 records linked to 2 records in I2 linked to each 1 record in I3:
	 * 		I1: 0	I2: 0 	I3: 0
	 * 		I1: 0	I2: 1 	I3: 1
	 * 		I1: 1	I2: 2 	I3: 2
	 * 		I1: 1	I2: 3 	I3: 3
	 * 
	 */
	public static List<Map<String, RecordTracker>> getDataLinks(InjectMain injectMain,File archiveFile,ScriptType script,
			Map<String,List<RecordTracker>> inventoryToRecordTrackerMap) throws Exception {
		FusionDataType fusionDataType=getFusionDataType(injectMain,archiveFile,script);
		List<InventoryMapType> inventoryMapTypeList=getInventoryMapTypeList(injectMain,archiveFile,script);
		Map<String,String> codeToInventoryNameMap=new HashMap<String,String>();
		for (InventoryMapType inventoryMap:inventoryMapTypeList){
			String name=inventoryMap.getName();
			String code=inventoryMap.getCode();
			codeToInventoryNameMap.put(code,name);
		}
		LType[] lArray=fusionDataType.getLArray();
		List<Map<String, RecordTracker>> toReturn=new ArrayList<Map<String,RecordTracker>>();
		//int counter=0;
		for (LType l:lArray) {
			//FileUtils.println("getDataLinks, new datalink: "+counter);
			//counter++;
			MType[] mArray=l.getMArray();
			Map<String,RecordTracker> inventoryNameToRecordTrackerMap=new HashMap<String,RecordTracker>();
			boolean hasAtLeastOneMapping=false;
			
			for (MType m:mArray) {
				String code=m.getC();
				int rowIndex=m.getR().intValue();
				String inventoryName=codeToInventoryNameMap.get(code);
				//FileUtils.println("getDataLinks, code:"+code+" rowIndex:"+rowIndex+" inventoryName: '"+inventoryName+"'");
				
				/*
				if (isBasedOnGridSelection) {
					// filter the records based on tablegrid selection
					boolean isSelected=scriptsGrid.isSelected(script.getUniqueID().intValue(),inventoryName,rowIndex);
					//System.out.println("code:"+code+" rowIndex:"+rowIndex+" isSelected:"+isSelected+" inventoryName:"+inventoryName+"@");
					if (!isSelected) {
						FileUtils.println("getDataLinks, WARNING: RECORD IS NOT SELECTED!!!");
						continue;
					}
				}
				*/
				hasAtLeastOneMapping=true;
				List<RecordTracker> recordTrackerList=inventoryToRecordTrackerMap.get(inventoryName);
				RecordTracker recordTracker=recordTrackerList.get(rowIndex);
				inventoryNameToRecordTrackerMap.put(inventoryName, recordTracker);
			}
			if ( hasAtLeastOneMapping){
				toReturn.add(inventoryNameToRecordTrackerMap); 
			}
		}
		return toReturn;
	}
	
	public static NavigationDocument getFusionNavigationDocument(InjectMain injectMain,File injectionPackage,ScriptType scriptType) throws Exception {
		File currentScriptFile=getNavigationFile(injectMain,injectionPackage,scriptType);
		return getFusionNavigationDocument(currentScriptFile);
	}
	
	public static NavigationDocument getExtraFusionNavigationDocument(InjectMain injectMain,File injectionPackage,String navigationName) throws Exception {
		File tempOutputFolder = getTempInjectionPackageFolder(injectMain,injectionPackage);
		File extraFolder=new File(tempOutputFolder,"extra");
		File file=FileUtils.getFile(extraFolder, navigationName+".xml");
		return getFusionNavigationDocument(file);
	}


	@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
	public static JLabel linkify(final JTextComponent hostPortTextComponent,final String hostPort,final String text,final String suffixURL, String toolTip)
	{
	    final JLabel link = new JLabel(){
	          public JToolTip createToolTip() {
		            JToolTip tip = super.createToolTip();
		            return getCustomToolTip(tip);
		          }
		    };
	    link.setText(text);
		link.setForeground(Color.BLUE);//Color.decode("#000099") );
		assignArialPlainFont(link,ExecutionPanel.FONT_SIZE_SMALL);
		Font font = link.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		link.setFont(font.deriveFont(attributes));
		
	    if (!toolTip.isEmpty()) {
	        link.setToolTipText(toolTip);
	    }
	    link.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    link.addMouseListener(new MouseListener()
	    {
	        public void mouseExited(MouseEvent arg0)
	        {
	        }

	        public void mouseEntered(MouseEvent arg0)
	        {
	        }

	        public void mouseClicked(MouseEvent arg0)
	        {
	        	String controllerHost="";
	        	if (hostPortTextComponent!=null) {
	        		int indexOf=hostPortTextComponent.getText().indexOf(":");
	        		if (indexOf==-1) {
	        			controllerHost=hostPortTextComponent.getText();
	        		}
	        		else {
	        			controllerHost=hostPortTextComponent.getText().substring(0,indexOf);
	        		}
	        	}
	        	else {
	        		controllerHost=hostPort;
	        	}
	        	String url="http://"+controllerHost+suffixURL;
	        	
	        	try
	        	{
	        		if (hostPortTextComponent!=null && hostPortTextComponent.getText().isEmpty()) {
	        			GUIUtils.popupErrorMessage("You must enter the server ID.");
	        			return;
	        		}
	        		URI uri = URI.create(url);
	        		DesktopApi.browse( uri );
	        	}
	        	catch (Exception e)
	        	{
	        		FileUtils.printStackTrace(e);
	        		GUIUtils.popupErrorMessage(e.getMessage());
	        	}
	        }

	        public void mousePressed(MouseEvent e)
	        {
	        }

	        public void mouseReleased(MouseEvent e)
	        {
	        }
	    });
	  
	    return link;
	}
	
	public static String getErrors(List<XmlValidationError> validationErrors) {
		Iterator<XmlValidationError> iterator = validationErrors.iterator();
		StringBuffer toReturn=new StringBuffer("");
		while (iterator.hasNext())
		{
			XmlValidationError xmlValidationError=iterator.next();
			toReturn.append("XML VALIDATION ERROR AT LINE: "+xmlValidationError.getLine()+ "\n");
			toReturn.append("====================================\n");
			toReturn.append(xmlValidationError+ "\n");
		}
		return toReturn.toString();
	}
	

	public static XmlOptions getXmlOptions(List<XmlValidationError> validationErrors) throws Exception {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setErrorListener(validationErrors);
		xmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS);
		return xmlOptions;
	}
	
	public static void validateXMLBeanDocument(XmlObject document,XmlOptions xmlOptions,List<XmlValidationError> validationErrors) throws Exception {
		boolean isValid =document.validate(xmlOptions);
		if (!isValid)	{
			String errors=getErrors(validationErrors);
			FileUtils.println(errors);
			throw new Exception("INVALID XML DOCUMENT: IT DOES NOT CONFORM TO THE XSD SPECIFICATION!!! Errors:\n"+errors);
		}
	}

	public static NavigationDocument validateNavigationDocument(File fusionNavigationFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = getXmlOptions(validationErrors);
		NavigationDocument fusionNavigationDocument =NavigationDocument.Factory.parse(fusionNavigationFile,xmlOptions);
		validateXMLBeanDocument(fusionNavigationDocument,xmlOptions,validationErrors);
		return fusionNavigationDocument;
	}	

	public static NavigationDocument getFusionNavigationDocument(File fusionNavigationFile) throws Exception {
		NavigationDocument fusionNavigationDocument =validateNavigationDocument(fusionNavigationFile);
		return fusionNavigationDocument;
	}
	
	public static List<XmlObject> parseFusionNavigationDocument(Navigation navigation) throws Exception {
		FusionNavigationType fusionNavigation=navigation.getFusionNavigation();
		XmlObject[] xmlObjects=fusionNavigation.selectPath("*");

		return Arrays.asList(xmlObjects);
	}
	
	public static List<XmlObject> parseFusionWebServiceNavigationDocument(NavigationDocument fusionNavigationDocument) throws Exception {
		Navigation navigation=fusionNavigationDocument.getNavigation();	
		FusionWebServiceNavigationType fusionNavigation=navigation.getFusionWebServiceNavigation();
		XmlObject[] xmlObjects=fusionNavigation.selectPath("*");

		return Arrays.asList(xmlObjects);
	}
	
	public static TitledBorder getTitleBorder(String title) {
		Border borderLine = BorderFactory.createLineBorder(Color.decode("#4B4F4E"), 1, true);
		Font titleFont = new Font("Arial", Font.PLAIN,InjectMain.FONT_SIZE_BIG);
	    Color titleColor =Color.black;
		TitledBorder border = BorderFactory.createTitledBorder(borderLine,title, TitledBorder.LEFT, TitledBorder.TOP,titleFont,titleColor);  
		
		return border;
	}
	
	public static void saveScriptsGridToExcel(File outputFile,List<ScriptGridTracker> scriptGridTrackerList) throws Exception {
		List<String[]> rows=new ArrayList<String[]>();
		int counter=0;
		int totalColumnsToDisplay=17;
		for (ScriptGridTracker scriptGridTracker:scriptGridTrackerList) {
			counter++;
			int index=0;
			String[] row=new String[totalColumnsToDisplay];
			row[index++]=""+counter;
			
			String val=scriptGridTracker.getDisplayName();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			val=scriptGridTracker.getDisplayType();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			val=scriptGridTracker.getStatus();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			DecimalFormat df = new DecimalFormat("#%");
			val=df.format(scriptGridTracker.getPercentageComplete());
			row[index++]=val;

			val=""+scriptGridTracker.getTotalRecords();
			row[index++]=val;
			
			val=""+scriptGridTracker.getTotalRemainingRecords();
			row[index++]=val;
			
			val=""+scriptGridTracker.getTotalSuccessRecords();
			row[index++]=val;
			
			val=""+scriptGridTracker.getTotalFailedRecords();
			row[index++]=val;
			
			int valInt=scriptGridTracker.getBatchSize();
			if (valInt==-1) {
				val="";
			}
			else {
				val=""+valInt;
			}
			row[index++]=val;
			
			valInt=scriptGridTracker.getExecutionRetries();
			if (valInt==0) {
				val="";
			}
			else {
				val=""+valInt;
			}
			row[index++]=val;
			
			valInt=scriptGridTracker.getTotalBatchCount();
			if (valInt==0) {
				val="";
			}
			else {
				val=""+valInt;
			}
			row[index++]=val;
			
			valInt=scriptGridTracker.getCompletedBatchCount();
			if (valInt==0) {
				val="";
			}
			else {
				val=""+valInt;
			}
			row[index++]=val;
			
			val=scriptGridTracker.getRemarks();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			val=scriptGridTracker.getExecutionTotalTime();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			val=scriptGridTracker.getExecutionStartTime();
			if (val==null) {
				val="";
			}
			row[index++]=val;
			
			val=scriptGridTracker.getExecutionEndTime();
			if (val==null) {
				val="";
			}
			row[index++]=val;

			rows.add(row);
		}
		String[] headerRow=new String[totalColumnsToDisplay];
		int index=0;
		String NEW_LINE="\n";
		
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_TYPE.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_PERCENTAGE.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_TOTAL_RECORDS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_REMAINING_RECORDS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_SUCCESS_RECORDS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_FAILED_RECORDS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_SCRIPT_BATCH_SIZE.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_RETRIES.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_TOTAL_BATCH_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_COMPLETED_BATCH_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_TOTAL_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_START_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ScriptsGrid.COLUMN_HEADING_END_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		
		rows.add(0,headerRow);

		String sheetTitle="Status";
		ModelUtils.doCreateAndSaveXLSXExcelFile(sheetTitle,rows,outputFile, false, false);
	}
	
}
