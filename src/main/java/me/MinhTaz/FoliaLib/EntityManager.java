package me.MinhTaz.FoliaLib;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * EntityManager - Folia compatible entity operations
 * Developer: MinhTaz
 * This class provides Folia-compatible entity management with fallback support
 */
public class EntityManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public EntityManager(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFoliaServer();
    }
    
    private boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Get entity by UUID safely on Folia
     */
    public CompletableFuture<Entity> getEntityAsync(UUID entityUUID) {
        CompletableFuture<Entity> future = new CompletableFuture<>();
        
        if (isFolia) {
            // On Folia, we need to find the entity in the correct region
            runEntitySearch(entityUUID, future);
        } else {
            // Fallback to synchronous lookup
            Entity entity = Bukkit.getEntity(entityUUID);
            future.complete(entity);
        }
        
        return future;
    }
    
    /**
     * Get entities in world with predicate filter
     */
    public CompletableFuture<List<Entity>> getEntitiesInWorldAsync(World world, Predicate<Entity> filter) {
        CompletableFuture<List<Entity>> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldEntitySearch(world, filter, future);
        } else {
            // Synchronous fallback
            List<Entity> entities = world.getEntities().stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            future.complete(entities);
        }
        
        return future;
    }
    
    /**
     * Get players in world
     */
    public CompletableFuture<List<Player>> getPlayersInWorldAsync(World world) {
        CompletableFuture<List<Player>> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldPlayerSearch(world, future);
        } else {
            // Synchronous fallback
            List<Player> players = world.getPlayers();
            future.complete(players);
        }
        
        return future;
    }
    
    /**
     * Run operation on entity safely
     */
    public TaskScheduler.TaskWrapper runWithEntity(UUID entityUUID, Runnable operation) {
        getEntityAsync(entityUUID).thenAccept(entity -> {
            if (entity != null) {
                if (isFolia) {
                    runEntityTask(entity, operation);
                } else {
                    operation.run();
                }
            }
        });
        // Return null as we can't get a specific task from async completion
        return null;
    }
    
    /**
     * Run operation on all players in world
     */
    public java.util.List<TaskScheduler.TaskWrapper> runWithPlayersInWorld(World world, Runnable operation) {
        java.util.List<TaskScheduler.TaskWrapper> tasks = new java.util.ArrayList<>();
        getPlayersInWorldAsync(world).thenAccept(players -> {
            if (isFolia) {
                players.forEach(player -> tasks.add(runPlayerTask(player, operation)));
            } else {
                players.forEach(player -> {
                    if (player != null) operation.run();
                });
            }
        });
        return tasks;
    }
    
    // Private helper methods
    
    private void runEntitySearch(UUID entityUUID, CompletableFuture<Entity> future) {
        try {
            // Try to get entity from all worlds
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(entityUUID)) {
                        future.complete(entity);
                        return;
                    }
                }
            }
            future.complete(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to search for entity: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runWorldEntitySearch(World world, Predicate<Entity> filter, CompletableFuture<List<Entity>> future) {
        try {
            List<Entity> result = new ArrayList<>();
            for (Entity entity : world.getEntities()) {
                if (filter.test(entity)) {
                    result.add(entity);
                }
            }
            future.complete(result);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to search entities in world: " + e.getMessage());
            future.complete(new ArrayList<>());
        }
    }
    
    private void runWorldPlayerSearch(World world, CompletableFuture<List<Player>> future) {
        try {
            List<Player> players = new ArrayList<>(world.getPlayers());
            future.complete(players);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to search players in world: " + e.getMessage());
            future.complete(new ArrayList<>());
        }
    }
    
    private TaskScheduler.TaskWrapper runEntityTask(Entity entity, Runnable operation) {
        try {
            if (entity.isValid()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                return scheduler.runEntity(entity, operation);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run task on entity: " + e.getMessage());
        }
        return null;
    }
    
    private TaskScheduler.TaskWrapper runPlayerTask(Player player, Runnable operation) {
        try {
            if (player.isOnline()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                return scheduler.runPlayer(player, operation);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run task on player: " + e.getMessage());
        }
        return null;
    }
}