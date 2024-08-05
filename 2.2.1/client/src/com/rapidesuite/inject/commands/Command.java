package com.rapidesuite.inject.commands;

import com.rapidesuite.inject.Worker;

public class Command {

	protected Worker worker;
	
	public Command(Worker worker){
		this.worker=worker;
	}
	
}
