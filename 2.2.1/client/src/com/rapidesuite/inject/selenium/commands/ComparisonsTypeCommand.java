package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.ComparisonType;
import com.erapidsuite.configurator.navigation0005.ComparisonsType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ComparisonsTypeCommand extends Command{

	public ComparisonsTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public boolean process(ComparisonsType comparisonsType) throws Exception {
		ComparisonsType.Separator.Enum separator=comparisonsType.getSeparator();
		ComparisonType[] comparisonTypeArray=comparisonsType.getComparisonArray();
		for (ComparisonType comparisonType:comparisonTypeArray) {
			ComparisonTypeCommand comparisonTypeCommand=new ComparisonTypeCommand(((SeleniumWorker)worker));
			boolean isSuccessTemp=comparisonTypeCommand.process(comparisonType);
			if ( separator==ComparisonsType.Separator.OR) {
				if (isSuccessTemp) {
					return true;
				}
			}
			else {
				if (!isSuccessTemp) {
					return false;
				}
			}
		}
		if ( separator==ComparisonsType.Separator.OR) {
			return false;
		}
		else {
			return true;
		}
	}
	
}
