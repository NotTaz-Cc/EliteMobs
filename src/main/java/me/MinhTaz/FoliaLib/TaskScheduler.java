package me.MinhTaz.FoliaLib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * TaskScheduler - Folia API compatible scheduler
 * Developer: MinhTaz
 * This class provides Folia-compatible task scheduling with fallback support
 */
public class TaskScheduler {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public TaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFoliaServer();
    }
    
    /**
     * Check if running on Folia server
     */
    private boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Run task asynchronously on global region
     */
    public TaskWrapper runAsync(Runnable task) {
        if (isFolia) {
            return runFoliaAsync(task);
        } else {
            return runBukkitAsync(task);
        }
    }
    
    /**
     * Run task on specific world region
     */
    public TaskWrapper runWorld(org.bukkit.World world, Runnable task) {
        if (isFolia) {
            return runFoliaWorld(world, task);
        } else {
            // Fallback to async for non-Folia
            return runBukkitAsync(task);
        }
    }
    
    /**
     * Run task on specific entity region
     */
    public TaskWrapper runEntity(org.bukkit.entity.Entity entity, Runnable task) {
        if (isFolia) {
            return runFoliaEntity(entity, task);
        } else {
            // Fallback to async for non-Folia
            return runBukkitAsync(task);
        }
    }
    
    /**
     * Run task on specific player region
     */
    public TaskWrapper runPlayer(org.bukkit.entity.Player player, Runnable task) {
        if (isFolia) {
            return runFoliaPlayer(player, task);
        } else {
            // Fallback to async for non-Folia
            return runBukkitAsync(task);
        }
    }
    
    /**
     * Run delayed task on global region
     */
    public TaskWrapper runDelayedAsync(Runnable task, long delay) {
        if (isFolia) {
            return runFoliaDelayedAsync(task, delay);
        } else {
            return runBukkitDelayedAsync(task, delay);
        }
    }
    
    /**
     * Run timer task on global region
     */
    public TaskWrapper runTimerAsync(Runnable task, long delay, long period) {
        if (isFolia) {
            return runFoliaTimerAsync(task, delay, period);
        } else {
            return runBukkitTimerAsync(task, delay, period);
        }
    }
    
    // Folia implementations
    private TaskWrapper runFoliaAsync(Runnable task) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("run", Plugin.class, Runnable.class)
                    .invoke(scheduler, plugin, task);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia async task, falling back to Bukkit: " + e.getMessage());
            return runBukkitAsync(task);
        }
    }
    
    private TaskWrapper runFoliaWorld(org.bukkit.World world, Runnable task) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("run", org.bukkit.World.class, Plugin.class, Runnable.class)
                    .invoke(scheduler, world, plugin, task);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia world task, falling back to async: " + e.getMessage());
            return runBukkitAsync(task);
        }
    }
    
    private TaskWrapper runFoliaEntity(org.bukkit.entity.Entity entity, Runnable task) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("runEntity", org.bukkit.entity.Entity.class, Plugin.class, Runnable.class)
                    .invoke(scheduler, entity, plugin, task);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia entity task, falling back to async: " + e.getMessage());
            return runBukkitAsync(task);
        }
    }
    
    private TaskWrapper runFoliaPlayer(org.bukkit.entity.Player player, Runnable task) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("runPlayer", org.bukkit.entity.Player.class, Plugin.class, Runnable.class)
                    .invoke(scheduler, player, plugin, task);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia player task, falling back to async: " + e.getMessage());
            return runBukkitAsync(task);
        }
    }
    
    private TaskWrapper runFoliaDelayedAsync(Runnable task, long delay) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("runDelayed", Plugin.class, Runnable.class, long.class)
                    .invoke(scheduler, plugin, task, delay);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia delayed task, falling back to Bukkit: " + e.getMessage());
            return runBukkitDelayedAsync(task, delay);
        }
    }
    
    private TaskWrapper runFoliaTimerAsync(Runnable task, long delay, long period) {
        try {
            Object scheduler = getFoliaScheduler();
            Object foliaTask = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class)
                    .invoke(scheduler, plugin, task, delay, period);
            return new FoliaTaskWrapper(foliaTask);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run Folia timer task, falling back to Bukkit: " + e.getMessage());
            return runBukkitTimerAsync(task, delay, period);
        }
    }
    
    // Bukkit fallback implementations
    private TaskWrapper runBukkitAsync(Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }
    
    private TaskWrapper runBukkitDelayedAsync(Runnable task, long delay) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
    }
    
    private TaskWrapper runBukkitTimerAsync(Runnable task, long delay, long period) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
    }
    
    private Object getFoliaScheduler() {
        try {
            Class<?> clazz = Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaScheduler");
            java.lang.reflect.Method method = clazz.getMethod("getGlobalRegionScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Folia scheduler", e);
        }
    }
    
    /**
     * Generic task wrapper interface
     */
    public interface TaskWrapper {
        void cancel();
        boolean isCancelled();
        boolean isRunning();
    }
    
    /**
     * Folia task wrapper
     */
    private static class FoliaTaskWrapper implements TaskWrapper {
        private final Object foliaTask;
        
        public FoliaTaskWrapper(Object foliaTask) {
            this.foliaTask = foliaTask;
        }
        
        @Override
        public void cancel() {
            try {
                foliaTask.getClass().getMethod("cancel").invoke(foliaTask);
            } catch (Exception e) {
                // Ignore
            }
        }
        
        @Override
        public boolean isCancelled() {
            try {
                return (Boolean) foliaTask.getClass().getMethod("isCancelled").invoke(foliaTask);
            } catch (Exception e) {
                return true;
            }
        }
        
        @Override
        public boolean isRunning() {
            return !isCancelled();
        }
    }
    
    /**
     * Bukkit task wrapper
     */
    private static class BukkitTaskWrapper implements TaskWrapper {
        private final BukkitTask task;
        
        public BukkitTaskWrapper(BukkitTask task) {
            this.task = task;
        }
        
        @Override
        public void cancel() {
            task.cancel();
        }
        
        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
        
        @Override
        public boolean isRunning() {
            return !task.isCancelled();
        }
    }
}