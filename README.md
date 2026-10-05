# Alch Ledger

Shows which items make a profit when high alched, outlines them by profit tier, and keeps a ledger of how much you've made from alching. Works for standard accounts and ironmen.

## Screenshots

| Side panel and alch log | My inventory | Overlay |
| --- | --- | --- |
| ![Side panel with session stats and the alch log](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/profit.png) | ![Profitable items in your inventory](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/inventory.png) | ![Session overlay](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/screencard.png) |

## Features

### Item outlines
Items in your inventory and bank are outlined by profit per cast:

| Tier | Default | Colour |
| --- | --- | --- |
| High | 500+ gp | Light blue |
| Medium | 100+ gp | Green |
| Low | 1+ gp | Yellow |
| Loss | below 0 gp (off by default) | Red |

The thresholds and colours can be changed, and outlines can be turned off for the inventory or bank.

### Side panel
- Your account mode and the nature rune price in use.
- Session profit and casts, profit per hour, and your all-time total.
- A dropdown with four views:
  - **Best GE alchs**: the most profitable items to buy from the Grand Exchange and alch.
  - **My inventory**: profitable items in your inventory, sorted by total profit for the whole stack.
  - **My bank**: the same for your bank (open your bank once to load it).
  - **Alch log**: your recent casts with the time, item and profit.

### Profit log
- Each High Level Alchemy cast is logged with the profit at the prices when it was cast.
- Session profit, profit per hour and an all-time total saved separately for each account.
- An overlay with this session's casts, profit and profit per hour.
- Optional: a chat message for each cast, Magic xp and xp/hr, and gp/xp for each item.

## How profit is worked out

- **Standard:** high alch value − GE price − nature rune price
- **Ironman:** high alch value − nature rune price (you already own the item)

The nature rune price defaults to 180 gp. You can set your own or use the live GE price. Your account type is detected automatically, or you can choose Standard or Ironman in the settings.

## Settings

| Section | Options |
| --- | --- |
| Prices | Account mode, nature rune price, use live nature rune price |
| Outline tiers | Outline in inventory/bank, outline losing items, tier thresholds and colours |
| Side panel | Show side panel, min profit to list, max items listed, include members items, show gp/xp |
| Profit log | Show profit overlay, chat message per cast, show xp and xp/hr |
