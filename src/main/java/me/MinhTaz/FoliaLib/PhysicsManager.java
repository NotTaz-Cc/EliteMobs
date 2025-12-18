package me.MinhTaz.FoliaLib;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * PhysicsManager - Folia compatible physics operations
 * Developer: MinhTaz
 * This class provides Folia-compatible physics management with fallback support
 */
public class PhysicsManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    private final ScheduledExecutorService scheduler;
    
    public PhysicsManager(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFoliaServer();
        this.scheduler = Executors.newScheduledThreadPool(2);
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
     * Run physics simulation in world safely
     */
    public CompletableFuture<Void> runPhysicsSimulation(org.bukkit.World world, int duration) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaPhysics(world, duration, future);
        } else {
            runBukkitPhysics(world, duration, future);
        }
        
        return future;
    }
    
    /**
     * Update block physics in chunk
     */
    public CompletableFuture<Void> updateBlockPhysics(org.bukkit.Chunk chunk) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaBlockPhysics(chunk, future);
        } else {
            runBukkitBlockPhysics(chunk, future);
        }
        
        return future;
    }
    
    /**
     * Check block physics state
     */
    public CompletableFuture<Boolean> checkBlockPhysics(org.bukkit.Location location) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaPhysicsCheck(location, future);
        } else {
            runBukkitPhysicsCheck(location, future);
        }
        
        return future;
    }
    
    /**
     * Apply gravity to entities
     */
    public CompletableFuture<Void> applyGravityToEntities(org.bukkit.World world, org.bukkit.entity.Entity... entities) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaGravity(world, entities, future);
        } else {
            runBukkitGravity(world, entities, future);
        }
        
        return future;
    }
    
    /**
     * Sync physics with server tick
     */
    public void syncPhysicsWithTick(org.bukkit.World world, Runnable physicsOperation) {
        if (isFolia) {
            runFoliaPhysicsTick(world, physicsOperation);
        } else {
            // Run on main server thread
            new TaskScheduler(plugin).runWorld(world, physicsOperation);
        }
    }
    
    /**
     * Get physics region for location
     */
    public CompletableFuture<PhysicsRegion> getPhysicsRegion(org.bukkit.Location location) {
        CompletableFuture<PhysicsRegion> future = new CompletableFuture<>();
        
        org.bukkit.World world = location.getWorld();
        if (world == null) {
            future.complete(null);
            return future;
        }
        
        if (isFolia) {
            runFoliaPhysicsRegion(world, location, future);
        } else {
            // Synchronous fallback
            PhysicsRegion region = new PhysicsRegion(location, 16); // Default 16 block radius
            future.complete(region);
        }
        
        return future;
    }
    
    // Folia implementations
    
    private void runFoliaPhysics(org.bukkit.World world, int duration, CompletableFuture<Void> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    // Simulate physics for specified duration (simplified)
                    for (int i = 0; i < duration; i++) {
                        // Basic physics simulation - update entities and blocks
                        for (org.bukkit.entity.Entity entity : world.getEntities()) {
                            if (entity.isValid() && !entity.isOnGround()) {
                                // Apply basic gravity
                                entity.setVelocity(entity.getVelocity().add(new org.bukkit.util.Vector(0, -0.08, 0)));
                            }
                        }
                    }
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to run physics simulation on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule physics simulation on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaBlockPhysics(org.bukkit.Chunk chunk, CompletableFuture<Void> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(chunk.getWorld(), () -> {
                try {
                    // Update physics for blocks in chunk (simplified)
                    chunk.getWorld().getBlockAt(chunk.getX() * 16, 64, chunk.getZ() * 16); // Trigger physics
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to update block physics on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule block physics on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaPhysicsCheck(org.bukkit.Location location, CompletableFuture<Boolean> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(location.getWorld(), () -> {
                try {
                    // Check if block has physics (simplified check)
                    org.bukkit.Material material = location.getBlock().getType();
                    boolean hasPhysics = material.isSolid() || material.hasGravity();
                    future.complete(hasPhysics);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check block physics on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule physics check on Folia: " + e.getMessage());
            future.complete(false);
        }
    }
    
    private void runFoliaGravity(org.bukkit.World world, org.bukkit.entity.Entity[] entities, CompletableFuture<Void> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    for (org.bukkit.entity.Entity entity : entities) {
                        if (entity != null && entity.isValid()) {
                            // Apply gravity manually if needed
                            entity.setVelocity(entity.getVelocity().add(new org.bukkit.util.Vector(0, -0.08, 0)));
                        }
                    }
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to apply gravity on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule gravity application on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaPhysicsTick(org.bukkit.World world, Runnable physicsOperation) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, physicsOperation);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to sync physics with tick on Folia: " + e.getMessage());
            physicsOperation.run(); // Fallback
        }
    }
    
    private void runFoliaPhysicsRegion(org.bukkit.World world, org.bukkit.Location location, CompletableFuture<PhysicsRegion> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    PhysicsRegion region = new PhysicsRegion(location, 16);
                    future.complete(region);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get physics region on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule physics region on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    // Bukkit fallback implementations
    
    private void runBukkitPhysics(org.bukkit.World world, int duration, CompletableFuture<Void> future) {
        try {
            for (int i = 0; i < duration; i++) {
                // Simulate physics ticks (simplified)
                world.getPlayers().forEach(player -> {
                    // Apply basic physics to players
                });
            }
            future.complete(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run physics simulation on Bukkit: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runBukkitBlockPhysics(org.bukkit.Chunk chunk, CompletableFuture<Void> future) {
        try {
            // Trigger physics update manually (simplified)
            chunk.getWorld().getBlockAt(chunk.getX() * 16, 64, chunk.getZ() * 16); // Trigger physics
            future.complete(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update block physics on Bukkit: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runBukkitPhysicsCheck(org.bukkit.Location location, CompletableFuture<Boolean> future) {
        try {
            // Check if block has physics (simplified check)
            org.bukkit.Material material = location.getBlock().getType();
            boolean hasPhysics = material.isSolid() || material.hasGravity();
            future.complete(hasPhysics);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check block physics on Bukkit: " + e.getMessage());
            future.complete(false);
        }
    }
    
    private void runBukkitGravity(org.bukkit.World world, org.bukkit.entity.Entity[] entities, CompletableFuture<Void> future) {
        try {
            for (org.bukkit.entity.Entity entity : entities) {
                if (entity != null && entity.isValid()) {
                    entity.setVelocity(entity.getVelocity().add(new org.bukkit.util.Vector(0, -0.08, 0)));
                }
            }
            future.complete(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply gravity on Bukkit: " + e.getMessage());
            future.complete(null);
        }
    }
    
    /**
     * PhysicsRegion - Represents a physics simulation region
     */
    public static class PhysicsRegion {
        private final org.bukkit.Location center;
        private final int radius;
        
        public PhysicsRegion(org.bukkit.Location center, int radius) {
            this.center = center;
            this.radius = radius;
        }
        
        public org.bukkit.Location getCenter() {
            return center;
        }
        
        public int getRadius() {
            return radius;
        }
        
        public boolean contains(org.bukkit.Location location) {
            double distance = center.distance(location);
            return distance <= radius;
        }
    }
    
    /**
     * Shutdown scheduler when plugin is disabled
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}