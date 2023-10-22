# Dedicated Applied Energistics

## Table of Contents


* [About](#about)
* [Quick Start](#quick-start)
* [Items](#items)
* [Blocks](#blocks)
* [Commands](#commands)
* [Downloads](#downloads)
* [Installation](#installation)
* [Issues](#issues)
* [Contacts](#contacts)
* [Credits](#credits)


## About

This Minecraft mod aims to make [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) work across multiple servers.

### Why is there a need for this mod?
 In large modpacks, servers can get extremely laggy because of large scale automation. A solution to this problem would be to connect multiple ME systems, on different servers together in order to spread the load.

### How dose the mod archive this?
Currently only the inventory is synchronised between servers. Meaning that cross server auto crafting doesn't work yet. The plan is to make the entire ME system work in the "cloud" with all features.

## Items

### Inter Dimensional Storage Cell
The Inter Dimensional Storage Cell connects to the database and stores all items in it. It is optimised to work with as many item types as possible.

![](https://github.com/DaNussi/DedicatedAppliedEnergistics/blob/master/src/main/resources/assets/dae2/textures/git/InterDimensionalStorageCell_Recipe.png?raw=true)

[//]: # (#### Limitations)
[//]: # (* +10.000 item types supportet)
[//]: # (* 2^64 max item amount per item type)

[//]: # (## Blocks)

[//]: # ()
[//]: # (### Inter Dimensional Interface)

[//]: # (This block is planned to hold the [Inter Dimensional Storage Cell]&#40;#inter-dimensional-storage-cell&#41;, but it is currently bugged.)

[//]: # ()
[//]: # (![]&#40;https://github.com/DaNussi/DedicatedAppliedEnergistics/blob/master/src/main/resources/assets/dae2/textures/git/InterDimensionalInterface.png?raw=true&#41;)


# Quick Start

Install redis locally or get one for free [here](https://app.redislabs.com/).

1. Use `/dae2 config set "<redis-url>"` to configure the mod to use the database. 
If you use a database on the same machine as your minecraft server you can use `/dae2 config set "redis://localhost:6379"`. 
If you need help with a cloud based database follow this [Guide](wiki/GUIDE.md)

3. Then enable the virtual inventory `/dae2 virtual_inventory enable`. This should only be enabled on ONE minecraft server that is running dae2.
4. Enable autostart `/dae2 autostart enable`.
5. Start  the mod `/dae2 start`
6. If everrything has gone well it should look like this now. `/dae2 status` ![img_1.png](wiki/img_1.png)

## Commands

### Starting

* `/dae2 start/stop/restart` Starts/Stops/Restart the mod.
* `/dae2 autostart enable/disable` Autostarts the mod when the minecraft server starts.


### Virtual Inventory

The virtual inventory must be enabled (default disabled) on only ONE server.

> [!WARNING]
> If multiple server have it enabled items might be lost or duplicated.

* `/dae2 virtual_inventory enable/disable`

## Downloads

* [GitHub](https://github.com/DaNussi/DedicatedAppliedEnergistics/releases)
* [CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/dedicatedappliedenergistics)

## Installation
I created this mod to be used with [ATM-8](https://www.curseforge.com/minecraft/modpacks/all-the-mods-8), please keep that in mind.

### Basic Install

#### Forge version
Install the correct version of Forge
* [forge-1.19.2-43.2.11-installer.jar](https://maven.minecraftforge.net/net/minecraftforge/forge/1.19.2-43.2.11/forge-1.19.2-43.2.11-installer.jar)

#### Jars
Add these jars to the `mod` folder!
* [dae2-1.19.2-0.0.2-all.jar](https://github.com/DaNussi/DedicatedAppliedEnergistics/releases/download/DAE2-1.19.2-0.0.2/dae2-1.19.2-0.0.2-all.jar)
* [appliedenergistics2-forge-12.9.4.jar](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/download/forge%2Fv12.9.4/appliedenergistics2-forge-12.9.4.jar)
* [appliedenergistics2-forge-12.9.4-api.jar](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/download/forge%2Fv12.9.4/appliedenergistics2-forge-12.9.4-api.jar)

## Issues

If you finde a bug or your game crashes please create a issue [here](https://github.com/DaNussi/DedicatedAppliedEnergistics/issues)!


## Contacts

* [Discord](https://discordapp.com/users/283218848130531329)
* [GitHub](https://github.com/DaNussi)
* [YouTube](https://www.youtube.com/channel/UClqALJaQu-uTKzWrPuYUbkA)
* [CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/dedicatedappliedenergistics)

## Credits

Thanks to everyone who contributed to [AE2](https://github.com/AppliedEnergistics/Applied-Energistics-2)!

## Donation
I am still a student and would appreciate a small donation. ;)

* [☕ Buy Me A Coffee](https://bmc.link/danussi)