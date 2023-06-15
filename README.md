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
The Inter Dimensional Storage Cell connects to a redis database and stores all items in it. It is optimised to work with as many item types as possible.

![](https://github.com/DaNussi/DedicatedAppliedEnergistics/blob/master/src/main/resources/assets/dae2/textures/git/InterDimensionalStorageCell_Recipe.png?raw=true)

#### Limitations
* +10.000 item types supportet
* 2^64 max item amount per item type

## Blocks

### Inter Dimensional Interface
This block is planned to hold the [Inter Dimensional Storage Cell](#inter-dimensional-storage-cell), but it is currently bugged.

![](https://github.com/DaNussi/DedicatedAppliedEnergistics/blob/master/src/main/resources/assets/dae2/textures/git/InterDimensionalInterface.png?raw=true)


# Quick Start

Install redis locally or get one for free [here](https://app.redislabs.com/).
Copy the Host, Port, Username and password into the commands and run them step by step. 

1. `/dae2 config set <host> <port> <username> <password>`
2. `/dae2 config apply`
3. `/dae2 virtual_inventory enable`
4. `/dae2 autostart enable`
5. `/dae2 restart`

## Commands

### Config File
There are two types of config files. 

* The first one is the current configuration. This config is loaded on start.
* The second one is the pending configuration. It can override the current configuration with `/dae2 config apply`.

### Starting

* `/dae2 start`
* `/dae2 restart`
* `/dae2 autostart enable`


### Stopping

* `/dae2 stop`
* `/dae2 autostart disable`

### Virtual Inventory

The virtual inventory must be enabled (default disabled) on only ONE server. If multiple server have it enabled items might be lost.

* `/dae2 virtual_inventory enable`
* `/dae2 virtual_inventory disable`

## Downloads

* [GitHub](https://github.com/DaNussi/DedicatedAppliedEnergistics/releases)
* [CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/dedicatedappliedenergistics)

## Installation
I created this mod to be used mainly with [ATM-8](https://www.curseforge.com/minecraft/modpacks/all-the-mods-8), but it can also be installed standalone.

With dependencies of course ;)

### Basic Install

#### Forge version
* [forge-1.19.2-43.2.11-installer.jar](https://maven.minecraftforge.net/net/minecraftforge/forge/1.19.2-43.2.11/forge-1.19.2-43.2.11-installer.jar)

#### Jars
These jars have to be in the /mods folder!
* [dae2-1.19.2-0.0.1-all.jar](https://github.com/DaNussi/DedicatedAppliedEnergistics/releases/download/Forge-1.19.2/dae2-1.19.2-0.0.1-all.jar)
* [appliedenergistics2-forge-12.9.4.jar](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/download/forge%2Fv12.9.4/appliedenergistics2-forge-12.9.4.jar)
* [appliedenergistics2-forge-12.9.4-api.jar](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/download/forge%2Fv12.9.4/appliedenergistics2-forge-12.9.4-api.jar)

### With ATM-8

Add these jars to the /mods folder!
* [dae2-1.19.2-0.0.1-all.jar](https://github.com/DaNussi/DedicatedAppliedEnergistics/releases/download/Forge-1.19.2/dae2-1.19.2-0.0.1-all.jar)
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