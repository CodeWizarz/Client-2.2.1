package com.horizonlive.tightvnc;

//
//  Copyright (C) 2001-2004 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

//
// VncViewer.java - the VNC viewer applet.  This class mainly just sets up the
// user interface, leaving it to the VncCanvas to do the actual rendering of
// a VNC desktop.
//

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import com.keypoint.PngEncoder;
import com.rapidesuite.panopticon.Config;
import com.rapidesuite.panopticon.Main;

public class VncViewer extends java.applet.Applet
implements java.lang.Runnable, WindowListener
{
	
	double width = 0;
	double height = 0;
	
	boolean inAnApplet = true;
    boolean inSeparateFrame = false;

    //
    // main() is called when run as a java program from the command line.
    // It simply runs the applet inside a newly-created frame.
    //

    public VncViewer()
    {
        this.setVisible(false);
    }



    public static void captureVMScreen(Config.ObservedHost observedHost, File pathToImageOutputFile) throws Exception {
    	captureVMScreen(observedHost, pathToImageOutputFile, true);
    }
    public static VncViewer captureVMScreen(Config.ObservedHost observedHost,
        							   File pathToImageOutputFile, boolean startThread) throws Exception
    {
        VncViewer v = new VncViewer();
        String[] argv = new String[]{"HOST", observedHost.getHost().getHostName(),
        							 "PORT", String.valueOf(observedHost.getPort()),
        							 "PASSWORD", observedHost.getPassword()};

        v.mainArgs = argv;
        v.inAnApplet = false;
        v.inSeparateFrame = true;
        v.pathToImageOutputFile = pathToImageOutputFile;
        
        v.width = Config.getScreenCaptureDimensions().getWidth();
        v.height = Config.getScreenCaptureDimensions().getHeight();

        v.init(startThread);
        v.start();
        return v;
    }

    public volatile boolean screenCaptured = false;
    public File pathToImageOutputFile = null;
    public void captureImageAndDestroyApplet()
    {
        try
        {
            //Main.debugPrint("Entered captureImageAndDestroyApplet for pathToImageOutputFile = " + pathToImageOutputFile);
            //BufferedImage buffer = new BufferedImage(1011,703, BufferedImage.TYPE_INT_RGB);
            BufferedImage buffer = new BufferedImage(vc.getWidth(),vc.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = buffer.createGraphics();
            this.vc.print(g);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PngEncoder png = new PngEncoder(buffer, false, 0, 9);
            byte[] bytes = png.pngEncode();
            FileOutputStream fos = new FileOutputStream(pathToImageOutputFile);
            fos.write(bytes);
            fos.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        try
        {
            this.destroy();
        }
        catch ( Exception e )
        {
            throw new Error(e);
        }
        finally
        {
            //Main.debugPrint("Leaving captureImageAndDestroyApplet for pathToImageOutputFile = " + pathToImageOutputFile);
        }
    }

    String[] mainArgs;

    RfbProto rfb;
    Thread rfbThread;

    Frame vncFrame;
    Container vncContainer;
    ScrollPane desktopScrollPane;
    GridBagLayout gridbag;
    ButtonPanel buttonPanel;
    Label connStatusLabel;
    VncCanvas vc;
    OptionsFrame options;
    ClipboardFrame clipboard;
    RecordingFrame rec;

    // Control session recording.
    Object recordingSync;
    String sessionFileName;
    boolean recordingActive;
    boolean recordingStatusChanged;
    String cursorUpdatesDef;
    String eightBitColorsDef;

    // Variables read from parameter values.
    String socketFactory;
    String host;
    int port;
    String passwordParam;
    boolean showControls;
    boolean offerRelogin;
    boolean showOfflineDesktop;
    int deferScreenUpdates;
    int deferCursorUpdates;
    int deferUpdateRequests;

    // Reference to this applet for inter-applet communication.
    public static java.applet.Applet refApplet;

    //
    // init()
    //

    public void init() {
    	init(true);
    }
    
    public void init(boolean startThread)
    {
        this.setVisible(false);

        readParameters();

        refApplet = this;

        if ( inSeparateFrame )
        {
            vncFrame = new Frame("GIANT SALMANDER");
            vncFrame.setVisible(false);
            if ( !inAnApplet )
            {
                vncFrame.add("Center", this);
                this.setVisible(false);
            }
            vncContainer = vncFrame;
        }
        else
        {
            vncContainer = this;
        }
        vncContainer.setVisible(false);
        recordingSync = new Object();

        options = new OptionsFrame(this);
        clipboard = new ClipboardFrame(this);
        if ( RecordingFrame.checkSecurity() )
            rec = new RecordingFrame(this);

        sessionFileName = null;
        recordingActive = false;
        recordingStatusChanged = false;
        cursorUpdatesDef = null;
        eightBitColorsDef = null;

        if ( inSeparateFrame )
            vncFrame.addWindowListener(this);

        rfbThread = new Thread(this);
        if(startThread) {
        	rfbThread.start();
        }
        this.setVisible(false);

    }

    public void update(Graphics g)
    {
    }

    //
    // run() - executed by the rfbThread to deal with the RFB socket.
    //

    public void run()
    {
//    	System.out.println(Thread.currentThread().getName());
        gridbag = new GridBagLayout();
        vncContainer.setLayout(gridbag);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        if ( showControls )
        {
            buttonPanel = new ButtonPanel(this);
            gridbag.setConstraints(buttonPanel, gbc);
            vncContainer.add(buttonPanel);
        }

        boolean exceptionsWereThrown = true;
        try
        {
            connectAndAuthenticate();
            doProtocolInitialisation();
            // FIXME: Use auto-scaling not only in a separate frame.
            if ( options.autoScale && inSeparateFrame )
            {
                Dimension screenSize;
                try
                {
                    screenSize = vncContainer.getToolkit().getScreenSize();
                }
                catch ( Exception e )
                {
                    screenSize = new Dimension(0, 0);
                }
                createCanvas(screenSize.width - 32, screenSize.height - 32);
            }
            else
            {
                createCanvas((int)width, (int)height);
            }
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            if ( inSeparateFrame )
            {
                // Create a panel which itself is resizeable and can hold
                // non-resizeable VncCanvas component at the top left corner.
                Panel canvasPanel = new Panel();
                canvasPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                canvasPanel.add(vc);
                canvasPanel.setVisible(false);

                // Create a ScrollPane which will hold a panel with VncCanvas
                // inside.
                desktopScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
                gbc.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(desktopScrollPane, gbc);
                desktopScrollPane.add(canvasPanel);
                desktopScrollPane.setVisible(false);

                // Finally, add our ScrollPane to the Frame window.
                vncFrame.setVisible(false);
                vncFrame.add(desktopScrollPane);
                vncFrame.setTitle(rfb.desktopName);
                vncFrame.pack();
                vc.setVisible(false);
                vc.resizeDesktopFrame();
                vc.setVisible(false);
                vncFrame.setVisible(false);
            }
            else
            {

                // Just add the VncCanvas component to the Applet.
                gridbag.setConstraints(vc, gbc);
                add(vc);
                validate();

            }

            try
            {
	            if ( showControls )
	                buttonPanel.enableButtons();

	            moveFocusToDesktop();
	            processNormalProtocol();
            }
            catch(java.net.SocketException ignored)
            {
            	if ( ignored.getMessage().toLowerCase().indexOf("socket closed") == -1 )
            	{ //only ignore the exception if it's the one we expect
            	  //this one was apparently being allowed through the catch clauses below, and ignored
            	  //elsewhere, but was killing my "exceptionsWereThrown" check.
            		throw ignored;
            	}
            }
            exceptionsWereThrown = false;
        }
        catch ( NoRouteToHostException e )
        {
            fatalError("Network error: no route to server: " + host, e);
        }
        catch ( UnknownHostException e )
        {
            fatalError("Network error: server name unknown: " + host, e);
        }
        catch ( ConnectException e )
        {
            fatalError("Network error: could not connect to server: " +
                       host + ":" + port, e);
        }
        catch ( EOFException e )
        {
            if ( showOfflineDesktop )
            {
                e.printStackTrace();
                //System.out.println("Network error: remote side closed connection");
                if ( vc != null )
                {
                    vc.enableInput(false);
                }
                if ( inSeparateFrame )
                {
                    vncFrame.setTitle(rfb.desktopName + " [disconnected]");
                }
                if ( rfb != null && !rfb.closed() )
                    rfb.close();
                if ( showControls && buttonPanel != null )
                {
                    buttonPanel.disableButtonsOnDisconnect();
                    if ( inSeparateFrame )
                    {
                        vncFrame.pack();
                    }
                    else
                    {
                        validate();
                    }
                }
            }
            else
            {
                fatalError("Network error: remote side closed connection", e);
            }
        }
        catch ( IOException e )
        {
            String str = e.getMessage();
            if ( str != null && str.length() != 0 )
            {
                fatalError("Network Error: " + str, e);
            }
            else
            {
                fatalError(e.toString(), e);
            }
        }
        catch ( Exception e )
        {
            String str = e.getMessage();
            if ( str != null && str.length() != 0 )
            {
                fatalError("Error: " + str, e);
            }
            else
            {
                fatalError(e.toString(), e);
            }
        }
        catch(Throwable t) 
        {
        	Main.debugPrint(t.toString());
        }
        finally
        {
        	Main.setLastErrorStatusForObservedHost(this.host, this.port, !exceptionsWereThrown);
        }
    }

    //
    // Create a VncCanvas instance.
    //

    void createCanvas(int maxWidth, int maxHeight) throws IOException {
        // Determine if Java 2D API is available and use a special
        // version of VncCanvas if it is present.
        vc = null;
        try
        {
            // This throws ClassNotFoundException if there is no Java 2D API.
            Class cl = Class.forName("java.awt.Graphics2D");
            // If we could load Graphics2D class, then we can use VncCanvas2D.
            cl = Class.forName("VncCanvas2");
            Class[] argClasses = { this.getClass(), Integer.TYPE, Integer.TYPE};
            java.lang.reflect.Constructor cstr = cl.getConstructor(argClasses);
            Object[] argObjects =
            { this, new Integer(maxWidth), new Integer(maxHeight)};
            vc = (VncCanvas)cstr.newInstance(argObjects);
        }
        catch ( Exception e )
        {
            //System.out.println("Warning: Java 2D API is not available");
        }

        // If we failed to create VncCanvas2D, use old VncCanvas.
        if ( vc == null )
            vc = new VncCanvas(this, maxWidth, maxHeight);

    }


    //
    // Process RFB socket messages.
    // If the rfbThread is being stopped, ignore any exceptions,
    // otherwise rethrow the exception so it can be handled.
    //

    void processNormalProtocol() throws Exception {
        try
        {
            vc.processNormalProtocol();
        }
        catch ( Exception e )
        {
            if ( rfbThread == null )
            {
                //System.out.println("Ignoring RFB socket exceptions" +
                //                   " because applet is stopping");
            }
            else
            {
                throw e;
            }
        }
    }


    //
    // Connect to the RFB server and authenticate the user.
    //

    void connectAndAuthenticate() throws Exception
    {
        showConnectionStatus("Initializing...");
        if ( inSeparateFrame )
        {
            vncFrame.pack();
            //vncFrame.show();
        }
        else
        {
            validate();
        }

        Main.debugPrint("Connecting to " + host + ", port " + port + "...");
        showConnectionStatus("Connecting to " + host + ", port " + port + "...");

        rfb = new RfbProto(host, port, this);
        showConnectionStatus("Connected to server");

        rfb.readVersionMsg();
        showConnectionStatus("RFB server supports protocol version " +
                             rfb.serverMajor + "." + rfb.serverMinor);

        rfb.writeVersionMsg();
        showConnectionStatus("Using RFB protocol version " +
                             rfb.clientMajor + "." + rfb.clientMinor);

        int secType = rfb.negotiateSecurity();
        int authType;
        if ( secType == RfbProto.SecTypeTight )
        {
            showConnectionStatus("Enabling TightVNC protocol extensions");
            rfb.initCapabilities();
            rfb.setupTunneling();
            authType = rfb.negotiateAuthenticationTight();
        }
        else
        {
            authType = secType;
        }

        switch ( authType )
        {
        case RfbProto.AuthNone:
            showConnectionStatus("No authentication needed");
            rfb.authenticateNone();
            break;
        case RfbProto.AuthVNC:
            showConnectionStatus("Performing standard VNC authentication");
            if ( passwordParam != null )
            {
                rfb.authenticateVNC(passwordParam);
            }
            else
            {
                String pw = askPassword();
                rfb.authenticateVNC(pw);
            }
            break;
        default:
            throw new Exception("Unknown authentication scheme " + authType);
        }
    }


    //
    // Show a message describing the connection status.
    // To hide the connection status label, use (msg == null).
    //

    void showConnectionStatus(String msg)
    {
        this.setVisible(false);
        vncContainer.setVisible(false);
        if ( msg == null )
        {
            if ( vncContainer.isAncestorOf(connStatusLabel) )
            {
                vncContainer.remove(connStatusLabel);
            }
            return;
        }

        //System.out.println(msg);

        if ( connStatusLabel == null )
        {
            connStatusLabel = new Label("Status: " + msg);
            connStatusLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
        }
        else
        {
            connStatusLabel.setText("Status: " + msg);
        }
        connStatusLabel.setVisible(false);

        if ( !vncContainer.isAncestorOf(connStatusLabel) )
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(20, 30, 20, 30);
            gridbag.setConstraints(connStatusLabel, gbc);
            vncContainer.add(connStatusLabel);
            vncContainer.setVisible(false);
        }

        if ( inSeparateFrame )
        {
            vncFrame.pack();
        }
        else
        {
            validate();
        }
    }


    //
    // Show an authentication panel.
    //

    String askPassword() throws Exception
    {
        showConnectionStatus(null);

        AuthPanel authPanel = new AuthPanel(this);
        authPanel.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 100;
        gbc.ipady = 50;
        gridbag.setConstraints(authPanel, gbc);
        vncContainer.add(authPanel);
        vncContainer.setVisible(false);

        if ( inSeparateFrame )
        {
            vncFrame.pack();
        }
        else
        {
            validate();
        }

        authPanel.moveFocusToDefaultField();
        String pw = authPanel.getPassword();
        vncContainer.remove(authPanel);

        return pw;
    }


    //
    // Do the rest of the protocol initialisation.
    //

    void doProtocolInitialisation() throws IOException
    {
        rfb.writeClientInit();
        rfb.readServerInit();

        //System.out.println("Desktop name is " + rfb.desktopName);
        //System.out.println("Desktop size is " + rfb.framebufferWidth + " x " +
        //                   rfb.framebufferHeight);

        setEncodings();

        showConnectionStatus(null);
    }


    //
    // Send current encoding list to the RFB server.
    //

    int[] encodingsSaved;
    int nEncodingsSaved;

    void setEncodings()
    {
        setEncodings(false);
    }
    void autoSelectEncodings()
    {
        setEncodings(true);
    }

    void setEncodings(boolean autoSelectOnly)
    {
        if ( options == null || rfb == null || !rfb.inNormalProtocol )
            return;

        int preferredEncoding = options.preferredEncoding;
        if ( preferredEncoding == -1 )
        {
            long kbitsPerSecond = rfb.kbitsPerSecond();
            if ( nEncodingsSaved < 1 )
            {
                // Choose Tight or ZRLE encoding for the very first update.
                //System.out.println("Using Tight/ZRLE encodings");
                preferredEncoding = RfbProto.EncodingTight;
            }
            else if ( kbitsPerSecond > 2000 &&
                      encodingsSaved[0] != RfbProto.EncodingHextile )
            {
                // Switch to Hextile if the connection speed is above 2Mbps.
                //System.out.println("Throughput " + kbitsPerSecond +
                //                   " kbit/s - changing to Hextile encoding");
                preferredEncoding = RfbProto.EncodingHextile;
            }
            else if ( kbitsPerSecond < 1000 &&
                      encodingsSaved[0] != RfbProto.EncodingTight )
            {
                // Switch to Tight/ZRLE if the connection speed is below 1Mbps.
                //System.out.println("Throughput " + kbitsPerSecond +
                //                   " kbit/s - changing to Tight/ZRLE encodings");
                preferredEncoding = RfbProto.EncodingTight;
            }
            else
            {
                // Don't change the encoder.
                if ( autoSelectOnly )
                    return;
                preferredEncoding = encodingsSaved[0];
            }
        }
        else
        {
            // Auto encoder selection is not enabled.
            if ( autoSelectOnly )
                return;
        }

        int[] encodings = new int[20];
        int nEncodings = 0;

        encodings[nEncodings++] = preferredEncoding;
        if ( options.useCopyRect )
        {
            encodings[nEncodings++] = RfbProto.EncodingCopyRect;
        }

        if ( preferredEncoding != RfbProto.EncodingTight )
        {
            encodings[nEncodings++] = RfbProto.EncodingTight;
        }
        if ( preferredEncoding != RfbProto.EncodingZRLE )
        {
            encodings[nEncodings++] = RfbProto.EncodingZRLE;
        }
        if ( preferredEncoding != RfbProto.EncodingHextile )
        {
            encodings[nEncodings++] = RfbProto.EncodingHextile;
        }
        if ( preferredEncoding != RfbProto.EncodingZlib )
        {
            encodings[nEncodings++] = RfbProto.EncodingZlib;
        }
        if ( preferredEncoding != RfbProto.EncodingCoRRE )
        {
            encodings[nEncodings++] = RfbProto.EncodingCoRRE;
        }
        if ( preferredEncoding != RfbProto.EncodingRRE )
        {
            encodings[nEncodings++] = RfbProto.EncodingRRE;
        }

        if ( options.compressLevel >= 0 && options.compressLevel <= 9 )
        {
            encodings[nEncodings++] =
            RfbProto.EncodingCompressLevel0 + options.compressLevel;
        }
        if ( options.jpegQuality >= 0 && options.jpegQuality <= 9 )
        {
            encodings[nEncodings++] =
            RfbProto.EncodingQualityLevel0 + options.jpegQuality;
        }

        if ( options.requestCursorUpdates )
        {
            encodings[nEncodings++] = RfbProto.EncodingXCursor;
            encodings[nEncodings++] = RfbProto.EncodingRichCursor;
            if ( !options.ignoreCursorUpdates )
                encodings[nEncodings++] = RfbProto.EncodingPointerPos;
        }

        encodings[nEncodings++] = RfbProto.EncodingLastRect;
        encodings[nEncodings++] = RfbProto.EncodingNewFBSize;

        boolean encodingsWereChanged = false;
        if ( nEncodings != nEncodingsSaved )
        {
            encodingsWereChanged = true;
        }
        else
        {
            for ( int i = 0; i < nEncodings; i++ )
            {
                if ( encodings[i] != encodingsSaved[i] )
                {
                    encodingsWereChanged = true;
                    break;
                }
            }
        }

        if ( encodingsWereChanged )
        {
            try
            {
                rfb.writeSetEncodings(encodings, nEncodings);
                if ( vc != null )
                {
                    vc.softCursorFree();
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            encodingsSaved = encodings;
            nEncodingsSaved = nEncodings;
        }
    }


    //
    // setCutText() - send the given cut text to the RFB server.
    //

    void setCutText(String text)
    {
        try
        {
            if ( rfb != null && rfb.inNormalProtocol )
            {
                rfb.writeClientCutText(text);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    //
    // Order change in session recording status. To stop recording, pass
    // null in place of the fname argument.
    //

    void setRecordingStatus(String fname)
    {
        synchronized(recordingSync)
        {
            sessionFileName = fname;
            recordingStatusChanged = true;
        }
    }

    //
    // Start or stop session recording. Returns true if this method call
    // causes recording of a new session.
    //

    boolean checkRecordingStatus() throws IOException {
        synchronized(recordingSync)
        {
            if ( recordingStatusChanged )
            {
                recordingStatusChanged = false;
                if ( sessionFileName != null )
                {
                    startRecording();
                    return true;
                }
                else
                {
                    stopRecording();
                }
            }
        }
        return false;
    }

    //
    // Start session recording.
    //

    protected void startRecording() throws IOException {
        synchronized(recordingSync)
        {
            if ( !recordingActive )
            {
                // Save settings to restore them after recording the session.
                cursorUpdatesDef =
                options.choices[options.cursorUpdatesIndex].getSelectedItem();
                eightBitColorsDef =
                options.choices[options.eightBitColorsIndex].getSelectedItem();
                // Set options to values suitable for recording.
                options.choices[options.cursorUpdatesIndex].select("Disable");
                options.choices[options.cursorUpdatesIndex].setEnabled(false);
                options.setEncodings();
                options.choices[options.eightBitColorsIndex].select("No");
                options.choices[options.eightBitColorsIndex].setEnabled(false);
                options.setColorFormat();
            }
            else
            {
                rfb.closeSession();
            }

            //System.out.println("Recording the session in " + sessionFileName);
            rfb.startSession(sessionFileName);
            recordingActive = true;
        }
    }

    //
    // Stop session recording.
    //

    protected void stopRecording() throws IOException {
        synchronized(recordingSync)
        {
            if ( recordingActive )
            {
                // Restore options.
                options.choices[options.cursorUpdatesIndex].select(cursorUpdatesDef);
                options.choices[options.cursorUpdatesIndex].setEnabled(true);
                options.setEncodings();
                options.choices[options.eightBitColorsIndex].select(eightBitColorsDef);
                options.choices[options.eightBitColorsIndex].setEnabled(true);
                options.setColorFormat();

                rfb.closeSession();
                //System.out.println("Session recording stopped.");
            }
            sessionFileName = null;
            recordingActive = false;
        }
    }


    //
    // readParameters() - read parameters from the html source or from the
    // command line.  On the command line, the arguments are just a sequence of
    // param_name/param_value pairs where the names and values correspond to
    // those expected in the html applet tag source.
    //

    void readParameters()
    {
        host = readParameter("HOST", !inAnApplet);
        if ( host == null )
        {
            host = getCodeBase().getHost();
            if ( host.equals("") )
            {
                fatalError("HOST parameter not specified");
            }
        }

        String str = readParameter("PORT", true);
        port = Integer.parseInt(str);

        // Read "ENCPASSWORD" or "PASSWORD" parameter if specified.
        readPasswordParameters();

        if ( inAnApplet )
        {
            str = readParameter("Open New Window", false);
            if ( str != null && str.equalsIgnoreCase("Yes") )
                inSeparateFrame = true;
        }

        // "Show Controls" set to "No" disables button panel.
        showControls = true;
        str = readParameter("Show Controls", false);
        if ( str != null && str.equalsIgnoreCase("No") )
            showControls = false;

        // "Offer Relogin" set to "No" disables "Login again" and "Close
        // window" buttons under error messages in applet mode.
        offerRelogin = true;
        str = readParameter("Offer Relogin", false);
        if ( str != null && str.equalsIgnoreCase("No") )
            offerRelogin = false;

        // Do we continue showing desktop on remote disconnect?
        showOfflineDesktop = false;
        str = readParameter("Show Offline Desktop", false);
        if ( str != null && str.equalsIgnoreCase("Yes") )
            showOfflineDesktop = true;

        // Fine tuning options.
        deferScreenUpdates = readIntParameter("Defer screen updates", 20);
        deferCursorUpdates = readIntParameter("Defer cursor updates", 10);
        deferUpdateRequests = readIntParameter("Defer update requests", 50);

        // SocketFactory.
        socketFactory = readParameter("SocketFactory", false);
    }

    //
    // Read password parameters. If an "ENCPASSWORD" parameter is set,
    // then decrypt the password into the passwordParam string. Otherwise,
    // try to read the "PASSWORD" parameter directly to passwordParam.
    //

    private void readPasswordParameters()
    {
        String encPasswordParam = readParameter("ENCPASSWORD", false);
        if ( encPasswordParam == null )
        {
            passwordParam = readParameter("PASSWORD", false);
        }
        else
        {
            // ENCPASSWORD is hexascii-encoded. Decode.
            byte[] pw = {0, 0, 0, 0, 0, 0, 0, 0};
            int len = encPasswordParam.length() / 2;
            if ( len > 8 )
                len = 8;
            for ( int i = 0; i < len; i++ )
            {
                String hex = encPasswordParam.substring(i*2, i*2+2);
                Integer x = new Integer(Integer.parseInt(hex, 16));
                pw[i] = x.byteValue();
            }
            // Decrypt the password.
            byte[] key = {23, 82, 107, 6, 35, 78, 88, 7};
            DesCipher des = new DesCipher(key);
            des.decrypt(pw, 0, pw, 0);
            passwordParam = new String(pw);
        }
    }

    public String readParameter(String name, boolean required)
    {
        if ( inAnApplet )
        {
            String s = getParameter(name);
            if ( (s == null) && required )
            {
                fatalError(name + " parameter not specified");
            }
            return s;
        }

        for ( int i = 0; i < mainArgs.length; i += 2 )
        {
            if ( mainArgs[i].equalsIgnoreCase(name) )
            {
                try
                {
                    return mainArgs[i+1];
                }
                catch ( Exception e )
                {
                    if ( required )
                    {
                        fatalError(name + " parameter not specified");
                    }
                    return null;
                }
            }
        }
        if ( required )
        {
            fatalError(name + " parameter not specified");
        }
        return null;
    }

    int readIntParameter(String name, int defaultValue)
    {
        String str = readParameter(name, false);
        int result = defaultValue;
        if ( str != null )
        {
            try
            {
                result = Integer.parseInt(str);
            }
            catch ( NumberFormatException e )
            {
            }
        }
        return result;
    }

    //
    // moveFocusToDesktop() - move keyboard focus either to VncCanvas.
    //

    void moveFocusToDesktop()
    {
        if ( vncContainer != null )
        {
            if ( vc != null && vncContainer.isAncestorOf(vc) )
            {
                vc.requestFocus();
                vc.setVisible(false);
            }
        }
    }

    //
    // disconnect() - close connection to server.
    //

    synchronized public void disconnect()
    {
        //System.out.println("Disconnect");

        if ( rfb != null && !rfb.closed() )
            rfb.close();
        options.dispose();
        clipboard.dispose();
        if ( rec != null )
            rec.dispose();

        if ( inAnApplet )
        {
            showMessage("Disconnected");
        }
        else
        {
            //System.exit(0);
        }
    }

    //
    // fatalError() - print out a fatal error message.
    // FIXME: Do we really need two versions of the fatalError() method?
    //

    synchronized public void fatalError(String str)
    {
        //System.out.println(str);

        if ( inAnApplet )
        {
            // vncContainer null, applet not inited,
            // can not present the error to the user.
            //Thread.currentThread().stop();
            Main.debugPrint("VncViewer.fatalError: \"" + str + "\"");
        }
        else
        {
            //System.exit(1);
        }
    }

    synchronized public void fatalError(String str, Exception e)
    {

        if ( rfb != null && rfb.closed() )
        {
            // Not necessary to show error message if the error was caused
            // by I/O problems after the rfb.close() method call.
            //System.out.println("RFB thread finished");
            return;
        }

        //System.out.println(str);
        e.printStackTrace();

        if ( rfb != null )
            rfb.close();

        if ( inAnApplet )
        {
            showMessage(str);
        }
        else
        {
            //System.exit(1);
        }
    }

    //
    // Show message text and optionally "Relogin" and "Close" buttons.
    //

    void showMessage(String msg)
    {
        vncContainer.removeAll();

        Label errLabel = new Label(msg, Label.CENTER);
        errLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));

        if ( offerRelogin )
        {

            Panel gridPanel = new Panel(new GridLayout(0, 1));
            Panel outerPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
            outerPanel.add(gridPanel);
            vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 16));
            vncContainer.add(outerPanel);
            Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(errLabel);
            gridPanel.add(textPanel);
            gridPanel.add(new ReloginPanel(this));

        }
        else
        {

            vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 30));
            vncContainer.add(errLabel);

        }

        if ( inSeparateFrame )
        {
            vncFrame.pack();
        }
        else
        {
            validate();
        }
    }

    //
    // Stop the applet.
    // Main applet thread will terminate on first exception
    // after seeing that rfbThread has been set to null.
    //

    public void stop()
    {
        //System.out.println("Stopping applet");
        rfbThread = null;
    }

    //
    // This method is called before the applet is destroyed.
    //

    public void destroy()
    {
        //System.out.println("Destroying applet");

        vncContainer.removeAll();
        options.dispose();
        clipboard.dispose();
        if ( rec != null )
            rec.dispose();
        if ( rfb != null && !rfb.closed() )
            rfb.close();
        if ( inSeparateFrame )
            vncFrame.dispose();
    }

    //
    // Start/stop receiving mouse events.
    //

    public void enableInput(boolean enable)
    {
        vc.enableInput(enable);
    }

    //
    // Close application properly on window close event.
    //

    public void windowClosing(WindowEvent evt)
    {
        //System.out.println("Closing window");
        if ( rfb != null )
            disconnect();

        //vncContainer.hide();

        if ( !inAnApplet )
        {
            //System.exit(0);
        }
    }

    //
    // Ignore window events we're not interested in.
    //

    public void windowActivated(WindowEvent evt)
    {
    }
    public void windowDeactivated (WindowEvent evt)
    {
    }
    public void windowOpened(WindowEvent evt)
    {
    }
    public void windowClosed(WindowEvent evt)
    {
    }
    public void windowIconified(WindowEvent evt)
    {
    }
    public void windowDeiconified(WindowEvent evt)
    {
    }
}
