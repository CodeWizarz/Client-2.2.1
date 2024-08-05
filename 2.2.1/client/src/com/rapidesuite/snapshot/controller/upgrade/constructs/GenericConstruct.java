package com.rapidesuite.snapshot.controller.upgrade.constructs;

import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class GenericConstruct {

	protected UpgradeEngine upgradeEngine;
	
	public GenericConstruct(UpgradeEngine upgradeEngine) {
		this.upgradeEngine=upgradeEngine;
	}
}