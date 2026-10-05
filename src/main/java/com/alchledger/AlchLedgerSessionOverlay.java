package com.alchledger;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.QuantityFormatter;

class AlchLedgerSessionOverlay extends OverlayPanel
{
	private final ProfitTracker tracker;
	private final AlchLedgerConfig config;

	@Inject
	AlchLedgerSessionOverlay(AlchLedgerPlugin plugin, ProfitTracker tracker, AlchLedgerConfig config)
	{
		super(plugin);
		this.tracker = tracker;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(150, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showProfitOverlay() || tracker.getSessionCasts() == 0)
		{
			return null;
		}

		long profit = tracker.getSessionProfit();
		Long perHour = tracker.getProfitPerHour();

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Alch Ledger")
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Casts:")
			.right(QuantityFormatter.formatNumber(tracker.getSessionCasts()))
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Profit:")
			.right(Format.gp(profit))
			.rightColor(Format.profitColor(profit))
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Profit/hr:")
			.right(perHour == null ? "-" : Format.gpShort(perHour))
			.build());

		if (config.showXp())
		{
			Long xpPerHour = tracker.getXpPerHour();
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Xp:")
				.right(QuantityFormatter.formatNumber(tracker.getSessionXp()))
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Xp/hr:")
				.right(xpPerHour == null ? "-" : QuantityFormatter.quantityToStackSize(xpPerHour))
				.build());
		}

		return super.render(graphics);
	}
}
