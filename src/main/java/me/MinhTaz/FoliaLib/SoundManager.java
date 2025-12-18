package me.MinhTaz.FoliaLib;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SoundManager - Folia compatible sound operations
 * Developer: MinhTaz
 * This class provides Folia-compatible sound management with fallback support
 */
public class SoundManager {
    
    private final Plugin plugin;
    private final boolean isFolia;
    
    public SoundManager(Plugin plugin) {
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
     * Play sound at location for all players in world
     */
    public void playSoundAtLocation(Location location, Sound sound, float volume, float pitch) {
        if (isFolia) {
            runFoliaSoundPlay(location, sound, volume, pitch);
        } else {
            location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
        }
    }
    
    /**
     * Play sound for specific entity
     */
    public void playSoundForEntity(Entity entity, Sound sound, float volume, float pitch) {
        if (isFolia) {
            runFoliaEntitySound(entity, sound, volume, pitch);
        } else {
            entity.getWorld().playSound(entity.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
        }
    }
    
    /**
     * Play sound for specific player
     */
    public void playSoundForPlayer(Player player, Sound sound, float volume, float pitch) {
        if (isFolia) {
            runFoliaPlayerSound(player, sound, volume, pitch);
        } else {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
            }
        }
    }
    
    /**
     * Play sound for all players in world
     */
    public void playSoundForWorld(World world, Sound sound, Location location, float volume, float pitch) {
        if (isFolia) {
            runFoliaWorldSound(world, sound, location, volume, pitch);
        } else {
            world.playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
        }
    }
    
    /**
     * Play custom sound with custom data
     */
    public CompletableFuture<Void> playCustomSound(Location location, String soundName, float volume, float pitch) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaCustomSound(location, soundName, volume, pitch, future);
        } else {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
                future.complete(null);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name: " + soundName);
                future.complete(null);
            }
        }
        
        return future;
    }
    
    /**
     * Stop all sounds for player
     */
    public void stopAllSounds(Player player) {
        if (isFolia) {
            runFoliaStopAllSounds(player);
        } else {
            if (player.isOnline()) {
                player.stopSound(SoundCategory.PLAYERS);
            }
        }
    }
    
    /**
     * Stop specific sound for player
     */
    public void stopSound(Player player, Sound sound) {
        if (isFolia) {
            runFoliaStopSpecificSound(player, sound);
        } else {
            if (player.isOnline()) {
                player.stopSound(sound, SoundCategory.PLAYERS);
            }
        }
    }
    
    /**
     * Play multiple sounds in sequence
     */
    public CompletableFuture<Void> playSoundSequence(List<SoundSequence> soundSequence) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        playSoundSequenceRecursive(soundSequence, 0, future);
        return future;
    }
    
    /**
     * Get available sounds list
     */
    public CompletableFuture<List<String>> getAvailableSounds() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        
        if (isFolia) {
            runFoliaGetSounds(future);
        } else {
            List<String> sounds = new ArrayList<>();
            for (Sound sound : Sound.values()) {
                sounds.add(sound.name());
            }
            future.complete(sounds);
        }
        
        return future;
    }
    
    // Folia implementations
    
    private void runFoliaSoundPlay(Location location, Sound sound, float volume, float pitch) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(location.getWorld(), () -> {
                try {
                    location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to play sound on Folia: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule sound play on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaEntitySound(Entity entity, Sound sound, float volume, float pitch) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runEntity(entity, () -> {
                try {
                    entity.getWorld().playSound(entity.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to play entity sound on Folia: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule entity sound on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaPlayerSound(Player player, Sound sound, float volume, float pitch) {
        try {
            if (player.isOnline()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                scheduler.runPlayer(player, () -> {
                    try {
                        player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to play player sound on Folia: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule player sound on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaWorldSound(World world, Sound sound, Location location, float volume, float pitch) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(world, () -> {
                try {
                    world.playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to play world sound on Folia: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule world sound on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaCustomSound(Location location, String soundName, float volume, float pitch, CompletableFuture<Void> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runWorld(location.getWorld(), () -> {
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
                    future.complete(null);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name: " + soundName);
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to play custom sound on Folia: " + e.getMessage());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule custom sound on Folia: " + e.getMessage());
            future.complete(null);
        }
    }
    
    private void runFoliaStopAllSounds(Player player) {
        try {
            if (player.isOnline()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                scheduler.runPlayer(player, () -> {
                    try {
                        player.stopSound(SoundCategory.PLAYERS);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to stop all sounds on Folia: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule stop all sounds on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaStopSpecificSound(Player player, Sound sound) {
        try {
            if (player.isOnline()) {
                TaskScheduler scheduler = new TaskScheduler(plugin);
                scheduler.runPlayer(player, () -> {
                    try {
                        player.stopSound(sound, SoundCategory.PLAYERS);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to stop specific sound on Folia: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule stop specific sound on Folia: " + e.getMessage());
        }
    }
    
    private void runFoliaGetSounds(CompletableFuture<List<String>> future) {
        try {
            TaskScheduler scheduler = new TaskScheduler(plugin);
            scheduler.runAsync(() -> {
                try {
                    List<String> sounds = new ArrayList<>();
                    for (Sound sound : Sound.values()) {
                        sounds.add(sound.name());
                    }
                    future.complete(sounds);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to get sounds on Folia: " + e.getMessage());
                    future.complete(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to schedule get sounds on Folia: " + e.getMessage());
            future.complete(new ArrayList<>());
        }
    }
    
    // Private helper methods
    
    private void playSoundSequenceRecursive(List<SoundSequence> sequence, int index, CompletableFuture<Void> future) {
        if (index >= sequence.size()) {
            future.complete(null);
            return;
        }
        
        SoundSequence soundSeq = sequence.get(index);
        playSoundAtLocation(soundSeq.getLocation(), soundSeq.getSound(), soundSeq.getVolume(), soundSeq.getPitch());
        
        // Schedule next sound after delay
        new TaskScheduler(plugin).runDelayedAsync(() -> {
            playSoundSequenceRecursive(sequence, index + 1, future);
        }, soundSeq.getDelay());
    }
    
    /**
     * SoundSequence - Represents a sound to be played in sequence
     */
    public static class SoundSequence {
        private final Location location;
        private final Sound sound;
        private final float volume;
        private final float pitch;
        private final long delay;
        
        public SoundSequence(Location location, Sound sound, float volume, float pitch, long delay) {
            this.location = location;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
        }
        
        public Location getLocation() { return location; }
        public Sound getSound() { return sound; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
        public long getDelay() { return delay; }
    }
}