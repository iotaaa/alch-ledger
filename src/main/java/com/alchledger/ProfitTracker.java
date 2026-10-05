package com.alchledger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

/**
 * Session and all-time alching profit. The all-time totals are saved per account.
 */
@Singleton
public class ProfitTracker
{
	private static final String ALL_TIME_PROFIT_KEY = "allTimeProfit";
	private static final String ALL_TIME_CASTS_KEY = "allTimeCasts";
	private static final int MAX_RECENT = 200;
	private static final Duration MIN_RATE_DURATION = Duration.ofMinutes(1);
	private static final Duration RATE_UPDATE_INTERVAL = Duration.ofSeconds(10);

	private final ConfigManager configManager;

	private final Deque<AlchRecord> recent = new ArrayDeque<>();
	private long sessionProfit;
	private int sessionCasts;
	private Instant sessionStart;
	private long allTimeProfit;
	private long allTimeCasts;
	private long sessionXp;
	// Hourly rates are only recalculated every few seconds so they don't flicker
	private Long profitPerHour;
	private Long xpPerHour;
	private Instant ratesUpdated;

	@Inject
	ProfitTracker(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public synchronized void loadAllTime()
	{
		allTimeProfit = parseLong(configManager.getRSProfileConfiguration(AlchLedgerConfig.GROUP, ALL_TIME_PROFIT_KEY));
		allTimeCasts = parseLong(configManager.getRSProfileConfiguration(AlchLedgerConfig.GROUP, ALL_TIME_CASTS_KEY));
	}

	public synchronized void record(AlchRecord record)
	{
		if (sessionStart == null)
		{
			sessionStart = record.getTime();
		}
		sessionProfit += record.getProfit();
		sessionXp += record.getXp();
		sessionCasts++;
		allTimeProfit += record.getProfit();
		allTimeCasts++;

		recent.addFirst(record);
		while (recent.size() > MAX_RECENT)
		{
			recent.removeLast();
		}

		saveAllTime();
	}

	public synchronized void resetSession()
	{
		sessionProfit = 0;
		sessionCasts = 0;
		sessionXp = 0;
		sessionStart = null;
		profitPerHour = null;
		xpPerHour = null;
		ratesUpdated = null;
		recent.clear();
	}

	public synchronized void resetAllTime()
	{
		allTimeProfit = 0;
		allTimeCasts = 0;
		saveAllTime();
	}

	public synchronized long getSessionProfit()
	{
		return sessionProfit;
	}

	public synchronized int getSessionCasts()
	{
		return sessionCasts;
	}

	public synchronized long getSessionXp()
	{
		return sessionXp;
	}

	public synchronized long getAllTimeProfit()
	{
		return allTimeProfit;
	}

	public synchronized long getAllTimeCasts()
	{
		return allTimeCasts;
	}

	/**
	 * @return profit per hour since the first cast, or null until a minute has passed
	 */
	public synchronized Long getProfitPerHour()
	{
		updateRates();
		return profitPerHour;
	}

	/**
	 * @return xp per hour since the first cast, or null until a minute has passed
	 */
	public synchronized Long getXpPerHour()
	{
		updateRates();
		return xpPerHour;
	}

	/**
	 * Recalculates the hourly rates, at most every {@link #RATE_UPDATE_INTERVAL}.
	 */
	private void updateRates()
	{
		Instant now = Instant.now();
		if (sessionStart == null
			|| (ratesUpdated != null && Duration.between(ratesUpdated, now).compareTo(RATE_UPDATE_INTERVAL) < 0))
		{
			return;
		}

		Duration elapsed = Duration.between(sessionStart, now);
		if (elapsed.compareTo(MIN_RATE_DURATION) < 0)
		{
			return;
		}

		double hours = elapsed.toMillis() / 3_600_000.0;
		profitPerHour = (long) (sessionProfit / hours);
		xpPerHour = (long) (sessionXp / hours);
		ratesUpdated = now;
	}

	/**
	 * @return the recent casts, newest first
	 */
	public synchronized List<AlchRecord> getRecent()
	{
		return new ArrayList<>(recent);
	}

	private void saveAllTime()
	{
		configManager.setRSProfileConfiguration(AlchLedgerConfig.GROUP, ALL_TIME_PROFIT_KEY, allTimeProfit);
		configManager.setRSProfileConfiguration(AlchLedgerConfig.GROUP, ALL_TIME_CASTS_KEY, allTimeCasts);
	}

	private static long parseLong(String value)
	{
		if (value == null)
		{
			return 0;
		}
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
}
