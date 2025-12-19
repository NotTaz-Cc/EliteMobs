package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import me.MinhTaz.FoliaLib.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EnderDragonEmpoweredLightning extends MajorPower {

    private boolean isActive = false;
    private AtomicReference<TaskScheduler.TaskWrapper> taskReference = new AtomicReference<>();

    public EnderDragonEmpoweredLightning() {
        super(PowersConfig.getPower("ender_dragon_empowered_lightning.yml"));
    }

    public static void lightningTask(Location location) {
        AtomicInteger counter = new AtomicInteger(0);
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        scheduler.runTimerAsync(() -> {
            if (counter.incrementAndGet() > 20 * 3) {
                LightningSpawnBypass.bypass();
                location.getWorld().strikeLightning(location);
                Fireball fireball = (Fireball) location.getWorld().spawnEntity(location, EntityType.FIREBALL);
                EntityTracker.registerProjectileEntity(fireball);
                fireball.setDirection(new Vector(0, -3, 0));
                fireball.setVelocity(new Vector(0, -3, 0));
                fireball.setYield(5F);
                EliteProjectile.signExplosiveWithPower(fireball, "ender_dragon_empowered_lightning.yml");
                return;
            }
            location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 0.5, 1.5, 0.5, 0.3);
        }, 0, 1);
    }

    private void activate(EliteEntity eliteEntity) {
        if (isActive)
            return;

        isActive = true;
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        TaskScheduler.TaskWrapper taskWrapper = scheduler.runTimerAsync(() -> {
            if (!eliteEntity.isValid()) {
                return;
            }
            if (isInCooldown(eliteEntity)) return;
            fireLightning(eliteEntity);
        }, 0, 20);
        taskReference.set(taskWrapper);
    }

    private void deactivate() {
        isActive = false;
        TaskScheduler.TaskWrapper taskWrapper = taskReference.getAndSet(null);
        if (taskWrapper != null) {
            taskWrapper.cancel();
        }
    }

    public void fireLightning(EliteEntity eliteEntity) {

        doCooldown(eliteEntity);

        for (Entity entity : eliteEntity.getLivingEntity().getLocation().getWorld().getNearbyEntities(eliteEntity.getLivingEntity().getLocation(), 150, 150, 150))
            if (entity.getType().equals(EntityType.PLAYER))
                lightningTask(entity.getLocation().clone());

        for (int i = 0; i < 50; i++) {
            Location randomLocation = locationRandomizer(eliteEntity.getLivingEntity().getLocation(), 0);
            if (randomLocation == null) continue;

            TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
            scheduler.runDelayedAsync(() -> lightningTask(randomLocation), ThreadLocalRandom.current().nextInt(20 * 5));
        }
    }

    private Location locationRandomizer(Location location, int counter) {
        if (counter > 5) return null;
        Location randomLocation = location.clone().add(new Vector(
                ThreadLocalRandom.current().nextInt(-150, 150),
                0,
                ThreadLocalRandom.current().nextInt(-150, 150)));

        randomLocation.setY(randomLocation.getWorld().getHighestBlockAt(randomLocation).getY());
        if (randomLocation.getY() == -1)
            locationRandomizer(location, counter + 1);

        return randomLocation;
    }

    public static class EnderDragonEmpoweredLightningEvents implements Listener {
        @EventHandler
        public void onCombatEnter(EliteMobEnterCombatEvent event) {
            EnderDragonEmpoweredLightning empoweredLightning = (EnderDragonEmpoweredLightning) event.getEliteMobEntity().getPower("ender_dragon_empowered_lightning.yml");
            if (empoweredLightning == null) return;
            empoweredLightning.activate(event.getEliteMobEntity());
        }

        @EventHandler
        public void onCombatLeave(EliteMobExitCombatEvent event) {
            EnderDragonEmpoweredLightning empoweredLightning = (EnderDragonEmpoweredLightning) event.getEliteMobEntity().getPower("ender_dragon_empowered_lightning.yml");
            if (empoweredLightning == null) return;
            empoweredLightning.deactivate();
        }

    }

}
