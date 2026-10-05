package com.alchledger;

import com.google.inject.Provides;
import java.time.Instant;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Alch Ledger",
	description = "Shows which items profit from High Level Alchemy, outlines them by profit tier and logs your alching profit",
	tags = {"alch", "alchemy", "high alch", "magic", "profit", "money", "ironman", "nature rune"}
)
public class AlchLedgerPlugin extends Plugin
{
	private static final String HIGH_ALCH_SPELL = "High Level Alchemy";
	private static final int CAST_TICK_WINDOW = 5;
	private static final int STATS_REFRESH_TICKS = 10;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AlchLedgerConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ProfitCalculator calculator;

	@Inject
	private ProfitTracker tracker;

	@Inject
	private OwnedItems ownedItems;

	@Inject
	private AlchLedgerItemOverlay itemOverlay;

	@Inject
	private AlchLedgerSessionOverlay sessionOverlay;

	private AlchLedgerPanel panel;
	private NavigationButton navButton;

	private int pendingItemId = -1;
	private int pendingTick;
	private int lastMagicXp = -1;
	private Boolean lastIronman;

	@Override
	protected void startUp()
	{
		overlayManager.add(itemOverlay);
		overlayManager.add(sessionOverlay);

		panel = injector.getInstance(AlchLedgerPanel.class);
		navButton = NavigationButton.builder()
			.tooltip("Alch Ledger")
			.icon(ImageUtil.loadImageResource(AlchLedgerPlugin.class, "nav_icon.png"))
			.priority(7)
			.panel(panel)
			.build();
		if (config.showSidePanel())
		{
			clientToolbar.addNavigation(navButton);
		}

		tracker.loadAllTime();
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				// Started mid-session: pick up the current xp and containers
				lastMagicXp = client.getSkillExperience(Skill.MAGIC);
				updateContainer(client.getItemContainer(InventoryID.INV), false);
				updateContainer(client.getItemContainer(InventoryID.BANK), true);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(itemOverlay);
		overlayManager.remove(sessionOverlay);
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;

		calculator.clearCache();
		calculator.setIronmanDetected(false);
		tracker.resetSession();
		ownedItems.clear();
		pendingItemId = -1;
		lastMagicXp = -1;
		lastIronman = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		calculator.clearCache();

		boolean ironman = client.getVarbitValue(VarbitID.IRONMAN) != 0;
		calculator.setIronmanDetected(ironman);
		if (lastIronman == null || lastIronman != ironman)
		{
			lastIronman = ironman;
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.updateHeader();
					panel.applyDefaultView();
				}
			});
		}

		if (client.getTickCount() % STATS_REFRESH_TICKS == 0)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null && panel.isShowing())
				{
					panel.updateStats();
				}
			});
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			// The first xp update after logging in isn't a cast
			lastMagicXp = -1;
			pendingItemId = -1;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!AlchLedgerConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		calculator.clearCache();

		if ("showSidePanel".equals(event.getKey()))
		{
			if (config.showSidePanel())
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				clientToolbar.removeNavigation(navButton);
			}
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			if (panel == null)
			{
				return;
			}
			panel.applyDefaultView();
			if (panel.isShowing())
			{
				panel.refreshView(true);
			}
			else
			{
				panel.updateHeader();
			}
		});
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		tracker.loadAllTime();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.updateStats();
			}
		});
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.INV)
		{
			updateContainer(event.getItemContainer(), false);
		}
		else if (id == InventoryID.BANK)
		{
			updateContainer(event.getItemContainer(), true);
		}
	}

	private void updateContainer(ItemContainer container, boolean bank)
	{
		if (container == null)
		{
			return;
		}

		if (bank)
		{
			ownedItems.setBank(OwnedItems.count(container.getItems(), itemManager::canonicalize));
		}
		else
		{
			ownedItems.setInventory(OwnedItems.count(container.getItems(), itemManager::canonicalize));
		}

		SwingUtilities.invokeLater(() ->
		{
			if (panel != null && panel.isShowing() && panel.getView() == (bank ? AlchLedgerPanel.View.BANK : AlchLedgerPanel.View.INVENTORY))
			{
				panel.refreshView(false);
			}
		});
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!"Cast".equals(event.getMenuOption()))
		{
			return;
		}

		String target = Text.removeTags(event.getMenuTarget());
		if (!target.startsWith(HIGH_ALCH_SPELL))
		{
			return;
		}

		int itemId = event.getItemId();
		if (itemId <= 0 && event.getWidget() != null)
		{
			itemId = event.getWidget().getItemId();
		}
		if (itemId <= 0)
		{
			// Selecting the spell, not casting it on an item
			return;
		}

		pendingItemId = itemId;
		pendingTick = client.getTickCount();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.MAGIC)
		{
			return;
		}

		int xp = event.getXp();
		int previous = lastMagicXp;
		lastMagicXp = xp;
		if (previous < 0 || pendingItemId <= 0)
		{
			return;
		}

		int gained = xp - previous;
		if (gained >= ProfitCalculator.HIGH_ALCH_XP && client.getTickCount() - pendingTick <= CAST_TICK_WINDOW)
		{
			recordCast(pendingItemId, gained);
			pendingItemId = -1;
		}
	}

	private void recordCast(int itemId, int xp)
	{
		int id = itemManager.canonicalize(itemId);
		String name = itemManager.getItemComposition(id).getName();

		Integer profit = calculator.getProfit(id);
		if (profit == null)
		{
			// No GE price in standard mode, e.g. an untradeable item: nothing was paid for it
			profit = itemManager.getItemComposition(id).getHaPrice() - calculator.getNatureRunePrice();
		}

		tracker.record(new AlchRecord(Instant.now(), id, name, profit, xp));

		if (config.chatMessagePerCast())
		{
			String message = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("High alch: " + name + " ")
				.append(profit >= 0 ? ChatColorType.HIGHLIGHT : ChatColorType.NORMAL)
				.append(Format.gp(profit))
				.append(ChatColorType.NORMAL)
				.append(" (session " + Format.gp(tracker.getSessionProfit()) + ")")
				.build();
			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(message)
				.build());
		}

		SwingUtilities.invokeLater(() ->
		{
			if (panel == null)
			{
				return;
			}
			panel.updateStats();
			if (panel.isShowing() && panel.getView() == AlchLedgerPanel.View.LOG)
			{
				panel.refreshView(false);
			}
		});
	}

	@Provides
	AlchLedgerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AlchLedgerConfig.class);
	}
}
