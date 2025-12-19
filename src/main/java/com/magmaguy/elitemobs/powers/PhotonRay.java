package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.magmacore.util.Logger;
import me.MinhTaz.FoliaLib.TaskScheduler;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class PhotonRay extends CombatEnterScanPower {

    private final int range = 60;
    private List<Location> playerLocations = new ArrayList<>(5);
    private AtomicInteger tickCounter = new AtomicInteger(0);

    public PhotonRay() {
        super(PowersConfig.getPower("photon_ray.yml"));
    }

    private void doDamage(Location location, EliteEntity eliteEntity) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
            if (entity instanceof LivingEntity) {
                if (eliteEntity.getLivingEntity().equals(entity)) continue;
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
            }

        tickCounter.incrementAndGet();

    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        TaskScheduler.TaskWrapper taskWrapper = scheduler.runTimerAsync(() -> {
            if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                return;
            }

            List<Player> nearbyPlayers = new ArrayList<>();
            for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(range, range, range))
                if (entity.getType().equals(EntityType.PLAYER))
                    if (((Player) entity).getGameMode().equals(GameMode.ADVENTURE) ||
                            ((Player) entity).getGameMode().equals(GameMode.SURVIVAL))
                        nearbyPlayers.add((Player) entity);

            if (nearbyPlayers.isEmpty()) return;

            Player targetPlayer = nearbyPlayers.get(ThreadLocalRandom.current().nextInt(nearbyPlayers.size()));

            // Build ray
            Location currentLocation = eliteEntity.getLivingEntity().getLocation().clone();
            Vector direction = targetPlayer.getLocation().toVector().subtract(currentLocation.toVector()).normalize();

            Color color = Color.fromRGB(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255));

            for (int i = 0; i < range; i++) {
                currentLocation.add(direction);
                eliteEntity.getLivingEntity().getWorld().spawnParticle(Particle.FLAME, currentLocation, 1, 0.1, 0.1, 0.1);
                doDamage(currentLocation, eliteEntity);
                if (currentLocation.distance(targetPlayer.getLocation()) < 2) break;
            }

            if (tickCounter.get() > 40) {
                deactivate(eliteEntity);
                System.out.println("Photon Ray power failed to find target and has been deactivated!");
            }

        }, 0, 20);
        super.taskReference.set(taskWrapper);
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {
        tickCounter.set(0);
        playerLocations.clear();
    }

}