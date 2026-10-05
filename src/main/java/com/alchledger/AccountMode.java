package com.alchledger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountMode
{
	AUTO("Auto-detect"),
	STANDARD("Standard"),
	IRONMAN("Ironman");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
