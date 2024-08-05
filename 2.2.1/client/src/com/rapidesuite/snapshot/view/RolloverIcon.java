package com.rapidesuite.snapshot.view;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class RolloverIcon implements Icon
{
  protected Icon icon;
  float alpha = 0.7f;
  
  public RolloverIcon(Icon icon)
  {
    this.icon = icon;
  }

  public int getIconHeight()
  {
    return icon.getIconHeight();
  }

  public int getIconWidth()
  {
    return icon.getIconWidth();
  }

  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    Graphics2D graphics2d = (Graphics2D) g;
    Composite oldComposite = graphics2d.getComposite();
    graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    icon.paintIcon(c, g, x, y);
    graphics2d.setComposite(oldComposite);
  }
  
}