package com.rapidesuite.build.core.htmlplayback.utils;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.rapidesuite.client.common.util.FileUtils;

public class HTMLILCreator
{

	public static void main(String[] args)
	{
		try
		{
			PrintWriter printWriter = null;
			try
			{
				String fileName = "D:/PERFORCE02/development/SwiftSuite/SwiftBuild/" + "current/deploy/htmlplayback/il-AP/supplier-create.il";
				printWriter = new PrintWriter(new FileWriter(fileName));

				StringBuffer s = new StringBuffer("");
				s.append("#START_HEADER#\n");
				s.append("#       login to Oracle Apps\n");
				s.append("#       create the supplier\n");
				s.append("TEMPLATE_LOAD_URL LOAD_URL_TYPE=URL##%!%##PARAM1=http://orrsctst09.erapidsuite.com:8000/OA_HTML/RF.jsp?function_id=1348\n");
				s.append("TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=usernameField##%!%##PARAM2=CONSULT\n");
				s.append("TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=passwordField##%!%##PARAM2=oracle\n");
				s.append("TEMPLATE_BUTTON CLICK_BY=TITLE##%!%##PARAM1=Login\n");
				s.append("#       Select the responsibility\n");
				s.append("TEMPLATE_ANCHOR CLICK_BY=TEXTNODE##%!%##PARAM1=RSC Payables Manager\n");
				s.append("#END_HEADER#\n");
				printWriter.println(s);
				int counter = 10;
				String key = "THERTEST";
				for ( int i = 0; i < counter; i++ )
				{
					String temp = key + (i + 1);
					s = new StringBuffer("");
					s.append("# SUPPLIER " + temp + "\n");
					s.append("TEMPLATE_BUTTON CLICK_BY=TEXTNODE##%!%##PARAM1=Create Supplier \n");
					s.append("TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=organization_name##%!%##PARAM2=" + temp + "\n");
					s.append("TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=taxCountry##%!%##PARAM2=Argentina\n");
					s.append("TEMPLATE_BUTTON CLICK_BY=TITLE##%!%##PARAM1=Apply\n");
					s.append("TEMPLATE_ANCHOR CLICK_BY=ID##%!%##PARAM1=POS_HT_SP_B_SUPP\n");
					s.append("#BREAK#\n");
					printWriter.println(s);
				}
			}
			finally
			{
				if ( printWriter != null )
					printWriter.close();
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

}