package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import me.MinhTaz.FoliaLib.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class EnderDragonDiscoFireballs extends CombatEnterScanPower {

    int randomTiltSeed;
    private ArrayList<Vector> relativeLocationOffsets;
    private ArrayList<Location> realLocations = new ArrayList<>();
    private AtomicInteger warningCounter = new AtomicInteger(0);
    private ArrayList<Fireball> fireballs = new ArrayList<>();

    public EnderDragonDiscoFireballs() {
        super(PowersConfig.getPower("ender_dragon_disco_fireballs.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        TaskScheduler.TaskWrapper taskWrapper = scheduler.runTimerAsync(() -> {
            if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                return;
            }

            if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                if (!EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteEntity.getLivingEntity()).getPhase()))
                    return;

            doPower(eliteEntity);
        }, 0, 10);
        super.taskReference.set(taskWrapper);
    }

    private void doPower(EliteEntity eliteEntity) {
        if (warningCounter.get() == 0) {
            relativeLocationOffsets = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-5, 5), 0, ThreadLocalRandom.current().nextDouble(-5, 5));
                relativeLocationOffsets.add(offset);
            }
        }

        if (warningCounter.get() < 40) {
            for (Vector vector : relativeLocationOffsets) {
                Location particleLocation = eliteEntity.getLivingEntity().getLocation().clone().add(vector);
                particleLocation.getWorld().spawnParticle(Particle.FLAME, particleLocation, 1, 0.5, 0.5, 0.5, 0);
            }
            warningCounter.incrementAndGet();
        } else {
            if (realLocations.isEmpty()) {
                for (Vector vector : relativeLocationOffsets) {
                    realLocations.add(eliteEntity.getLivingEntity().getLocation().clone().add(vector));
                }
            }

            for (Location location : realLocations) {
                for (int i = 0; i < 3; i++) {
                    Vector shootDirection = new Vector(ThreadLocalRandom.current().nextDouble(-1, 1), ThreadLocalRandom.current().nextDouble(0, 2), ThreadLocalRandom.current().nextDouble(-1, 1)).normalize();
                    Fireball fireball = (Fireball) EliteProjectile.create(EntityType.FIREBALL, eliteEntity.getLivingEntity(), null, shootDirection, true);
                    // fireball.setLocation(location); // Not available in this API version
                    EntityTracker.registerProjectileEntity(fireball);
                    fireball.setVelocity(shootDirection.multiply(2));
                    fireball.setYield(3F);
                    fireballs.add(fireball);
                }
            }

            warningCounter.set(0);
            realLocations.clear();
            relativeLocationOffsets.clear();
        }
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {
        warningCounter.set(0);
        realLocations.clear();
        relativeLocationOffsets.clear();
        fireballs.clear();
    }

}