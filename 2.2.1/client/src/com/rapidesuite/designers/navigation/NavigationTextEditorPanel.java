package com.rapidesuite.designers.navigation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.StringWriter;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class NavigationTextEditorPanel extends JPanel {

	private RSyntaxTextArea textArea;
	private NavigationEditorMain navigationEditorMain;
	private KBPlaceholderPanel kbPlaceholderPanel;
	private int caretPosition;

	private static final String UNSAVED_CHANGED="Unsaved Changes!";
	
	public NavigationTextEditorPanel(final NavigationEditorMain navigationEditorMain) {
		this.navigationEditorMain=navigationEditorMain;
		super.setLayout(new BorderLayout());

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setAutoIndentEnabled(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		
		textArea.addKeyListener(new java.awt.event.KeyAdapter() {
	        public void keyPressed(java.awt.event.KeyEvent evt) {
	            if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_S) {
	            	if (navigationEditorMain.getSavedLabel().getText().equals(UNSAVED_CHANGED)) {
	            		navigationEditorMain.saveNavigation();
	            	}
	            }
	        }
	    });

		RTextScrollPane sp = new RTextScrollPane(textArea);
		add(sp);

		JPopupMenu popup = textArea.getPopupMenu();
		popup.addSeparator();
		
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText("Indent");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeIndent();
			}
		});
		popup.add(menuItem);

		JMenu commandsMenu=new  JMenu("Commands");
		popup.add(commandsMenu);
		popup.addSeparator();

		menuItem = new JMenuItem();
		menuItem.setText("VALUE KB");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeInsertValueKBCommand();
			}
		});
		commandsMenu.add(menuItem);

		menuItem = new JMenuItem();
		menuItem.setText("BLOCK");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeInsertBlockCommand();
			}
		});
		commandsMenu.add(menuItem);
		
		menuItem = new JMenuItem();
		menuItem.setText("EXECUTE BLOCK");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeInsertExecuteBlockCommand();
			}
		});
		commandsMenu.add(menuItem);

		menuItem = new JMenuItem();
		menuItem.setText("TEMPLATE SECTION");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeInsertTemplateSectionCommand();
			}
		});
		commandsMenu.add(menuItem);
	}

	protected void executeInsertBlockCommand() {
		insertCommandBlock("BLOCK_NAME_TO_REPLACE");
	}
	
	protected void executeInsertExecuteBlockCommand() {
		String text = "<executeBlock name=\"BLOCK_NAME_TO_REPLACE\" />\n";
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public void listenToChanges() {
		textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				notifyUnsavedChanges(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				notifyUnsavedChanges(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				notifyUnsavedChanges(e);
			}

			@SuppressWarnings("unused")
			private void notifyUnsavedChanges(DocumentEvent e) {
				navigationEditorMain.getSavedLabel().setText(UNSAVED_CHANGED);
			}

		});
	}


	protected void saveCaretPosition() {
		caretPosition=textArea.getCaretPosition();
	}

	protected void restoreCaretPosition() {
		textArea.setCaretPosition(caretPosition);
	}

	protected void executeIndent() {
		try{
			String oldText=textArea.getText();
			/*
			Source xmlInput = new StreamSource(new StringReader(oldText));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 1);
			Transformer transformer = transformerFactory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			String xmlString =xmlOutput.getWriter().toString();
			System.out.println(xmlString);
			 */
			String xmlString =formatXML(oldText);
			textArea.setText(xmlString);
			System.out.println("Indent completed!");
			restoreCaretPosition();
		}
		catch(Exception e) {
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

	protected void executeInsertText() {
		String text = JOptionPane.showInputDialog(this, "Type some text to insert at current cursor position");
		textArea.insert(text, textArea.getCaretPosition());
	}

	public void setCaretAfterText(String text) {
		try{
			javax.swing.text.Document document = textArea.getDocument();
			int pos=0;
			String find = text.toLowerCase();
			int findLength = find.length();
			boolean found = false;
			if (pos + findLength > document.getLength()) {
				pos = 0;
			}
			while (pos + findLength <= document.getLength()) {
				String match = document.getText(pos, findLength).toLowerCase();
				// Check to see if it matches or request
				if (match.equals(find)) {
					found = true;
					break;
				}
				pos++;
			}

			if (found) {
				System.out.println("found it, pos:"+pos);
				textArea.setCaretPosition(pos + findLength);
				//textArea.moveCaretPosition(pos);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public KBPlaceholderPanel openKBPlaceHolderDialogWindow(boolean isShowColumnName) {
		int width=800;
		int height=200;
		kbPlaceholderPanel=new KBPlaceholderPanel(navigationEditorMain,isShowColumnName);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(navigationEditorMain.getRootFrame(),"VALUEKB",width,height,
				kbPlaceholderPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
		kbPlaceholderPanel.setDialog(dialog);
		dialog.setVisible(true);
		return kbPlaceholderPanel;
	}

	public String formatXML(String input)
	{
		try 
		{
			Document doc = DocumentHelper.parseText(input);  
			StringWriter sw = new StringWriter();  
			OutputFormat format = OutputFormat.createPrettyPrint();  
			format.setIndent(true);
			format.setIndentSize(3); 
			XMLWriter xw = new XMLWriter(sw, format);  
			xw.write(doc);  

			return sw.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return input;
		}
	}

	protected void executeInsertValueKBCommand() {
		openKBPlaceHolderDialogWindow(true);
		if (!kbPlaceholderPanel.isSubmitted()) {
			return;
		}
		//String text = JOptionPane.showInputDialog(this, "Type some text to insert at current cursor position");
		//textArea.insert(text, textArea.getCaretPosition());
		String selectedInventoryName=kbPlaceholderPanel.getSelectedInventoryName();
		String selectedColumnName=kbPlaceholderPanel.getSelectedColumnName();
		String kbPlaceholderText="<valueKB inventoryName=\""+selectedInventoryName+"\" columnName=\""+selectedColumnName+"\"/>";
		textArea.insert(kbPlaceholderText, textArea.getCaretPosition());
	}

	protected String getKBPlaceholderText(String inventoryName,String columnName) {
		Inventory inventory=navigationEditorMain.getInventoryNameToInventoryMap().get(inventoryName);
		String kbPlaceholderText="";
		if (inventory==null) {
			return kbPlaceholderText;
		}
		Field field=inventory.getField(columnName);
		if (field!=null) {
			kbPlaceholderText="<valueKB inventoryName=\""+inventoryName+"\" columnName=\""+columnName+"\"/>";
		}
		return kbPlaceholderText;
	}	

	protected void insertTemplateInput(String labelName) {
		//String kbPlaceholderText=getKBPlaceholderText(inventoryName,labelName);
		//String text = "\n<templateInput label=\""+labelName+"\">"+kbPlaceholderText+"</templateInput>\n";
		String text = "\n<templateInput label=\""+labelName+"\" />\n";
		
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public void insertTemplateSelect(String labelName) {
		//String kbPlaceholderText=getKBPlaceholderText(inventoryName,labelName);
		//String text = "\n<templateSelect label=\""+labelName+"\">"+kbPlaceholderText+"</templateSelect>\n";
		String text = "\n<templateSelect label=\""+labelName+"\" />\n";
		
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public void insertCommandsButton(String textValue,String attributeName) {
		String text = "\n<templateClick type=\"button\" attribute=\""+attributeName+"\" >"+textValue+"</templateClick>\n";
		// //button[text()='']
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public void insertCommandBlock(String textValue) {
		setCaretAfterText(NavigationEditorMain.BLOCKS_COMMENT_LINE);
		String blockLine = "<block name=\""+textValue+"\">";

		String text = "\n"+blockLine+"\n\n"+
				"\n</block>\n";
		textArea.insert(text, textArea.getCaretPosition());
		setCaretAfterText(blockLine);
		saveCaretPosition();
	}

	public void insertTemplateTextArea(String textValue) {
		//String kbPlaceholderText=getKBPlaceholderText(inventoryName,textValue);
		//String text = "\n<templateTextArea label=\""+textValue+"\">"+kbPlaceholderText+"</templateTextArea>\n";
		String text = "\n<templateTextArea label=\""+textValue+"\" />\n";
		
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public void insertTemplateRadioButton(String inventoryName,String textValue) {
		String kbPlaceholderText=getKBPlaceholderText(inventoryName,textValue);
		String text = "\n<templateRadioButton label=\""+textValue+"\" isPageLoadingAfterSet=\"false\" >"+kbPlaceholderText+"</templateRadioButton>\n";
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}

	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	public void insertCommandsAnchorImage(String textValue,String attributeName) {
		// //img[@title='"+parameterValue+"']/..
		String text = "\n<templateClick type=\"anchor_image\" attribute=\""+attributeName+"\" >"+textValue+"</templateClick>\n";
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}
	
	public void insertTemplateGridSearch(String sectionTitle) {
		String text = 
		 "<templateGridSearch sectionTitle=\""+sectionTitle+"\" blockToExecuteIfFound=\"_UPDATE_MODE\" blockToExecuteIfNotFound=\"_INSERT_MODE\">\n"+
         "<gridSearch hasSearchBar=\"true\">\n"+
         "   <gridColumn position=\"1\">\n"+
         "      <!-- INSERT A VALUE KB COMMAND HERE -->\n\n"+ 
         "   </gridColumn>\n"+ 
         "</gridSearch>\n"+ 
         "</templateGridSearch>\n";
      
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}
	
	public void executeInsertTemplateSectionCommand() {
		String text = "<templateStartSection>SECTION_NAME</templateStartSection>\n\n"+
				"<!-- MOVE YOUR SECTION RELATIVE COMMANDS HERE-->\n"+
				"<templateEndSection />\n";
		textArea.insert(text, textArea.getCaretPosition());
		saveCaretPosition();
	}	

}