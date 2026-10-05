package com.alchledger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

class AlchLedgerItemOverlay extends WidgetItemOverlay
{
	private final ItemManager itemManager;
	private final ProfitCalculator calculator;
	private final AlchLedgerConfig config;

	@Inject
	AlchLedgerItemOverlay(ItemManager itemManager, ProfitCalculator calculator, AlchLedgerConfig config)
	{
		this.itemManager = itemManager;
		this.calculator = calculator;
		this.config = config;
		showOnInventory();
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		// Bank placeholders
		if (widgetItem.getQuantity() <= 0)
		{
			return;
		}

		boolean inBank = WidgetUtil.componentToInterface(widgetItem.getWidget().getId()) == InterfaceID.BANKMAIN;
		if (inBank ? !config.outlineBank() : !config.outlineInventory())
		{
			return;
		}

		Integer profit = calculator.getProfit(itemId);
		if (profit == null)
		{
			return;
		}

		ProfitTier tier = calculator.getTier(profit);
		if (tier == null || (tier == ProfitTier.BELOW_LOW && !config.outlineLoss()))
		{
			return;
		}

		Color color = calculator.getColor(tier);
		Rectangle bounds = widgetItem.getCanvasBounds();
		BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
		graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
	}
}
