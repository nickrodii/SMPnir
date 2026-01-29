# Notes

## Implemented Features
- YAML player data saved in `plugins/SMPnir/players/<uuid>.yml`.
- Level curve and XP system with multi-level ups.
- Chat prefix + tab/display names show `[level + symbol] name` with gradient colors per 10 levels.
- Below-name stat display with live refresh; default is hours played.
- /questbook (and aliases) opens the book UI; stats and collections menus use chest GUIs.
- /nir admin commands: xp add/remove/set, level add/remove/set, totalxp, debug.
- /board opens a public editable book saved server-wide.
- /coords (alias /coordinates) prints current coordinates.
- Health scales from 8 hearts (level 1) to 20 hearts (level 100), survival worlds only.
- Hunger caps from 10 to 20 points by level, survival worlds only.
- Level-up sound + particle burst.
- Action-bar XP popups for all XP sources except playtime.
- Fishing collections tracking (unique items) + 300 XP on first find.
- Enchanted book XP rewards (halved values).

## Future Features
- Questbook will use the written-book GUI (book & quill style UI).
- Planned commands/aliases: `/level`, `/quests`, `/questbook`, `/book`, `/progress`, `/stats`, `/statistics`.
- Questbook should show level progress, stats, and quests.
- TODO: Add quests in a future version of the plugin.
- TODO: Special Event Nights

## Level Curve (Locked)
- Baseline: 10,000 XP/hour (about 167 XP/min).
- Curve: `xpToNext(L) = round(10000 * (0.837124916161829 + 0.007275362752072346 * L^1.35))`
- Examples (XP -> time at 10k XP/hour):
  - L1: 8,444 XP (~0.84h, ~51m)
  - L10: 10,000 XP (1.00h)
  - L50: 22,675 XP (~2.27h)
  - L90: 40,000 XP (4.00h)
  - L99: 44,343 XP (~4.43h)
- Total XP to level 100: 2,362,183 XP (~236.2h, ~9.8 days of hard grind).

## XP Sources (Draft with Recommended Values)
- Playtime (active only): 20 XP/min (1,200 XP/hour; ~7.2 minutes of baseline time per hour played).
  - AFK timer: stop playtime XP after 15 minutes without activity.
- Survival streak (time since last death):
  - Multiplier on playtime XP only: `1.0 + 0.05 * hoursAlive`, capped at 2.0x.
  - Dying resets the multiplier.
- Mining ores (natural generation only):
  - Common (least XP):
    - Copper: 20 XP (~7s)
    - Coal: 20 XP (~7s)
    - Quartz: 50 XP (~18s)
    - Nether gold ore: 55 XP (~20s)
  - Uncommon:
    - Iron: 80 XP (~29s)
    - Redstone: 90 XP (~32s)
    - Lapis: 90 XP (~32s)
  - Rare:
    - Gold: 160 XP (~58s)
    - Emerald: 220 XP (~1.3m)
  - Epic:
    - Diamond: 320 XP (~1.9m)
  - Legendary:
    - Ancient debris: 650 XP (~3.9m)
  - Other:
    - Sculk shrieker: 90 XP (~32s)
  - Note: ore veins can be large (coal/copper). Keep common ore XP low so a huge vein does not outvalue a few diamonds.
- Kills:
  - Player kill: `400 + 220 * victimStreakHours`, cap 4,800 XP (~2.4m to ~28.8m).
  - Passive mobs: 10 XP (~3.6s)
  - Hostile monsters: 25 XP (~9s)
  - Elite mobs: 40 XP (~14s)
  - Bosses:
    - Ender Dragon: 5,000 XP (~30m)
    - Wither: 3,000 XP (~18m)
    - Warden: 8,000 XP (~48m), cap 1 kill per day
  - No XP from spawner mobs; bred animals are OK; natural monsters are OK.
- Harvesting crops:
  - Wheat/Carrot/Potato/Beetroot/Nether wart: 6 XP (~2s)
  - Sugar cane: 4 XP (~1s) per block, natural growth only (not placed)
    - XP also awarded when cane drops from non-player harvesting (e.g., piston) on pickup
  - Glow berries: 6 XP (~2s) per harvest
  - Melon/Pumpkin: 20 XP (~7s) per block
  - Cocoa/Sweet berries: 6 XP (~2s)
- Using Minecraft XP (enchanting/anvil):
  - 1 plugin XP per 1 vanilla XP spent (~0.36s per XP at baseline).
- Enchanted books (first time per enchantment type):
  - 400 XP (~2.4m) + 100 XP per level above 1 (~0.6m each).
  - Final tier (max level) bonus: +2,000 XP (~12m) for that book.
  - Each individual book can reward only one player once (prevent trading for free rewards).
- Trading with villagers:
  - 15 XP per trade (~5s), plus bonus by villager level: +0/+5/+10/+15/+20 XP.
- Advancements:
  - Task: 250 XP (~1.5m)
  - Goal: 750 XP (~4.5m)
  - Challenge (purple): 2,000 XP (~12m)
- Quests (later).
- Biomes visited (first time per biome):
  - Any biome: 50 XP (~18s)

- Structure chest rewards (first player to open; then locked for everyone):
  - Ancient city: 650 XP
  - Mineshaft minecart chest: 220 XP
  - Jungle pyramid: 160 XP
  - Desert pyramid: 120 XP
  - Ocean ruins: 80 XP
  - Buried treasure: 400 XP
  - Stronghold: 220 XP
  - End city: 300 XP
  - Woodland mansion: 200 XP
  - Pillager outpost: 80 XP
  - Village: 40 XP
  - Ruined portal: 40 XP
  - Trial chamber: 40 XP
  - Nether fortress: 150 XP
  - Igloo: 40 XP
  - Shipwreck: 60 XP
  - Bastion: 400 XP
  - Monster spawner (natural): 100 XP
- Fishing:
  - Junk catch: 10 XP
  - Fish catch: 40 XP
  - Treasure catch: 100 XP
  - +300 XP when a new fishing item is added to the collection

## Quests (Future)
- First-time boss kills (one-time rewards):

- All quests are always visible (no rotating pools).
- Questbook UI layout:
  - Page 1 (Landing):
    - Line 1: "Level <N>"
    - Line 2: Progress bar
    - Line 3: "(current / next XP)"
    - Centered buttons: "STATISTICS" (dark blue, bold) and "QUESTS" (gold, bold)
    - Progress bar: green filled, gray unfilled
  - Page 2 (Statistics):
    - Title: "STATISTICS"
    - Buttons: Playtime, Mining, Combat, Exploration, Collections, Extra (bold)
    - Back button
  - Page 3 (Quests):
    - Title: "QUESTS"
    - List incomplete quests first (silver), then complete quests (gold) with "(COMPLETE)"

- Explode a 3x3 of TNT 3000 xp
- Get down to half a heart and regenerate to full HP without dying 4000 xp
- 5 wardens spawned in the same ancient city 7000 xp
- Kill the Ender Dragon for the first time: 15000 XP
- Kill the Wither for the first time: 9000 XP
- Kill a player with max survival streak 25000 xp

## One Time World Quests (Future)
- Beat the ender dragon (you must be in the dimension to get this reward)
- Reach 100 hours played

## Special Event Nights (Future)

- all charged creepers instead of creepers
- only baby zombies instead of zombies
- only chicken jockey instead of skeleton
- only spider jockey instead of skeleton
- all mobs given speed 2
- double every monster

# Bestiary special obtain notes

These are the special obtain instructions that were removed from the in-game bestiary display names
(to keep formatting consistent). Use these in your wiki or external notes.

- Allay: give them an item
- Axolotl: bucket it
- Camel Husk: ride a camel
- Cod: bucket it (not from fishing)
- Copper Golem: create one (also requires custom name "copper golem")
- Happy Ghast: ride on one (also requires custom name "happy ghast")
- Salmon: bucket it (not from fishing)
- Snow Golem: make one
- Strider: ride
- Tadpole: bucket it
- Iron Golem: create one
- Piglin: trade (barter)
- Pufferfish: bucket it
- Creaking: break root
- Zombie Villager: heal to normal villager
- Nautilus: custom name required ("nautilus")
- Zombie Nautilus: custom name required ("zombie nautilus")
- Parched: custom name required ("parched")

