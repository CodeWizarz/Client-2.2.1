package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class TabbedPaneUI extends BasicTabbedPaneUI{

	private final Insets borderInsets = new Insets(0, 0, 0, 0);
	private int tabIndexGreaterThanNotDisplayWhiteBar;
	
	public TabbedPaneUI(int tabIndexGreaterThanNotDisplayWhiteBar) {
		this.tabIndexGreaterThanNotDisplayWhiteBar=tabIndexGreaterThanNotDisplayWhiteBar;
	}
	
	@SuppressWarnings({ "unused", "serial" })
	private class ScrollableTabPanel extends JPanel implements UIResource {
		public ScrollableTabPanel() {
			setLayout(null);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);  
		}
	}

	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
	}

	@Override
	protected Insets getContentBorderInsets(int tabPlacement) {
		return borderInsets;
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		if (tabIndex>tabIndexGreaterThanNotDisplayWhiteBar) {
			return;
		}
		g.setColor(Color.WHITE);
		if (isSelected && tabIndex!=0) {
			g.drawLine(x-1, y, x-1, y+h);
		}
		g.drawLine( x-1+w, y, x-1+w, y+h);
		//g.drawLine(x + 1, y + 1, x + w - (h / 2), y + 1);
	}    

	@Override
	protected void paintFocusIndicator(Graphics g,int tabPlacement,Rectangle[] rects,int tabIndex,Rectangle iconRect,Rectangle textRect,boolean isSelected) {
	}

}