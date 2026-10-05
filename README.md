# Alch Ledger

Shows which items make a profit when high alched, outlines them by profit tier, and keeps a ledger of how much you've made from alching. Works for standard accounts and ironmen.

![Side panel with session stats and the alch log](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/profit.png)

## Item outlines

Items in your inventory and bank are outlined by how much profit they make per cast:

- **High** (500+ gp): light blue
- **Medium** (100+ gp): green
- **Low** (1+ gp): yellow
- **Below low** (anything under the low tier, including losses): red, off by default

You can change the thresholds and colours, and turn outlines off for the inventory or bank.

## Side panel

Shows your account mode, the nature rune price in use, session profit, profit per hour and your all-time total. A dropdown switches between:

- **Best GE alchs**: the most profitable items to buy from the Grand Exchange and alch
- **My inventory**: profitable items in your inventory, sorted by profit for the whole stack
- **My bank**: the same for your bank (open your bank once to load it)
- **Alch log**: your recent casts with the time, item and profit

![Profitable items in your inventory](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/inventory.png)

## Profit tracking

Every High Level Alchemy cast is logged at the prices when you cast it. Session profit, profit per hour and an all-time total are kept separately for each account.

An overlay shows this session's casts, profit and profit per hour. You can also turn on a chat message for each cast, Magic xp and xp/hr, and gp/xp for each item.

![Session overlay](https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/screencard.png)

## How profit is worked out

- **Standard:** high alch value - GE price - nature rune price
- **Ironman:** high alch value - nature rune price (you already own the item)

The nature rune price defaults to 180 gp. You can set your own or use the live GE price. Your account type is detected automatically, or you can choose Standard or Ironman in the settings.

## Author

Made by iotaaa. More of my work at [iotaaa.co.uk](https://www.iotaaa.co.uk/).
