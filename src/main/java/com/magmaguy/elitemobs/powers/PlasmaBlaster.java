package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import me.MinhTaz.FoliaLib.TaskScheduler;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;


public class PlasmaBlaster extends CombatEnterScanPower {

    public PlasmaBlaster() {
        super(PowersConfig.getPower("plasma_blaster.yml"));
    }


    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        TaskScheduler.TaskWrapper taskWrapper = scheduler.runTimerAsync(() -> {
            if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                return;
            }
            doPower(eliteEntity);
        }, 0, 20 * 4);
        super.taskReference.set(taskWrapper);
    }

    private void doPower(EliteEntity eliteEntity) {
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(30, 30, 30))
            if (entity.getType().equals(EntityType.PLAYER)) {
                if (((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) continue;
                Vector shotVector = entity.getLocation().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize().multiply(0.5);
                createProjectile(shotVector, eliteEntity.getLocation(), eliteEntity, (Player) entity);
                break;
            }
    }

    private void createProjectile(Vector shotVector, Location sourceLocation, EliteEntity sourceEntity, Player player) {
        Vector targetLocation = player.getEyeLocation().toVector();
        Vector direction = targetLocation.subtract(sourceLocation.toVector()).normalize();
        Location projectileLocation = sourceLocation.clone();
        projectileLocation.setDirection(direction);

        Color color = Color.fromRGB(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255));
        for (int i = 0; i < 40; i++) {
            projectileLocation.add(direction);
            sourceLocation.getWorld().spawnParticle(Particle.FLAME, projectileLocation, 1, 0.1, 0.1, 0.1);
            if (projectileLocation.distance(player.getLocation()) < 2) {
                player.damage(2, sourceEntity.getLivingEntity());
                break;
            }
        }
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {
        // No cleanup needed
    }

}