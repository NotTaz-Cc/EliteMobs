package me.MinhTaz.FoliaLib;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * WorldManager - Folia compatible world operations
 * Developer: MinhTaz
 * This class provides Folia-compatible world management with fallback support
 */
public class WorldManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public WorldManager(Plugin plugin) {
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
     * Load chunk asynchronously on Folia
     */
    public CompletableFuture<Chunk> loadChunkAsync(World world, int x, int z) {
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaChunkLoad(world, x, z, future);
        } else {
            // Synchronous fallback
            Chunk chunk = world.getChunkAt(x, z);
            future.complete(chunk);
        }
        
        return future;
    }
    
    /**
     * Unload chunk safely on Folia
     */
    public CompletableFuture<Boolean> unloadChunkAsync(Chunk chunk) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaChunkUnload(chunk, future);
        } else {
            // Synchronous fallback
            boolean result = chunk.unload();
            future.complete(result);
        }
        
        return future;
    }
    
    /**
     * Get block at location safely on Folia
     */
    public CompletableFuture<Block> getBlockAtAsync(Location location) {
        CompletableFuture<Block> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaBlockGet(location, future);
        } else {
            // Synchronous fallback
            Block block = location.getBlock();
            future.complete(block);
        }
        
        return future;
    }
    
    /**
     * Set block at location safely on Folia
     */
    public CompletableFuture<Boolean> setBlockAsync(Location location, org.bukkit.Material material) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaBlockSet(location, material, future);
        } else {
            // Synchronous fallback
            Block block = location.getBlock();
            block.setType(material);
            future.complete(true);
        }
        
        return future;
    }
    
    /**
     * Get all chunks in world
     */
    public CompletableFuture<List<Chunk>> getAllChunksAsync(World world) {
        CompletableFuture<List<Chunk>> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaGetAllChunks(world, future);
        } else {
            // Synchronous fallback
            List<Chunk> chunks = new ArrayList<>();
            Chunk[] loadedChunks = world.getLoadedChunks();
            for (Chunk chunk : loadedChunks) {
                chunks.add(chunk);
            }
            future.complete(chunks);
        }
        
        return future;
    }
    
    /**
     * Run operation on world safely
     */
    public TaskScheduler.TaskWrapper runWithWorld(World world, Runnable operation) {
        if (isFolia) {
            return runFoliaWorldTask(world, operation);
        } else {
            operation.run();
            return null;
        }
    }
    
    /**
     * Get world by name safely
     */
    public CompletableFuture<World> getWorldAsync(String worldName) {
        CompletableFuture<World> future = new CompletableFuture<>();
        
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            if (isFolia) {
                runFoliaWorldAccess(world, future);
            } else {
                future.complete(world);
            }
        } else {
            future.complete(null);
        }
        
        return future;
    }
    
    // Folia implementations
    
    private void runFoliaChunkLoad(World world, int x, int z, CompletableFuture<Chunk> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    Chunk chunk = world.getChunkAt(x, z);
                    future.complete(chunk);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load chunk on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run chunk load task on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaChunkUnload(Chunk chunk, CompletableFuture<Boolean> future) {
        try {
            World world = chunk.getWorld();
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    boolean result = chunk.unload();
                    future.complete(result);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to unload chunk on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run chunk unload task on Folia: " + e.getMessage());
            future.complete(false);
        }
    }
    
    private void runFoliaBlockGet(Location location, CompletableFuture<Block> future) {
        try {
            World world = location.getWorld();
            if (world == null) {
                future.complete(null);
                return;
            }
            
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    Block block = location.getBlock();
                    future.complete(block);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get block on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run block get task on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaBlockSet(Location location, org.bukkit.Material material, CompletableFuture<Boolean> future) {
        try {
            World world = location.getWorld();
            if (world == null) {
                future.complete(false);
                return;
            }
            
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    Block block = location.getBlock();
                    block.setType(material);
                    future.complete(true);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set block on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run block set task on Folia: " + e.getMessage());
            future.complete(false);
        }
    }
    
    private void runFoliaGetAllChunks(World world, CompletableFuture<List<Chunk>> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    List<Chunk> chunks = new ArrayList<>();
                    Chunk[] loadedChunks = world.getLoadedChunks();
                    for (Chunk chunk : loadedChunks) {
                        chunks.add(chunk);
                    }
                    future.complete(chunks);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get all chunks on Folia: " + e.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run get all chunks task on Folia: " + e.getMessage());
            future.complete(new ArrayList<>());
        }
    }
    
    private TaskScheduler.TaskWrapper runFoliaWorldTask(World world, Runnable operation) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            return scheduler.runWorld(world, operation);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run world task on Folia: " + e.getMessage());
            // Fallback to running directly
            operation.run();
            return null;
        }
    }
    
    private void runFoliaWorldAccess(World world, CompletableFuture<World> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                future.complete(world);
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to access world on Folia: " + e.getMessage());
            future.complete(world); // Return original world anyway
        }
    }
}