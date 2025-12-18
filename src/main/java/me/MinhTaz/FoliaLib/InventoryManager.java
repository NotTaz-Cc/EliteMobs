package me.MinhTaz.FoliaLib;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * InventoryManager - Folia compatible inventory operations
 * Developer: MinhTaz
 * This class provides Folia-compatible inventory management with fallback support
 */
public class InventoryManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public InventoryManager(Plugin plugin) {
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
     * Open inventory for player safely on Folia
     */
    public void openInventory(Player player, Inventory inventory) {
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    player.openInventory(inventory);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to open inventory for player on Folia: " + e.getMessage());
                }
            });
        } else {
            player.openInventory(inventory);
        }
    }
    
    /**
     * Update inventory contents for player
     */
    public void updateInventory(Player player) {
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    player.updateInventory();
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to update inventory for player on Folia: " + e.getMessage());
                }
            });
        } else {
            player.updateInventory();
        }
    }
    
    /**
     * Create inventory with holder safely
     */
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        if (isFolia) {
            // Create inventory synchronously as Bukkit.createInventory works fine
            return org.bukkit.Bukkit.createInventory(holder, size, title);
        } else {
            return org.bukkit.Bukkit.createInventory(holder, size, title);
        }
    }
    
    /**
     * Get player's current inventory
     */
    public CompletableFuture<Inventory> getPlayerInventory(Player player) {
        CompletableFuture<Inventory> future = new CompletableFuture<>();
        
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    Inventory inventory = player.getInventory();
                    future.complete(inventory);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get player inventory on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } else {
            future.complete(player.getInventory());
        }
        
        return future;
    }
    
    /**
     * Add item to player's inventory
     */
    public CompletableFuture<Boolean> addItemToPlayer(Player player, org.bukkit.inventory.ItemStack... items) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    boolean success = player.getInventory().addItem(items).isEmpty();
                    future.complete(success);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to add items to player inventory on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } else {
            boolean success = player.getInventory().addItem(items).isEmpty();
            future.complete(success);
        }
        
        return future;
    }
    
    /**
     * Remove item from player's inventory
     */
    public CompletableFuture<Integer> removeItemFromPlayer(Player player, org.bukkit.inventory.ItemStack... items) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    int removed = 0;
                    for (org.bukkit.inventory.ItemStack item : items) {
                        removed += player.getInventory().removeItem(item).values().stream()
                                .mapToInt(org.bukkit.inventory.ItemStack::getAmount)
                                .sum();
                    }
                    future.complete(removed);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to remove items from player inventory on Folia: " + e.getMessage());
                    future.complete(0);
                }
            });
        } else {
            int removed = 0;
            for (org.bukkit.inventory.ItemStack item : items) {
                removed += player.getInventory().removeItem(item).values().stream()
                        .mapToInt(org.bukkit.inventory.ItemStack::getAmount)
                        .sum();
            }
            future.complete(removed);
        }
        
        return future;
    }
    
    /**
     * Check if player has item in inventory
     */
    public CompletableFuture<Boolean> hasItem(Player player, org.bukkit.inventory.ItemStack item) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (isFolia) {
            runPlayerInventoryTask(player, () -> {
                try {
                    boolean hasItem = player.getInventory().containsAtLeast(item, item.getAmount());
                    future.complete(hasItem);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check item in player inventory on Folia: " + e.getMessage());
                    future.complete(false);
                }
            });
        } else {
            boolean hasItem = player.getInventory().containsAtLeast(item, item.getAmount());
            future.complete(hasItem);
        }
        
        return future;
    }
    
    // Private helper methods
    
    private void runPlayerInventoryTask(Player player, Runnable operation) {
        try {
            if (player.isOnline()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                scheduler.runPlayer(player, operation);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to run inventory task on player: " + e.getMessage());
        }
    }
}