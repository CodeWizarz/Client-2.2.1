package com.rapidesuite.inject.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageFileOpener extends JPanel{
	
    private ImageIcon backImage;
 
    public ImageFileOpener(String imgPath) {
    	backImage = new ImageIcon(imgPath);
    }
 
    @Override
    public void paintComponent(Graphics g){
    	BufferedImage scaledImage = getScaledImage();
        super.paintComponent(g);
        g.drawImage(scaledImage, 0, 0, null);
    }
    
    private BufferedImage getScaledImage(){
        BufferedImage image = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(backImage.getImage(), 0, 0,getWidth(),getHeight(), null);

        return image;
    }
 
    public static void createFrame(File file) {
        JFrame frame = new JFrame("Screenshot");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageFileOpener it = new ImageFileOpener(file.getAbsolutePath());
        frame.add(it);
        frame.setSize(600,600);
        frame.setVisible(true);
    }
}
