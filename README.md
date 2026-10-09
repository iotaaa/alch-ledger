# Alch Ledger

Shows which items make a profit when high alched, outlines them by profit tier, and keeps a ledger of how much you've made from alching. Works for standard accounts and ironmen.

<p align="center">
  <img src="https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/sidebar.png" alt="Best GE alchs">
  <img src="https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/inventory.png" alt="My inventory">
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/alchlog.png" alt="Alch log">
  <img src="https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/alltime.png" alt="All-time by item">
</p>

## Side panel

Shows your account mode, the nature rune price in use, session profit, profit per hour and your all-time total. A dropdown switches between:

- **Best GE alchs**: the most profitable items to buy from the Grand Exchange and alch
- **My inventory**: profitable items in your inventory, sorted by profit for the whole stack
- **My bank**: the same for your bank (open your bank once to load it)
- **Alch log**: your recent casts with the time, item and profit
- **All-time by item**: every item you've alched on this account, with its casts and total profit

## Item outlines

Items in your inventory and bank are outlined by how much profit they make per cast:

- **Super** (5,000+ gp): purple
- **High** (500+ gp): light blue
- **Medium** (100+ gp): green
- **Low** (1+ gp): yellow
- **Below low** (anything under the low tier, including losses): red, off by default

You can change the thresholds and colours, and turn outlines off for the inventory or bank. You can also turn on a tooltip that shows an item's alch value and profit per cast when you hover over it.

## Profit tracking

Every High Level Alchemy cast is logged at the prices when you cast it. Session profit, profit per hour, an all-time total and the totals for each item are kept separately for each account.

An overlay shows this session's casts, profit and profit per hour. You can also turn on a chat message for each cast, Magic xp and xp/hr, and gp/xp for each item.

<p align="center">
  <img src="https://raw.githubusercontent.com/iotaaa/alch-ledger/main/images/screencard.png" alt="Session overlay">
</p>

## How profit is worked out

- **Standard:** high alch value - GE price - nature rune price
- **Ironman:** high alch value - nature rune price (you already own the item)

The GE price is the higher of RuneLite's GE guide price and wiki price, so rarely traded items with an out-of-date price don't look more profitable than they are. Items that can't be alched are left out, and Best GE alchs also skips items that never trade on the GE.

The nature rune price defaults to 180 gp. You can set your own or use the live GE price. Your account type is detected automatically, or you can choose Standard or Ironman in the settings.

## Author

You can find my socials at [iotaaa.co.uk](https://www.iotaaa.co.uk/).
