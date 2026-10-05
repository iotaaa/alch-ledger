package com.alchledger;

import java.time.Instant;
import lombok.Value;

@Value
public class AlchRecord
{
	Instant time;
	int itemId;
	String itemName;
	int profit;
	int xp;
}
