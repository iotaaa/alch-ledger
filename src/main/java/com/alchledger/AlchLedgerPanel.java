package com.alchledger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.QuantityFormatter;

class AlchLedgerPanel extends PluginPanel
{
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	@RequiredArgsConstructor
	enum View
	{
		BEST_GE("Best GE alchs"),
		INVENTORY("My inventory"),
		BANK("My bank"),
		LOG("Alch log");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	@Value
	private static class Row
	{
		int itemId;
		String name;
		String detail;
		long profit;
		ProfitTier tier;
	}

	private final Client client;
	private final ClientThread clientThread;
	private final ItemManager itemManager;
	private final ProfitCalculator calculator;
	private final ProfitTracker tracker;
	private final OwnedItems ownedItems;
	private final AlchLedgerConfig config;

	private final JLabel modeLabel = new JLabel();
	private final JLabel sessionLabel = new JLabel();
	private final JLabel rateLabel = new JLabel();
	private final JLabel xpLabel = new JLabel();
	private final JLabel allTimeLabel = new JLabel();
	private final JComboBox<View> viewBox = new JComboBox<>(View.values());
	private final JPanel listPanel = new JPanel();

	private boolean settingView;
	private boolean viewChosenByUser;
	// Bumped on every list rebuild, so slower stale results are dropped
	private int generation;
	// The last best GE alchs scan, which is only redone on open or Refresh
	private List<Row> geRows;
	private String geEmptyMessage;

	@Inject
	AlchLedgerPanel(Client client, ClientThread clientThread, ItemManager itemManager, ProfitCalculator calculator,
		ProfitTracker tracker, OwnedItems ownedItems, AlchLedgerConfig config)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.itemManager = itemManager;
		this.calculator = calculator;
		this.tracker = tracker;
		this.ownedItems = ownedItems;
		this.config = config;

		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Alch Ledger");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		top.add(title);

		modeLabel.setFont(FontManager.getRunescapeSmallFont());
		modeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		top.add(modeLabel);

		JPanel stats = new JPanel();
		stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
		stats.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		stats.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		for (JLabel label : new JLabel[]{sessionLabel, rateLabel, xpLabel, allTimeLabel})
		{
			label.setFont(FontManager.getRunescapeSmallFont());
			stats.add(label);
		}
		stats.setAlignmentX(LEFT_ALIGNMENT);
		top.add(spacer());
		top.add(stats);

		JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 4));
		buttons.setAlignmentX(LEFT_ALIGNMENT);
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(e -> refreshView(true));
		JButton reset = new JButton("Reset");
		reset.setToolTipText("Reset this session's profit");
		reset.addActionListener(e ->
		{
			tracker.resetSession();
			updateStats();
			if (getView() == View.LOG)
			{
				refreshView(false);
			}
		});
		JButton resetAll = new JButton("Reset all");
		resetAll.setToolTipText("Reset the all-time profit for this account");
		resetAll.addActionListener(e ->
		{
			int result = JOptionPane.showConfirmDialog(this,
				"Reset the all-time alch profit for this account?",
				"Reset all", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION)
			{
				tracker.resetAllTime();
				updateStats();
			}
		});
		JPanel resetButtons = new JPanel(new GridLayout(1, 2, 4, 0));
		resetButtons.add(reset);
		resetButtons.add(resetAll);
		for (JButton button : new JButton[]{refresh, reset, resetAll})
		{
			button.setMargin(new Insets(4, 4, 4, 4));
			button.setFocusPainted(false);
		}
		buttons.add(refresh);
		buttons.add(resetButtons);
		top.add(spacer());
		top.add(buttons);

		viewBox.setAlignmentX(LEFT_ALIGNMENT);
		viewBox.addActionListener(e ->
		{
			if (!settingView)
			{
				viewChosenByUser = true;
				refreshView(false);
			}
		});
		top.add(spacer());
		top.add(viewBox);

		title.setAlignmentX(LEFT_ALIGNMENT);
		modeLabel.setAlignmentX(LEFT_ALIGNMENT);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

		add(top, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);

		updateHeader();
		updateStats();
	}

	@Override
	public void onActivate()
	{
		refreshView(true);
	}

	/**
	 * Switches ironmen to the "My items" view, unless they picked a view themselves. Swing thread only.
	 */
	void applyDefaultView()
	{
		if (viewChosenByUser)
		{
			return;
		}
		View view = calculator.isIronman() ? View.INVENTORY : View.BEST_GE;
		if (view != getView())
		{
			settingView = true;
			viewBox.setSelectedItem(view);
			settingView = false;
			if (isShowing())
			{
				refreshView(true);
			}
		}
	}

	void updateHeader()
	{
		modeLabel.setText((calculator.isIronman() ? "Ironman" : "Standard")
			+ " · Nature rune " + QuantityFormatter.formatNumber(calculator.getNatureRunePrice()) + " gp");
	}

	void updateStats()
	{
		long sessionProfit = tracker.getSessionProfit();
		sessionLabel.setText("Session: " + Format.gp(sessionProfit) + " (" + plural(tracker.getSessionCasts()) + ")");
		sessionLabel.setForeground(Format.profitColor(sessionProfit));

		Long perHour = tracker.getProfitPerHour();
		rateLabel.setText("Profit/hr: " + (perHour == null ? "-" : Format.gp(perHour)));
		rateLabel.setForeground(Color.WHITE);

		Long xpPerHour = tracker.getXpPerHour();
		xpLabel.setText("Xp: " + QuantityFormatter.formatNumber(tracker.getSessionXp())
			+ " (" + (xpPerHour == null ? "-" : QuantityFormatter.formatNumber(xpPerHour)) + "/hr)");
		xpLabel.setForeground(Color.WHITE);
		xpLabel.setVisible(config.showXp());

		long allTime = tracker.getAllTimeProfit();
		allTimeLabel.setText("All time: " + Format.gp(allTime) + " (" + plural(tracker.getAllTimeCasts()) + ")");
		allTimeLabel.setForeground(Format.profitColor(allTime));
	}

	View getView()
	{
		return (View) viewBox.getSelectedItem();
	}

	/**
	 * Rebuilds the current list. The best GE alchs list only rescans items when {@code rescanGe} is set.
	 */
	void refreshView(boolean rescanGe)
	{
		updateHeader();
		updateStats();
		int gen = ++generation;
		switch (getView())
		{
			case BEST_GE:
				if (rescanGe || geRows == null)
				{
					showMessage("Loading...");
					clientThread.invokeLater(() -> buildGeList(gen));
				}
				else
				{
					showRows(geRows, geEmptyMessage);
				}
				break;
			case INVENTORY:
				clientThread.invokeLater(() -> buildOwnedList(gen, false));
				break;
			case BANK:
				if (!ownedItems.isBankSeen())
				{
					showMessage("Open your bank to load your bank items.");
					break;
				}
				clientThread.invokeLater(() -> buildOwnedList(gen, true));
				break;
			case LOG:
				showLog();
				break;
		}
	}

	private boolean buildGeList(int gen)
	{
		if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
		{
			return false;
		}

		int natureRunePrice = calculator.getNatureRunePrice();
		int minProfit = config.minProfitToList();
		boolean includeMembers = config.includeMembers();

		List<Row> rows = new ArrayList<>();
		int count = client.getItemCount();
		for (int id = 0; id < count; id++)
		{
			ItemComposition item = itemManager.getItemComposition(id);
			if (item.getNote() != -1
				|| item.getPlaceholderTemplateId() != -1
				|| !item.isTradeable()
				|| item.getHaPrice() <= 0
				|| "null".equals(item.getName())
				|| (!includeMembers && item.isMembers())
				|| ProfitCalculator.isCurrency(id))
			{
				continue;
			}

			int gePrice = (int) itemManager.getItemPrice(id);
			if (gePrice <= 0)
			{
				continue;
			}

			int profit = item.getHaPrice() - gePrice - natureRunePrice;
			if (profit < minProfit)
			{
				continue;
			}

			rows.add(new Row(id, item.getName(),
				"HA " + QuantityFormatter.formatNumber(item.getHaPrice()) + " / GE " + QuantityFormatter.formatNumber(gePrice)
					+ gpPerXpSuffix(profit),
				profit, calculator.getTier(profit)));
		}

		rows.sort(Comparator.comparingLong(Row::getProfit).reversed());
		List<Row> top = rows.size() > config.maxItemsListed() ? rows.subList(0, config.maxItemsListed()) : rows;
		List<Row> result = new ArrayList<>(top);

		String empty = "No items make at least " + QuantityFormatter.formatNumber(minProfit) + " gp per cast.";

		SwingUtilities.invokeLater(() ->
		{
			geRows = result;
			geEmptyMessage = empty;
			if (gen == generation)
			{
				showRows(result, empty);
			}
		});
		return true;
	}

	private boolean buildOwnedList(int gen, boolean bank)
	{
		if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
		{
			return false;
		}

		boolean ironman = calculator.isIronman();
		int minProfit = config.minProfitToList();

		List<Row> rows = new ArrayList<>();
		for (Map.Entry<Integer, Long> entry : (bank ? ownedItems.getBank() : ownedItems.getInventory()).entrySet())
		{
			int id = entry.getKey();
			long quantity = entry.getValue();
			Integer profit = calculator.getProfit(id);
			if (profit == null || profit < minProfit)
			{
				continue;
			}

			ItemComposition item = itemManager.getItemComposition(id);
			String prices = "HA " + QuantityFormatter.formatNumber(item.getHaPrice());
			if (!ironman)
			{
				prices += " / GE " + QuantityFormatter.formatNumber(itemManager.getItemPrice(id));
			}
			String detail = "x" + QuantityFormatter.quantityToStackSize(quantity)
				+ " · " + Format.gp(profit) + " ea · " + prices + gpPerXpSuffix(profit);
			rows.add(new Row(id, item.getName(), detail, profit * quantity, calculator.getTier(profit)));
		}

		rows.sort(Comparator.comparingLong(Row::getProfit).reversed());

		SwingUtilities.invokeLater(() ->
		{
			if (gen == generation)
			{
				showRows(rows, "No items in your " + (bank ? "bank" : "inventory") + " make at least "
					+ QuantityFormatter.formatNumber(minProfit) + " gp per cast.");
			}
		});
		return true;
	}

	private void showLog()
	{
		List<Row> rows = new ArrayList<>();
		for (AlchRecord record : tracker.getRecent())
		{
			String time = LocalTime.ofInstant(record.getTime(), ZoneId.systemDefault()).format(TIME_FORMAT);
			rows.add(new Row(record.getItemId(), record.getItemName(), time, record.getProfit(),
				calculator.getTier(record.getProfit())));
		}
		showRows(rows, "No casts yet this session.");
	}

	private void showMessage(String message)
	{
		listPanel.removeAll();
		JLabel label = new JLabel("<html>" + message + "</html>");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setAlignmentX(LEFT_ALIGNMENT);
		listPanel.add(label);
		listPanel.revalidate();
		listPanel.repaint();
	}

	private void showRows(List<Row> rows, String emptyMessage)
	{
		if (rows.isEmpty())
		{
			showMessage(emptyMessage);
			return;
		}

		listPanel.removeAll();
		for (Row row : rows)
		{
			listPanel.add(createRow(row));
			listPanel.add(spacer());
		}
		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel createRow(Row row)
	{
		JPanel panel = new JPanel(new BorderLayout(6, 0));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 6));
		panel.setAlignmentX(LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(36, 32));
		itemManager.getImage(row.getItemId()).addTo(icon);
		panel.add(icon, BorderLayout.WEST);

		JPanel text = new JPanel(new GridLayout(2, 1));
		text.setOpaque(false);

		JPanel firstLine = new JPanel(new BorderLayout(4, 0));
		firstLine.setOpaque(false);
		JLabel name = new JLabel(row.getName());
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(Color.WHITE);
		name.setToolTipText(row.getName());
		JLabel profit = new JLabel(Format.gp(row.getProfit()));
		profit.setFont(FontManager.getRunescapeSmallFont());
		profit.setForeground(tierColor(row));
		firstLine.add(name, BorderLayout.CENTER);
		firstLine.add(profit, BorderLayout.EAST);

		JLabel detail = new JLabel(row.getDetail());
		detail.setFont(FontManager.getRunescapeSmallFont());
		detail.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		detail.setToolTipText(row.getDetail());

		text.add(firstLine);
		text.add(detail);
		panel.add(text, BorderLayout.CENTER);
		return panel;
	}

	private Color tierColor(Row row)
	{
		Color color = calculator.getColor(row.getTier());
		// Drop the alpha, which is meant for outlines
		return color == null ? Color.WHITE : new Color(color.getRGB());
	}

	private String gpPerXpSuffix(int profit)
	{
		return config.showGpPerXp() ? " · " + Format.gpPerXp(profit) : "";
	}

	private static JPanel spacer()
	{
		JPanel spacer = new JPanel();
		spacer.setOpaque(false);
		spacer.setAlignmentX(LEFT_ALIGNMENT);
		spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
		spacer.setPreferredSize(new Dimension(0, 6));
		return spacer;
	}

	private static String plural(long casts)
	{
		return QuantityFormatter.formatNumber(casts) + (casts == 1 ? " cast" : " casts");
	}
}
