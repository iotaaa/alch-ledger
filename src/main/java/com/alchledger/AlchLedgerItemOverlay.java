package com.alchledger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

class AlchLedgerItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final TooltipManager tooltipManager;
	private final ItemManager itemManager;
	private final ProfitCalculator calculator;
	private final AlchLedgerConfig config;

	@Inject
	AlchLedgerItemOverlay(Client client, TooltipManager tooltipManager, ItemManager itemManager,
		ProfitCalculator calculator, AlchLedgerConfig config)
	{
		this.client = client;
		this.tooltipManager = tooltipManager;
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

		Integer profit = calculator.getProfit(itemId);
		Rectangle bounds = widgetItem.getCanvasBounds();

		if (config.showAlchValueTooltip() && !client.isMenuOpen() && isHovered(bounds))
		{
			addTooltip(itemId, profit);
		}

		boolean inBank = WidgetUtil.componentToInterface(widgetItem.getWidget().getId()) == InterfaceID.BANKMAIN;
		if (inBank ? !config.outlineBank() : !config.outlineInventory())
		{
			return;
		}

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
		BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
		graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
	}

	private boolean isHovered(Rectangle bounds)
	{
		Point mouse = client.getMouseCanvasPosition();
		return bounds.contains(mouse.getX(), mouse.getY());
	}

	private void addTooltip(int itemId, Integer profit)
	{
		int id = itemManager.canonicalize(itemId);
		if (ProfitCalculator.isCurrency(id))
		{
			return;
		}

		ItemComposition item = itemManager.getItemComposition(id);
		if (!ProfitCalculator.isAlchable(item))
		{
			return;
		}
		int alchValue = item.getHaPrice();

		String text = "Alch value: " + QuantityFormatter.formatNumber(alchValue) + " gp";
		if (profit != null)
		{
			Color color = new Color(calculator.getColor(calculator.getTier(profit)).getRGB());
			text += "</br>Profit: " + ColorUtil.wrapWithColorTag(Format.gp(profit), color);
		}
		tooltipManager.add(new Tooltip(text));
	}
}
