package me.MinhTaz.FoliaLib;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ChunkManager - Folia compatible chunk operations
 * Developer: MinhTaz
 * This class provides Folia-compatible chunk management with fallback support
 */
public class ChunkManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public ChunkManager(Plugin plugin) {
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
     * Load chunk at coordinates with retry logic
     */
    public CompletableFuture<Chunk> loadChunkWithRetry(World world, int x, int z, int maxRetries) {
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        AtomicInteger retryCount = new AtomicInteger(0);
        
        loadChunkInternal(world, x, z, future, retryCount, maxRetries);
        return future;
    }
    
    /**
     * Get loaded chunks in world
     */
    public CompletableFuture<List<Chunk>> getLoadedChunks(World world) {
        CompletableFuture<List<Chunk>> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldChunkOperation(world, () -> {
                try {
                    List<Chunk> chunks = new ArrayList<>();
                    Chunk[] loadedChunks = world.getLoadedChunks();
                    for (Chunk chunk : loadedChunks) {
                        chunks.add(chunk);
                    }
                    future.complete(chunks);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get loaded chunks on Folia: " + e.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } else {
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
     * Check if chunk is loaded
     */
    public CompletableFuture<Boolean> isChunkLoaded(World world, int x, int z) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldChunkOperation(world, () -> {
                try {
                    boolean isLoaded = world.isChunkLoaded(x, z);
                    future.complete(isLoaded);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check chunk load status on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } else {
            boolean isLoaded = world.isChunkLoaded(x, z);
            future.complete(isLoaded);
        }
        
        return future;
    }
    
    /**
     * Unload chunk safely
     */
    public CompletableFuture<Boolean> unloadChunk(Chunk chunk, boolean save) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldChunkOperation(chunk.getWorld(), () -> {
                try {
                    boolean result = chunk.unload(save);
                    future.complete(result);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to unload chunk on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } else {
            boolean result = chunk.unload(save);
            future.complete(result);
        }
        
        return future;
    }
    
    /**
     * Force chunk generation if not exists
     */
    public CompletableFuture<Chunk> ensureChunkGenerated(World world, int x, int z) {
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldChunkOperation(world, () -> {
                try {
                    if (!world.isChunkGenerated(x, z)) {
                        world.getChunkAt(x, z); // This will generate the chunk
                    }
                    Chunk chunk = world.getChunkAt(x, z);
                    future.complete(chunk);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to ensure chunk generation on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } else {
            if (!world.isChunkGenerated(x, z)) {
                world.getChunkAt(x, z); // This will generate the chunk
            }
            Chunk chunk = world.getChunkAt(x, z);
            future.complete(chunk);
        }
        
        return future;
    }
    
    /**
     * Get all chunks in area around location
     */
    public CompletableFuture<List<Chunk>> getChunksInRadius(Location center, int radius) {
        CompletableFuture<List<Chunk>> future = new CompletableFuture<>();
        World world = center.getWorld();
        
        if (world == null) {
            future.complete(new ArrayList<>());
            return future;
        }
        
        if (isFolia) {
            runWorldChunkOperation(world, () -> {
                try {
                    List<Chunk> chunks = new ArrayList<>();
                    int centerX = center.getChunk().getX();
                    int centerZ = center.getChunk().getZ();
                    
                    for (int x = centerX - radius; x <= centerX + radius; x++) {
                        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                            if (world.isChunkLoaded(x, z)) {
                                chunks.add(world.getChunkAt(x, z));
                            }
                        }
                    }
                    future.complete(chunks);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get chunks in radius on Folia: " + e.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } else {
            List<Chunk> chunks = new ArrayList<>();
            int centerX = center.getChunk().getX();
            int centerZ = center.getChunk().getZ();
            
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    if (world.isChunkLoaded(x, z)) {
                        chunks.add(world.getChunkAt(x, z));
                    }
                }
            }
            future.complete(chunks);
        }
        
        return future;
    }
    
    /**
     * Preload chunks in area for performance
     */
    public CompletableFuture<Void> preloadChunks(World world, int centerX, int centerZ, int radius) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isFolia) {
            runWorldChunkOperation(world, () -> {
                try {
                    for (int x = centerX - radius; x <= centerX + radius; x++) {
                        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                            world.getChunkAt(x, z);
                        }
                    }
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to preload chunks on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } else {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    world.getChunkAt(x, z);
                }
            }
            future.complete(null);
        }
        
        return future;
    }
    
    // Private helper methods
    
    private void loadChunkInternal(World world, int x, int z, CompletableFuture<Chunk> future, 
                                   AtomicInteger retryCount, int maxRetries) {
        runWorldChunkOperation(world, () -> {
            try {
                Chunk chunk = world.getChunkAt(x, z);
                future.complete(chunk);
            } catch (Exception e) {
                if (retryCount.incrementAndGet() < maxRetries) {
                    // Retry after a short delay
                    new TaskScheduler(plugin).runDelayedAsync(() -> {
                        loadChunkInternal(world, x, z, future, retryCount, maxRetries);
                    }, 20); // 1 second delay
                } else {
                    plugin.getLogger().warning("Failed to load chunk after " + maxRetries + " retries: " + e.getMessage());
                    future.complete(null);
                }
            }
        });
    }
    
    private void runWorldChunkOperation(World world, Runnable operation) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, operation);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run chunk operation on world: " + e.getMessage());
            // Fallback to running directly
            operation.run();
        }
    }
}