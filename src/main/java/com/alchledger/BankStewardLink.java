package com.alchledger;

import javax.inject.Singleton;

/**
 * State shared with the Bank Steward plugin, which talks to Alch Ledger through PluginMessage events.
 */
@Singleton
class BankStewardLink
{
	static final String STEWARD_NAMESPACE = "banksteward";
	static final String NAMESPACE = "alchledger";

	/**
	 * True while Bank Steward's organise guide is running. Bank outlines are hidden then, so they can't be
	 * mistaken for the guide's highlights.
	 */
	private volatile boolean guideActive;

	boolean isGuideActive()
	{
		return guideActive;
	}

	void setGuideActive(boolean guideActive)
	{
		this.guideActive = guideActive;
	}
}
