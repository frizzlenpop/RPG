**RPG Skills Plugin (Minecraft 1.21)**

📜 Overview

RPGSkillsPlugin is a fully-fledged RPG-style skill system for Minecraft 1.21, designed to enhance gameplay with skill leveling, passive abilities, and active abilities. The plugin allows players to level up skills, unlock powerful abilities, and access a GUI to manage their progress.

✅ Key Features

Skill System: Gain XP and level up skills.

Active Abilities: Special abilities triggered by commands.

Passive Abilities: Automatically activated abilities when reaching milestones.

GUI Interface: View and manage skills in a clean graphical menu.

Data Persistence: Player progress is stored using YAML.

Command-Based Actions: Players can trigger skills and view progress using commands.

Event-Based XP Gains: Players earn XP from mining, farming, combat, fishing, and enchanting.

Modular Design: Easy to expand and integrate new skills and abilities.
-----------------------------------------------------------------------------------------------------------------


**🛠️ Installation & Setup**

Prerequisites

Minecraft Server 1.21 (Paper recommended)

Java 17+

Vault (optional, for economy integration)

Installation

Download the latest RPGSkillsPlugin.jar.

Place the .jar file into your server's plugins/ folder.

Restart your server to generate configuration files.

Edit the config files (if needed) and restart the server again.
-----------------------------------------------------------------------------------------------------------------
🎮 Gameplay & Features

📌 Skill System

Players can level up skills by performing relevant actions. Each skill has passive abilities unlocked at Level 5, 10, and 15.

📂 Current Skills & XP Gains

Skill / XP Source
Mining / Breaking ores, stone

Logging / Chopping trees

Farming / Harvesting crops

Fighting / Killing mobs

Fishing / Catching fish

Enchanting / Using enchanted books & items

🎯 XP Scaling & Leveling

XP increases dynamically based on level.

Formula: XP Required = (Level^1.5) * 100.

Passive abilities unlocked at Level 5, 10, 15.
-----------------------------------------------------------------------------------------------------------------
🎇 Active Abilities

Ability Name / Skill / Command / Effect

Mining Burst / Mining / /miningburst / Triple mining speed for 5s

Timber Chop / Logging / /timberchop / Instantly cut down trees

Berserker Rage / Fighting / /berserkerrage / +50% damage for 10s

Abilities have cooldowns, configurable in config.yml.

Players must unlock abilities by leveling up.
⚡ Passive Abilities

Passive abilities are automatically activated when a player reaches a milestone level.

Passive Ability Unlocks

Skill / Level 5 / Level 10 / Level 15

Mining / +10% XP boost / Auto-smelt ores / Double ore drops

Logging / +10% XP boost / Fast chop trees / Double wood drops

Farming / +10% XP boost / Auto-replant crops / Double crop yield

Fighting / +10% damage boost / Heal on kill / Critical hit bonus

Fishing / +10% XP boost / More treasure / Rare fish bonus

Enchanting / Faster research / Auto-upgrade books / Custom enchantments

Players can view their active passives using:

/passives
-----------------------------------------------------------------------------------------------------------------
🖥️ GUI Features

A custom GUI allows players to view skill progress and passive abilities.

Locked passives appear greyed out with unlock information.

Unlocked passives display in green.

Opens via command: /skills
-----------------------------------------------------------------------------------------------------------------
🛠️ Configuration (**COMING SOON**)

config.yml Options
```
cooldowns:
  miningburst: 60 # Cooldown in seconds
  timberchop: 30
  berserkerrage: 90

xp_multipliers:
  mining: 1.0
  logging: 1.0
  farming: 1.0
  fighting: 1.0
  fishing: 1.0
  enchanting: 1.0
```
Adjust XP multipliers for balancing.

Modify cooldowns for active abilities.
-----------------------------------------------------------------------------------------------------------------
**🔧 Developer Guide**

Project Structure
```
RPGSkillsPlugin/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── org/frizzlenpop/rPGSkillsPlugin/
│   │   │   │   ├── commands/ (Skill commands)
│   │   │   │   ├── data/ (Player data storage)
│   │   │   │   ├── gui/ (GUI system)
│   │   │   │   ├── listeners/ (XP event listeners)
│   │   │   │   ├── skills/ (Skill & Ability logic)
│   │   │   │   ├── RPGSkillsPlugin.java (Main class)
│   ├── resources/
│   │   ├── plugin.yml
│   │   ├── config.yml
```
How to Contribute

1. Clone the repository:
```
git clone https://github.com/YourUsername/RPGSkillsPlugin.git
```
2. Set up the project in IntelliJ IDEA or Eclipse.

3. Compile using Maven/Gradle.

4. Submit a pull request with new features!
-----------------------------------------------------------------------------------------------------------------
🎯 Future Plans

✅ More skills (Smithing, Alchemy, Cooking, etc.)
✅ Economy integration with Vault
✅ Party XP sharing system
✅ Custom enchantment expansion
-----------------------------------------------------------------------------------------------------------------
📜 License

This project is licensed under the MIT License. Contributions are welcome!

🚀 Join the development and help improve the RPG experience! 🎯
