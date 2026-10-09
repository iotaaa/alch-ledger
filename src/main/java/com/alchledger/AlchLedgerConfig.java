package com.alchledger;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(AlchLedgerConfig.GROUP)
public interface AlchLedgerConfig extends Config
{
	String GROUP = "alchledger";

	@ConfigSection(
		name = "Prices",
		description = "Account mode and nature rune price",
		position = 0
	)
	String pricesSection = "prices";

	@ConfigSection(
		name = "Outline tiers",
		description = "Item outlines by profit per cast",
		position = 1
	)
	String outlineSection = "outline";

	@ConfigSection(
		name = "Side panel",
		description = "Side panel options",
		position = 2
	)
	String panelSection = "panel";

	@ConfigSection(
		name = "Profit log",
		description = "Tracking profit from casts",
		position = 3
	)
	String logSection = "log";

	// Prices

	@ConfigItem(
		keyName = "accountMode",
		name = "Account mode",
		description = "Standard pays the GE price for each item. Ironman already owns the item, so only the nature rune counts.",
		position = 0,
		section = pricesSection
	)
	default AccountMode accountMode()
	{
		return AccountMode.AUTO;
	}

	@ConfigItem(
		keyName = "natureRunePrice",
		name = "Nature rune price",
		description = "Price of one nature rune, in gp",
		position = 1,
		section = pricesSection
	)
	default int natureRunePrice()
	{
		return 180;
	}

	@ConfigItem(
		keyName = "useLiveNatureRunePrice",
		name = "Use live nature rune price",
		description = "Use the current GE price of a nature rune instead of the price above",
		position = 2,
		section = pricesSection
	)
	default boolean useLiveNatureRunePrice()
	{
		return false;
	}

	// Outline tiers

	@ConfigItem(
		keyName = "outlineInventory",
		name = "Outline in inventory",
		description = "Outline items in your inventory",
		position = 0,
		section = outlineSection
	)
	default boolean outlineInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "outlineBank",
		name = "Outline in bank",
		description = "Outline items in your bank",
		position = 1,
		section = outlineSection
	)
	default boolean outlineBank()
	{
		return true;
	}

	@ConfigItem(
		keyName = "outlineLoss",
		name = "Outline below low tier",
		description = "Outline items making less than the low tier, including items that lose money",
		position = 2,
		section = outlineSection
	)
	default boolean outlineLoss()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "lossColor",
		name = "Below low colour",
		description = "Outline colour for items making less than the low tier",
		position = 3,
		section = outlineSection
	)
	default Color lossColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "lowThreshold",
		name = "Low tier from (gp)",
		description = "Minimum profit per cast for the low tier",
		position = 4,
		section = outlineSection
	)
	default int lowThreshold()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
		keyName = "lowColor",
		name = "Low colour",
		description = "Outline colour for the low tier",
		position = 5,
		section = outlineSection
	)
	default Color lowColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "mediumThreshold",
		name = "Medium tier from (gp)",
		description = "Minimum profit per cast for the medium tier",
		position = 6,
		section = outlineSection
	)
	default int mediumThreshold()
	{
		return 100;
	}

	@Alpha
	@ConfigItem(
		keyName = "mediumColor",
		name = "Medium colour",
		description = "Outline colour for the medium tier",
		position = 7,
		section = outlineSection
	)
	default Color mediumColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "highThreshold",
		name = "High tier from (gp)",
		description = "Minimum profit per cast for the high tier",
		position = 8,
		section = outlineSection
	)
	default int highThreshold()
	{
		return 500;
	}

	@Alpha
	@ConfigItem(
		keyName = "highColor",
		name = "High colour",
		description = "Outline colour for the high tier",
		position = 9,
		section = outlineSection
	)
	default Color highColor()
	{
		return new Color(102, 204, 255);
	}

	@ConfigItem(
		keyName = "superThreshold",
		name = "Super tier from (gp)",
		description = "Minimum profit per cast for the super tier",
		position = 10,
		section = outlineSection
	)
	default int superThreshold()
	{
		return 5000;
	}

	@Alpha
	@ConfigItem(
		keyName = "superColor",
		name = "Super colour",
		description = "Outline colour for the super tier",
		position = 11,
		section = outlineSection
	)
	default Color superColor()
	{
		return new Color(200, 80, 255);
	}

	@ConfigItem(
		keyName = "showAlchValueTooltip",
		name = "Show alch value on hover",
		description = "Show an item's alch value and profit per cast when you hover over it in your inventory or bank",
		position = 12,
		section = outlineSection
	)
	default boolean showAlchValueTooltip()
	{
		return false;
	}

	// Side panel

	@ConfigItem(
		keyName = "showSidePanel",
		name = "Show side panel",
		description = "Show the Alch Ledger side panel",
		position = 0,
		section = panelSection
	)
	default boolean showSidePanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "minProfitToList",
		name = "Min profit to list",
		description = "Only list items making at least this much per cast",
		position = 1,
		section = panelSection
	)
	default int minProfitToList()
	{
		return 1;
	}

	@Range(
		min = 10,
		max = 500
	)
	@ConfigItem(
		keyName = "maxItemsListed",
		name = "Max items listed",
		description = "Maximum number of items in the best GE alchs list",
		position = 2,
		section = panelSection
	)
	default int maxItemsListed()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "includeMembers",
		name = "Include members items",
		description = "Include members items in the best GE alchs list",
		position = 3,
		section = panelSection
	)
	default boolean includeMembers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGpPerXp",
		name = "Show gp/xp",
		description = "Show the profit per point of Magic xp for each item",
		position = 4,
		section = panelSection
	)
	default boolean showGpPerXp()
	{
		return false;
	}

	// Profit log

	@ConfigItem(
		keyName = "showProfitOverlay",
		name = "Show profit overlay",
		description = "Show this session's casts and profit in an overlay",
		position = 0,
		section = logSection
	)
	default boolean showProfitOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatMessagePerCast",
		name = "Chat message per cast",
		description = "Post a chat message with the profit of each cast",
		position = 1,
		section = logSection
	)
	default boolean chatMessagePerCast()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showXp",
		name = "Show xp and xp/hr",
		description = "Show the Magic xp from alching this session in the overlay and side panel",
		position = 2,
		section = logSection
	)
	default boolean showXp()
	{
		return false;
	}
}
