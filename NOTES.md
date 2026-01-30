# Notes

## Implemented Features
- YAML player data saved in `plugins/SMPnir/players/<uuid>.yml`.
- Level curve and XP system with multi-level ups.
- Chat prefix + tab/display names show `[level + symbol] name` with gradient colors per 10 nir.
- Below-name stat display with live refresh; default is hours played.
- /questbook (and aliases) opens the book UI; stats and collections menus use chest GUIs.
- /nir admin commands: xp add/remove/set/get, level add/remove/set, collections/stats edits, debug, per-player rules (death chest toggle).
- /coords (alias /coordinates) prints current coordinates.
- Health scales from 8 hearts (level 1) to 20 hearts (level 100), survival worlds only.
- Hunger scales from 10 points (level 1) to 40 points (level 100) by level, survival worlds only. Not visually     though, this is happening using hunger change multiplier (40 being .25x change)
- Level-up sound + particle burst.
- Action-bar XP popups for all XP sources except playtime.
- Fishing collections tracking (unique items) + 300 XP on first find.
- Enchanted book XP rewards (halved values).
- Sleep vote skip-to-day with unanimous chat buttons.
- Death chest on player death (with coords message), lava burn timer + hologram countdown, respects per-player enable/disable.
## Future Features
- TODO: Special Event Nights
- /board opens a public editable book saved server-wide.
- Add ways to supercharge enchantments

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
- Playtime: 20 XP/min if active; survival multiplier `1 + 0.05 * hoursAlive` capped at 2×.
- Player kills: up to 4,800 XP = `round(4,800 * min(1, hoursAlive/20))`, only in allowed worlds.
- Mobs:
  - Passive/ambient/water: 10 XP
  - Monsters: 25 XP
  - Elite (Enderman, Blaze, Piglin Brute, Evoker, Ravager, Shulker): 40 XP
  - Bosses: Ender Dragon 5,000; Wither 3,000; Warden 8,000 (max 1/day)
- Mining (natural, not player-placed):
  - Copper/Coal: 15 XP
  - Quartz: 40 XP
  - Nether gold ore: 44 XP
  - Iron: 64 XP
  - Redstone/Lapis/Sculk shrieker: 72 XP
  - Gold: 80 XP
  - Emerald: 176 XP
  - Diamond: 180 XP
  - Ancient debris: 520 XP
- Spawner break (natural): 100 XP.
- Fishing: junk 10 XP; fish 40 XP; treasure 100 XP; +300 XP on first-time catch per collection item.
  - Other:
    - Sculk shrieker: 90 XP (~32s)
  - Note: ore veins can be large (coal/copper). Keep common ore XP low so a huge vein does not outvalue a few diamonds.

  - XP from spawner mobs divides the amount given from the mob by 5; bred animals are OK; natural monsters are OK.
- Harvesting crops (natural only, fully grown):
  - wheat/carrot/potato/beetroot/nether wart/cocoa/sweet berry/torchflower/pitcher: 6 XP (~2s)
  - melon/pumpkin slice tag: 20 XP
  - Sugar cane: 4 XP (~1s) per block (not placed)
    - XP also awarded when cane drops from non-player harvesting (e.g., piston) on pickup
    - if the sugar cane is placed into a chest after dropping (most of the time thru a hopper), no xp can be given (to deter easy idle xp)
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
- Quests
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

