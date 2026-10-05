package com.alchledger;

import java.awt.Color;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

final class Format
{
	private Format()
	{
	}

	/**
	 * @return e.g. "+1,234 gp" or "-56 gp"
	 */
	static String gp(long value)
	{
		return (value > 0 ? "+" : "") + QuantityFormatter.formatNumber(value) + " gp";
	}

	/**
	 * @return a shortened amount for tight spaces, e.g. "+123K gp" or "-1.23M gp"
	 */
	static String gpShort(long value)
	{
		return (value > 0 ? "+" : "") + QuantityFormatter.quantityToStackSize(value) + " gp";
	}

	/**
	 * @return profit per point of Magic xp for a High Alchemy cast, e.g. "+2.5 gp/xp"
	 */
	static String gpPerXp(long profitPerCast)
	{
		return String.format("%+.1f gp/xp", (double) profitPerCast / ProfitCalculator.HIGH_ALCH_XP);
	}

	static Color profitColor(long value)
	{
		if (value > 0)
		{
			return ColorScheme.PROGRESS_COMPLETE_COLOR;
		}
		if (value < 0)
		{
			return ColorScheme.PROGRESS_ERROR_COLOR;
		}
		return Color.WHITE;
	}
}
