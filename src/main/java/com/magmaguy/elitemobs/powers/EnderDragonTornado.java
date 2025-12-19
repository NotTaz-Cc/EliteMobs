package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import me.MinhTaz.FoliaLib.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonTornado extends CombatEnterScanPower {

    private Location tornadoEye = null;
    private Vector tornadoSpeed = null;

    public EnderDragonTornado() {
        super(PowersConfig.getPower("ender_dragon_tornado.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        TaskScheduler.TaskWrapper taskWrapper = scheduler.runTimerAsync(() -> {
            if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                return;
            }

            if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                EnderDragon.Phase phase = ((EnderDragon) eliteEntity.getLivingEntity()).getPhase();
                if (!EnderDragonPhaseSimplifier.isLanded(phase)) return;
            }

            doPower(eliteEntity);

        }, 0, 20);
        super.taskReference.set(taskWrapper);
    }

    private void doPower(EliteEntity eliteEntity) {
        if (tornadoEye == null) {
            tornadoEye = eliteEntity.getLivingEntity().getLocation().clone();
            tornadoSpeed = new Vector(ThreadLocalRandom.current().nextDouble(-0.5, 0.5), 0, ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        }

        List<Location> tornadoLocations = new ArrayList<>();
        tornadoLocations.add(tornadoEye.clone());
        for (int i = 0; i < 10; i++) {
            tornadoLocations.add(tornadoEye.clone().add(0, i, 0));
        }

        for (Location location : tornadoLocations) {
            for (int i = 0; i < 10; i++) {
                Vector direction = new Vector(Math.cos(i), 0, Math.sin(i));
                location.getWorld().spawnParticle(Particle.PORTAL, location.add(direction), 1, 0, 0, 0, 1);
                location.subtract(direction);
            }
        }

        for (Entity entity : tornadoEye.getWorld().getNearbyEntities(tornadoEye, 4, 10, 4)) {
            if (entity instanceof LivingEntity && !entity.equals(eliteEntity.getLivingEntity())) {
                Vector direction = entity.getLocation().toVector().subtract(tornadoEye.toVector()).normalize();
                entity.setVelocity(direction.multiply(0.5));
            }
        }

        List<Block> blocks = new ArrayList<>();
        for (Location location : tornadoLocations) {
            for (int i = -1; i <= 1; i++) {
                Block block = location.getWorld().getBlockAt(location.getBlockX() + i, location.getBlockY() - 1, location.getBlockZ());
                blocks.add(block);
                block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ() + i);
                blocks.add(block);
            }
        }

        for (Block block : blocks) {
            if (!block.getType().isAir()) {
                Explosion.generateFakeExplosion(Arrays.asList(block), eliteEntity.getLivingEntity());
                block.getWorld().spawnParticle(Particle.LAVA, block.getLocation(), 3, 1, 1, 1);
            }
        }

        tornadoEye.add(tornadoSpeed);
        if (tornadoEye.distance(eliteEntity.getLivingEntity().getLocation()) > 20) {
            tornadoSpeed.multiply(-1);
        }
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {
        tornadoEye = null;
        tornadoSpeed = null;
    }

}