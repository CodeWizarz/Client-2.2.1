package com.rapidesuite.snapshot.model;

import java.io.File;

public class FoldersFlattener {

	public static void main(String[] args) throws Exception
	{
		String sourceFolderPath=args[0];
		String targetFolderPath=args[1];
		
		com.rapidesuite.snapshot.model.ModelUtils.flattenFolders(new File(sourceFolderPath),new File(targetFolderPath));	
	}
}
