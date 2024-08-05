package com.rapidesuite.build.core.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStreamReader;

import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.autoinjectors.navigation.fld.templates.RobotPasteTemplate;
import com.rapidesuite.configurator.robotPaste0001.RobotPasteDocument;
import com.rapidesuite.core.utility.CoreUtil;


public class RobotPasteAction implements ClipboardOwner
{
    public RobotPasteAction(int iteration, File pathToRobotPasteDocument)
    {
        this.iteration = iteration;
        this.pathToRobotPasteDocument = pathToRobotPasteDocument;
    }
    private Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    private ActionManager actionManager = null;

    private int iteration;

    public int getIteration()
    {
        return iteration;
    }
    public void setIteration(int iteration)
    {
        this.iteration = iteration;
    }

    File pathToRobotPasteDocument;
    RobotPasteDocument robotPasteDocument;

    private volatile boolean hasExecuted = false;
    public boolean hasExecuted()
    {
        return this.hasExecuted;
    }

    private boolean isLong()
    {
        return this.robotPasteDocument.getRobotPaste().getData().length() >= Config.getBuildRobotPasteMaxDataSizeChars();
    }

    private Long timeWhenLogContentWasFirstNotNull = null;
    public boolean shouldExecute(String logContent) throws Exception
    {
        if ( this.hasExecuted )
        {
            return false;
        }
        if ( null == this.robotPasteDocument )
        {
            this.robotPasteDocument = RobotPasteDocument.Factory.parse(new InputStreamReader(CoreUtil.getInputStream(true, this.pathToRobotPasteDocument), com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING));
        }

        if ( !isLong() )
        {
            return logContent != null && logContent.indexOf(RobotPasteTemplate.ROBOT_PASTE_IMMINENT) != -1;
        }
        else if ( timeWhenLogContentWasFirstNotNull == null )
        {
            timeWhenLogContentWasFirstNotNull = System.currentTimeMillis();
        }
        return (System.currentTimeMillis() - timeWhenLogContentWasFirstNotNull) > Config.getBuildRobotPasteDelayAfterInjectorStartSeconds() * 1000;
    }

    public void execute(ActionManager actionManager) throws Exception
    {
        this.actionManager = actionManager;
        dieIfStopped();
        com.rapidesuite.client.common.util.Utils.sleep(Config.getBuildRobotPasteDelayAfterLogIntSeconds() * 1000);

        java.awt.Robot r = new java.awt.Robot();
        String data = robotPasteDocument.getRobotPaste().getData();
        //undo the previous escaping on newlines
        data = data.replaceAll("\\\\n", "\n");
        pasteText(r, data);
		com.rapidesuite.client.common.util.Utils.sleep(Config.getBuildRobotPasteDelayAfterLogIntSeconds() * 1000);
        executePostPasteActions(robotPasteDocument, r);

        this.robotPasteDocument = null;
        this.hasExecuted = true;
    }

    private void dieIfStopped() throws ManualStopException
    {
        if ( actionManager != null && actionManager.isExecutionStopped() )
        {
            throw new ManualStopException();
        }
    }



    private void executePostPasteActions(RobotPasteDocument rpd, java.awt.Robot r) throws Exception
    {
        for ( String keyAction : rpd.getRobotPaste().getKeyActionArray() )
        {
            String[] pair = keyAction.split(" ");
            int keycode;

            try
            {
                keycode = Integer.parseInt(pair[1]);
            }
            catch(NumberFormatException e)
            {
                java.lang.reflect.Field field = KeyEvent.class.getField(pair[1].trim());
                keycode = field.getInt(KeyEvent.class);
            }

            RobotPasteTemplate.PRESS_OR_RELEASE functionName = RobotPasteTemplate.PRESS_OR_RELEASE.valueOf(pair[0]);
            if ( functionName.equals(RobotPasteTemplate.PRESS_OR_RELEASE.keyPress) )
            {
                com.rapidesuite.client.common.util.Utils.sleep(Config.getBuildRobotBeforeKeyPressDelayMS());
                dieIfStopped();
                r.keyPress(keycode);
            }
            else
            {
                r.keyRelease(keycode);
            }
        }
    }


    private void pasteText(java.awt.Robot r, String text) throws Exception
    {
        if (hasExecuted())
        {
            return;
        }
        StringSelection strSel = new StringSelection(text);
        while ( true )
        {
            if ( actionManager != null && actionManager.isExecutionStopped() )
            {
                throw new ManualStopException();
            }
            FileUtils.println("About to paste to clipboard");
            try
            {
                System.err.println("about to set text, length = " + text.length());
                sysClip.setContents(strSel, this);
                break;
            }
            catch(Throwable t)
            {
                int sleepDuration = 1000;
                FileUtils.println("Failed to paste to clipboard, sleeping " + sleepDuration + "...");
                FileUtils.printStackTrace(t);
                com.rapidesuite.client.common.util.Utils.sleep(sleepDuration);
            }
        }

        dieIfStopped();
        r.keyPress(KeyEvent.VK_CONTROL);
        com.rapidesuite.client.common.util.Utils.sleep(Config.getBuildRobotBeforeKeyPressDelayMS());
        r.keyPress(86);
        r.keyRelease(86);
        r.keyRelease(KeyEvent.VK_CONTROL);

		//VERY IMPORTANT to sleep after ctrl-V, give it time to paste the correct text.
		//timing is such that not sleeping will cause empty or old text to be pasted,
		//as set below.
		com.rapidesuite.client.common.util.Utils.sleep(2000L);
        sysClip.setContents(new StringSelection("empty clipboard"), null);
    }

    public void lostOwnership(Clipboard c, Transferable t)
    {
        if (hasExecuted())
        {
            return;
        }
        FileUtils.println("RobotPasteAction lostOwnership of clipboard");
        try
        {
            com.rapidesuite.client.common.util.Utils.sleep(1000);
        }
        catch(Throwable te)
        {
            FileUtils.printStackTrace(te);
        }
        Transferable contents = sysClip.getContents(this);
        regainOwnership(contents);
    }
    void regainOwnership(Transferable t)
    {
        sysClip.setContents(t, this);
    }
}
