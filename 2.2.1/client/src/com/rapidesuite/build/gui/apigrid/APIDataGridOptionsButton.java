package com.rapidesuite.build.gui.apigrid;

import javax.swing.ImageIcon;

import org.openswing.swing.client.DataController;
import org.openswing.swing.client.GenericButton;

@SuppressWarnings("serial")
public class APIDataGridOptionsButton extends GenericButton
{

	private APIDataGridController apiDataGridController;

	public APIDataGridOptionsButton(APIDataGridController apiDataGridController, ImageIcon imageIcon)
	{
		super(imageIcon);
		this.apiDataGridController = apiDataGridController;
	}

	protected final void executeOperation(DataController controller) throws Exception
	{
		apiDataGridController.showOptionsPanel();
	}

}