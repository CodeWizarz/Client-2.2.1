/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/DefaultTextTreeNode.java $:
 * $Id: DefaultTextTreeNode.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

@SuppressWarnings("serial")
public class DefaultTextTreeNode extends TextTreeNode  {

	private String internalTextNode;
	
	public DefaultTextTreeNode(String name,String nodePath) {
		super(name,nodePath);
	}
		
	public void initToolTipText() {
		
	}
	
	public void copyResequencedNodesFlat(TextTreeNode targetNode) {
		
	}
	
	public TextTreeNode getShallowCopy() {
		return null;
	}

	public String getInternalTextNode() {
		return internalTextNode;
	}

	public void setInternalTextNode(String internalTextNode) {
		this.internalTextNode = internalTextNode;
	}
	
}


