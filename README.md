
![SMPnir](https://cdn.modrinth.com/data/cached_images/babd30c4cdb6ac55de46ff9ab6be47f11bdb24e9.png)
[![Minecraft](https://img.shields.io/badge/Minecraft-PaperMC-4FC08D?style=for-the-badge)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)](https://www.oracle.com/java/technologies/downloads/)
[![Build](https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge)](https://maven.apache.org/)
[![Website](https://img.shields.io/badge/website-nickrodi.com-lightgrey?style=for-the-badge)](https://nickrodi.com)

**SMPnir** is a Paper Minecraft plugin built for small survival SMPs. It adds a full leveling + quest/collection system, quality‑of‑life features, and community tools that make long‑term play rewarding without changing core survival gameplay *too much*.

This plugin is designed to be **plug and play**, giving server admins a complete progression system with rich UI, clear goals, and meaningful rewards right out of the box.

### [<div align="center">Download on Modrinth</div>](https://modrinth.com/project/smpnir)

---

**Key Features**

**Leveling & XP Progression**
- 100 levels that progressively get harder to achieve with cool level‑up effects and extra hearts/hunger to reward leveling
- New cool badges and colors next to your display name every 10 levels!
- XP from playtime, staying alive, mining, mobs, crops, fishing, trades, structure chests, advancements, quests, collecting, enchanting, and even building (manual build submissions)
- HUD XP popups when XP is acquired
- Introduces a grinding-oriented late game where the players can kill the wither or even the warden to gain xp

**Questbook UI & Stat Displays**
- `/book` interactive book with stats, collections, quests, and player info
- Clickable navigation and even show off a per‑stat below‑name display for everyone to see
- `/xplist` shows XP sources and values

**Collections & Bestiary**
- First‑time collections for enchants, biomes, fishing items, music discs, and killing, breeding, or taming new mobs
- Bestiary tracks mob discoveries with XP rewards

**Survival Gameplay Additions**
- Level‑based health and hunger scaling in survival worlds, starting with 8 hearts from level 1 and achieving 20 hearts at level 100
- Death chest system with a coordinates message upon death and a lava burn timer when the chest is ingulfed in lava (that can be turned off per-player)
- Unanimous sleep vote to skip the night
- Redesigned sleep system where, once at least one player is sleeping, the rest of the players can all vote to skip to day (instead of everyone leaving to skip to day)

**Community Tools**
- Level badges and gradient name colors in chat, plus tab‑list progress header
- `/leaderboard` for top players and `/coords` for quick location sharing
- `/build` submissions with group support, admin grading, and XP share claims

---

**Installation**

1. Ensure you have a working [installation of PaperMC](https://papermc.io/downloads/paper) on your Minecraft server
2. Download the latest `.jar` release from Modrinth
3. Place it into your server’s `plugins` folder
4. Ensure that the `worlds: ` line in `config.yml` is set to your survival world
4. Restart your server

---

**Commands & Permissions**

| Command | Permission | Description |
|--------|------------|-------------|
| `/nir` | `smpnir.admin` | Admin toolbox for XP, levels, collections, quests, and debug. |
| `/coords` | `smpnir.use` | Show your coordinates. |
| `/build` | `smpnir.use` and `smpnir.admin` | Submit builds, view history, claim graded XP. Admins can grade. |
| `/book` | `smpnir.use` | Open the questbook (aliases: `level`, `quests`, `book`, `progress`, `stats`, `statistics`, `b`). |
| `/leaderboard` | `smpnir.use` | Show top players. |
| `/smphelp` | `smpnir.use` | Show welcome/help message. |
| `/xplist` | `smpnir.use` | Show XP sources in the book. |

**Default permissions:**  
`smpnir.use` defaults to `non-op`; `smpnir.admin` defaults to `op`.
