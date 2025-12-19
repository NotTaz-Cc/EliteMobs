package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class EnderDragonShockwave extends CombatEnterScanPower {

    private final int radius = 30;
    //this is structured like this because the relative block generator is moving out of this class
    private final ArrayList<PieBlock> realBlocks = new ArrayList<>();
    private ArrayList<PieBlock> pieBlocks = new ArrayList<>();
    private AtomicInteger warningPhaseCounter = new AtomicInteger(0);
    private AtomicInteger damagePhaseCounter = new AtomicInteger(0);

    public EnderDragonShockwave() {
        super(PowersConfig.getPower("ender_dragon_shockwave.yml"));
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
        if (warningPhaseCounter.get() == 0) {
            pieBlocks = new ArrayList<>();
            double numberOfBlocks = 360 / 10;
            for (int i = 0; i < numberOfBlocks; i++) {
                double angle = (i * 10) * Math.PI / 180;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                pieBlocks.add(new PieBlock(eliteEntity.getLivingEntity().getLocation().clone().add(x, 0, z), warningPhaseCounter.get()));
            }
        }

        if (warningPhaseCounter.get() < 40) {
            for (PieBlock pieBlock : pieBlocks) {
                pieBlock.location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, pieBlock.location, 1, 0.5, 0.5, 0.5, 0);
            }
            warningPhaseCounter.incrementAndGet();
        } else {
            if (damagePhaseCounter.get() == 0) {
                for (PieBlock pieBlock : pieBlocks) {
                    realBlocks.add(new PieBlock(pieBlock.location.clone(), damagePhaseCounter.get()));
                }
            }

            for (PieBlock realBlock : realBlocks) {
                if (realBlock.location.getBlock().getType().isSolid()) {
                    Explosion.generateFakeExplosion(Arrays.asList(realBlock.location.getBlock()), eliteEntity.getLivingEntity());
                }
                realBlock.location.getWorld().spawnParticle(Particle.LAVA, realBlock.location, 3, 1, 1, 1);
            }

            for (Entity nearbyEntity : eliteEntity.getLivingEntity().getNearbyEntities(radius, radius, radius)) {
                if (nearbyEntity instanceof LivingEntity && !nearbyEntity.equals(eliteEntity.getLivingEntity())) {
                    BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) nearbyEntity, 10);
                }
            }

            damagePhaseCounter.incrementAndGet();

            if (damagePhaseCounter.get() > 20) {
                warningPhaseCounter.set(0);
                damagePhaseCounter.set(0);
                realBlocks.clear();
            }
        }
    }

    private static class PieBlock {
        Location location;
        int counter;

        PieBlock(Location location, int counter) {
            this.location = location;
            this.counter = counter;
        }
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {
        warningPhaseCounter.set(0);
        damagePhaseCounter.set(0);
        realBlocks.clear();
        pieBlocks.clear();
    }

}