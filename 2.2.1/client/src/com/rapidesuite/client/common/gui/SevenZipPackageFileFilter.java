package com.rapidesuite.client.common.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;

public class SevenZipPackageFileFilter extends FileFilter
{

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = CoreUtil.getFileExtension(f);
        if (extension != null && extension.equalsIgnoreCase(UtilsConstants.SEVENZIP_FILE_EXTENSION)
        ) {
             return true;
        }
		return false;
    }

    public String getDescription() {
        return "7z";
    }

}