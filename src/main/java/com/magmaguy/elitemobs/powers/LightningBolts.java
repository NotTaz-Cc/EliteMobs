package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import me.MinhTaz.FoliaLib.TaskScheduler;
import me.MinhTaz.FoliaLib.TaskScheduler.TaskWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class LightningBolts extends BossPower implements Listener {

    public LightningBolts() {
        super(PowersConfig.getPower("lightning_bolts.yml"));
    }

    private static void setLightiningPaths(EliteEntity eliteEntity) {
        if (eliteEntity.getLivingEntity() == null) return;
        eliteEntity.getLivingEntity().setAI(false);
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(20, 20, 20)) {
            if (entity.getType().equals(EntityType.PLAYER)) {
                Location playerLocationClone = entity.getLocation().clone();
                Location powerLocation = eliteEntity.getLivingEntity().getLocation().clone();
                Vector powerDirection = playerLocationClone.clone().subtract(powerLocation).toVector().normalize();
                int counter = 0;
                while (playerLocationClone.distance(powerLocation) > 0.55) {
                    counter++;
                    powerLocation.add(powerDirection);
                    lightningTask(powerLocation.clone(), counter);
                }
            }
        }
        new TaskScheduler(MetadataHandler.PLUGIN).runDelayedAsync(() -> {
            if (eliteEntity != null && eliteEntity.getLivingEntity() != null)
                eliteEntity.getLivingEntity().setAI(true);
        }, 4L * 20);
        }

    public static void lightningTask(Location location, int counterDelay) {
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<TaskWrapper> taskRef = new AtomicReference<>();
        
        Runnable timerTask = () -> {
            counter.incrementAndGet();
            if (counter.get() > 20 * 2) {
                LightningSpawnBypass.bypass();
                location.getWorld().strikeLightning(location);
                if (taskRef.get() != null) taskRef.get().cancel();
                return;
            }
            location.getWorld().spawnParticle(Particle.CRIT, location, 10, 0.5, 1.5, 0.5, 0.3);
        };
        taskRef.set(scheduler.runTimerAsync(timerTask, counterDelay * 5L, 1));

    }

    @EventHandler
    public void onDamagedEvent(EliteMobDamagedByPlayerEvent event) {
        LightningBolts lightningBolts = (LightningBolts) event.getEliteMobEntity().getPower(this);
        if (lightningBolts == null) return;
        if (lightningBolts.isInCooldown(event.getEliteMobEntity())) return;

        lightningBolts.doCooldownTicks(event.getEliteMobEntity());

        lightningBolts.setFiring(true);
        setLightiningPaths(event.getEliteMobEntity());
    }

}
