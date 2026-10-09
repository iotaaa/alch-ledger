package com.alchledger;

import lombok.Value;

/**
 * All-time casts and profit for one item.
 */
@Value
public class ItemTotal
{
	int itemId;
	long casts;
	long profit;

	ItemTotal add(long profit)
	{
		return new ItemTotal(itemId, casts + 1, this.profit + profit);
	}
}
