package com.rapidesuite.inject.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class JPanelWithBackground extends JPanel {
	
	private static final long serialVersionUID = 1L;
	Image imageOrg = null;

	public JPanelWithBackground(Image image2) {
		imageOrg = image2;
		setOpaque(false);
		Dimension size = new Dimension(imageOrg.getWidth(null), imageOrg.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		//setLayout(null);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(imageOrg, 0, 0, null);
	}
	
}
