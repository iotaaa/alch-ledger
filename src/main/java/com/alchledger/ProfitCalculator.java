package com.alchledger;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;

/**
 * Works out the profit of high alching an item. Must be used on the client thread.
 */
@Singleton
public class ProfitCalculator
{
	public static final int HIGH_ALCH_XP = 65;

	private static final int UNKNOWN = Integer.MIN_VALUE;

	private final ItemManager itemManager;
	private final AlchLedgerConfig config;

	// Keyed by the raw item id, so overlays can look up without canonicalising every frame
	private final Map<Integer, Integer> cache = new ConcurrentHashMap<>();

	private volatile boolean ironmanDetected;

	@Inject
	ProfitCalculator(ItemManager itemManager, AlchLedgerConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
	}

	public void clearCache()
	{
		cache.clear();
	}

	public void setIronmanDetected(boolean ironmanDetected)
	{
		this.ironmanDetected = ironmanDetected;
	}

	public boolean isIronman()
	{
		switch (config.accountMode())
		{
			case STANDARD:
				return false;
			case IRONMAN:
				return true;
			default:
				return ironmanDetected;
		}
	}

	public int getNatureRunePrice()
	{
		if (config.useLiveNatureRunePrice())
		{
			int live = (int) itemManager.getItemPrice(ItemID.NATURERUNE);
			if (live > 0)
			{
				return live;
			}
		}
		return config.natureRunePrice();
	}

	/**
	 * @return the profit per cast in the current account mode, or null if it is unknown
	 */
	public Integer getProfit(int itemId)
	{
		int profit = cache.computeIfAbsent(itemId, this::computeProfit);
		return profit == UNKNOWN ? null : profit;
	}

	private int computeProfit(int itemId)
	{
		int id = itemManager.canonicalize(itemId);
		if (isCurrency(id))
		{
			return UNKNOWN;
		}

		int alchValue = itemManager.getItemComposition(id).getHaPrice();
		if (alchValue <= 0)
		{
			return UNKNOWN;
		}

		if (isIronman())
		{
			return alchValue - getNatureRunePrice();
		}

		int gePrice = (int) itemManager.getItemPrice(id);
		if (gePrice <= 0)
		{
			return UNKNOWN;
		}
		return alchValue - gePrice - getNatureRunePrice();
	}

	public static boolean isCurrency(int canonicalId)
	{
		return canonicalId == ItemID.COINS || canonicalId == ItemID.PLATINUM;
	}

	public ProfitTier getTier(int profit)
	{
		if (profit >= config.highThreshold())
		{
			return ProfitTier.HIGH;
		}
		if (profit >= config.mediumThreshold())
		{
			return ProfitTier.MEDIUM;
		}
		if (profit >= config.lowThreshold())
		{
			return ProfitTier.LOW;
		}
		return ProfitTier.BELOW_LOW;
	}

	public Color getColor(ProfitTier tier)
	{
		if (tier == null)
		{
			return null;
		}
		switch (tier)
		{
			case BELOW_LOW:
				return config.lossColor();
			case LOW:
				return config.lowColor();
			case MEDIUM:
				return config.mediumColor();
			case HIGH:
				return config.highColor();
			default:
				return null;
		}
	}
}
