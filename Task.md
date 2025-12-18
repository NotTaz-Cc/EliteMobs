# Task.md - Convert EliteMobs to 100% Folia Support

**Developer**: MinhTaz  
**Project**: EliteMobs Plugin  
**Total Files to Convert**: ~200-250 files  
**Status**: In Progress

## Overview
This document tracks the conversion of EliteMobs plugin from standard Bukkit API to 100% Folia support. The conversion uses the FoliaLib library located in `me/MinhTaz/FoliaLib/` which provides:

- `TaskScheduler` - Folia-compatible task scheduling
- `EntityManager` - Entity operations for Folia
- `WorldManager` - World operations for Folia

## File Conversion Status

### 🔴 HIGH PRIORITY - Core Files (Must Convert)

#### Plugin Entry Point
- [ ] `EliteMobs.java` - Main plugin class with scheduler usage
- [ ] `EliteMobsConfig.java` - Configuration handling

#### Event System
- [ ] `CustomEvent.java` - Event scheduling
- [ ] `TimedEvent.java` - Timer events  
- [ ] `ActionEvent.java` - Action events

#### Player Data Management
- [ ] `ElitePlayerInventory.java` - Player inventory management
- [ ] `PlayerData.java` - Player data handling
- [ ] `PlayerStatusScreen.java` - Player UI management

#### Entity Management
- [ ] `EntityTracker.java` - Entity tracking and management
- [ ] `CustomSpawn.java` - Entity spawning
- [ ] `PersistentObjectHandler.java` - Object persistence

#### Custom Boss System
- [ ] `CustomBossEntity.java` - Boss entity management
- [ ] `RegionalBossEntity.java` - Regional boss handling
- [ ] `CustomBossEscapeMechanism.java` - Boss AI and movement

#### Arena System
- [ ] `ArenaInstance.java` - Arena instance management
- [ ] `MatchInstance.java` - Match management

#### Dungeon System
- [ ] `DungeonInstance.java` - Dungeon instance management
- [ ] `DynamicDungeonInstance.java` - Dynamic dungeon handling
- [ ] `DungeonProtector.java` - Dungeon protection

#### Item System
- [ ] `SharedLootTable.java` - Loot table management
- [ ] `ItemLootShower.java` - Loot display system
- [ ] `RareDropEffect.java` - Rare drop effects

#### Power System
- [ ] `ScriptAction.java` - Script action execution
- [ ] `FrostCone.java` - Power effects
- [ ] `ShieldWall.java` - Power effects
- [ ] `SpiritWalk.java` - Power effects

#### Third Party Integrations
- [ ] `DisguiseEntity.java` - LibsDisguises integration
- [ ] `NPCProximitySensor.java` - NPC proximity detection
- [ ] `NPCInteractions.java` - NPC interaction handling
- [ ] `VersionChecker.java` - Version checking system

### 🟡 MEDIUM PRIORITY - Utility Files

#### Combat System
- [ ] `EliteProjectile.java` - Projectile handling
- [ ] `PreventPathfindingExploit.java` - Anti-exploit measures
- [ ] `BossHealthDisplay.java` - Health display management

#### Menu System
- [ ] `EliteMenu.java` - Menu base class
- [ ] `RepairMenu.java` - Repair menu functionality
- [ ] `ItemEnchantmentMenu.java` - Enchantment menu
- [ ] `UnbindMenu.java` - Item unbind menu

#### Treasure System
- [ ] `TreasureChest.java` - Treasure chest management
- [ ] `CustomTreasureChestsConfig.java` - Treasure chest configuration

#### World System
- [ ] `FindNewWorlds.java` - World discovery
- [ ] `WorldDungeonPackage.java` - World package management
- [ ] `DungeonUtils.java` - Dungeon utility functions

#### Event Packages
- [ ] `EventsPackage.java` - Event package handling
- [ ] `ItemsPackage.java` - Item package management

#### Quest System
- [ ] `QuestTracking.java` - Quest tracking and management

### 🟢 LOW PRIORITY - Config and Helper Files

#### Configuration Files
- [ ] `TranslationsConfigFields.java` - Translation handling
- [ ] `WorldSwitchBehavior.java` - World switching behavior
- [ ] `GuildRank.java` - Guild ranking system

#### Utility Classes
- [ ] `StringColorAnimator.java` - Color animation
- [ ] `SimpleScoreboard.java` - Scoreboard management
- [ ] `VisualDisplay.java` - Visual effects
- [ ] `WeightedProbability.java` - Probability calculations

#### Shape Utilities
- [ ] `Ray.java` - Ray calculations
- [ ] `Dome.java` - Dome shapes
- [ ] `Cuboid.java` - Cuboid shapes
- [ ] `Cylinder.java` - Cylinder shapes
- [ ] `Sphere.java` - Sphere shapes

#### Custom Enchantments
- [ ] `LightningEnchantment.java` - Lightning enchantment
- [ ] `EarthquakeEnchantment.java` - Earthquake effect
- [ ] `PlasmaBootsEnchantment.java` - Plasma boots
- [ ] `FlamethrowerEnchantment.java` - Flamethrower effect
- [ ] `DrillingEnchantment.java` - Drilling effect
- [ ] `MeteorShowerEnchantment.java` - Meteor shower

#### Item Management
- [ ] `CustomItem.java` - Custom item handling
- [ ] `VanillaCustomLootEntry.java` - Custom loot entries
- [ ] `LootTables.java` - Loot table management
- [ ] `DefaultDropsHandler.java` - Default drop handling

### 📝 DETAILED CONVERSION PATTERNS

#### Pattern 1: Scheduler Conversion
**Before:**
```java
Bukkit.getScheduler().runTask(plugin, task);
Bukkit.getScheduler().runTaskLater(plugin, task, delay);
Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
```

**After:**
```java
// Import FoliaLib
import me.MinhTaz.FoliaLib.TaskScheduler;

private TaskScheduler scheduler = new TaskScheduler(plugin);

// Usage
scheduler.runAsync(task);
scheduler.runDelayedAsync(task, delay);
scheduler.runTimerAsync(task, delay, period);
```

#### Pattern 2: World Access Conversion
**Before:**
```java
World world = Bukkit.getWorld(worldName);
Chunk chunk = world.getChunkAt(x, z);
Block block = location.getBlock();
```

**After:**
```java
// Import FoliaLib
import me.MinhTaz.FoliaLib.WorldManager;

private WorldManager worldManager = new WorldManager(plugin);

// Usage
CompletableFuture<World> future = worldManager.getWorldAsync(worldName);
CompletableFuture<Chunk> chunkFuture = worldManager.loadChunkAsync(world, x, z);
CompletableFuture<Block> blockFuture = worldManager.getBlockAtAsync(location);
```

#### Pattern 3: Entity Access Conversion
**Before:**
```java
Entity entity = Bukkit.getEntity(entityUUID);
List<Entity> entities = world.getEntities();
```

**After:**
```java
// Import FoliaLib
import me.MinhTaz.FoliaLib.EntityManager;

private EntityManager entityManager = new EntityManager(plugin);

// Usage
CompletableFuture<Entity> entityFuture = entityManager.getEntityAsync(entityUUID);
CompletableFuture<List<Entity>> entitiesFuture = entityManager.getEntitiesInWorldAsync(world, filter);
```

### 📋 CONVERSION CHECKLIST

For each file, perform these steps:

1. **Import FoliaLib classes** at the top of the file
2. **Replace scheduler calls** with TaskScheduler usage
3. **Replace world access** with WorldManager async methods
4. **Replace entity access** with EntityManager async methods
5. **Update method signatures** if needed for CompletableFuture returns
6. **Test compatibility** with both Folia and Paper servers
7. **Handle exceptions** properly for async operations

### 🎯 MIGRATION STRATEGY

1. **Phase 1**: Convert core plugin files (High Priority)
2. **Phase 2**: Convert utility and helper files (Medium Priority)  
3. **Phase 3**: Convert configuration and low-priority files (Low Priority)
4. **Phase 4**: Testing and optimization on both Paper and Folia servers

### 🔧 ADDITIONAL FOLIALIB CLASSES TO CREATE

- [ ] `PacketManager` - Network packet handling for Folia
- [ ] `ChunkManager` - Chunk operations optimization
- [ ] `PhysicsManager` - Block physics for Folia
- [ ] `InventoryManager` - Inventory operations for Folia
- [ ] `SoundManager` - Sound effect handling for Folia

### 📊 CURRENT PROGRESS

**Total Files**: ~1317 Java files  
**Files Using Scheduler**: 30  
**Files Using World Access**: 134+  
**Files Using Entity Operations**: 200+  
**Estimated Conversion Time**: 2-3 weeks  
**Current Status**: ✅ Phase 0 - FoliaLib Foundation Complete

## ✅ COMPLETED - FoliaLib Foundation

### Created FoliaLib Library Structure
- [x] **TaskScheduler.java** - Task scheduling for Folia with Bukkit fallback
- [x] **EntityManager.java** - Safe entity operations for Folia
- [x] **WorldManager.java** - World operations with async support  
- [x] **InventoryManager.java** - Inventory management for Folia
- [x] **ChunkManager.java** - Chunk operations with retry logic
- [x] **PhysicsManager.java** - Physics simulation for Folia servers
- [x] **SoundManager.java** - Sound operations with region support

### Build Status
- [x] **Compilation**: ✅ SUCCESS (0 errors)
- [x] **Shadow JAR**: ✅ SUCCESS (Build completed)
- [x] **Folia compatibility**: ✅ Ready with fallback support

### Key Features Implemented
- [x] **Region-based execution** (global, world, entity, player)
- [x] **Async operations** with CompletableFuture support
- [x] **Error handling** và logging in all classes
- [x] **Cross-thread safety** cho inventory và entity operations
- [x] **Fallback compatibility** with Paper/Bukkit servers
- [x] **TaskWrapper interface** for unified task management

---

**Notes**:
- Always test on both Paper 1.20+ and Folia servers
- Maintain backward compatibility where possible
- Use async operations extensively for Folia compatibility
- Follow Folia's region-based execution model
- Document any breaking changes clearly