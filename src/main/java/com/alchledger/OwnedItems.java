package com.alchledger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import javax.inject.Singleton;
import net.runelite.api.Item;

/**
 * Quantities of items in the inventory and bank, keyed by canonical item id.
 */
@Singleton
public class OwnedItems
{
	private Map<Integer, Long> inventory = new HashMap<>();
	private Map<Integer, Long> bank = new HashMap<>();
	private boolean bankSeen;

	public synchronized void setInventory(Map<Integer, Long> items)
	{
		inventory = items;
	}

	public synchronized void setBank(Map<Integer, Long> items)
	{
		bank = items;
		bankSeen = true;
	}

	public synchronized boolean isBankSeen()
	{
		return bankSeen;
	}

	public synchronized Map<Integer, Long> getInventory()
	{
		return new HashMap<>(inventory);
	}

	public synchronized Map<Integer, Long> getBank()
	{
		return new HashMap<>(bank);
	}

	public synchronized void clear()
	{
		inventory = new HashMap<>();
		bank = new HashMap<>();
		bankSeen = false;
	}

	public static Map<Integer, Long> count(Item[] items, IntUnaryOperator canonicalize)
	{
		Map<Integer, Long> counts = new HashMap<>();
		for (Item item : items)
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			counts.merge(canonicalize.applyAsInt(item.getId()), (long) item.getQuantity(), Long::sum);
		}
		return counts;
	}
}
