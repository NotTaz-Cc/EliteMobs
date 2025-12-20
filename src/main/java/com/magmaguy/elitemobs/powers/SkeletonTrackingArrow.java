package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import me.MinhTaz.FoliaLib.TaskScheduler;
import me.MinhTaz.FoliaLib.TaskScheduler.TaskWrapper;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SkeletonTrackingArrow extends MajorPower implements Listener {

    public SkeletonTrackingArrow() {
        super(PowersConfig.getPower("skeleton_tracking_arrow.yml"));
    }

    private static void trackingArrowLoop(Player player, Arrow arrow) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<TaskWrapper> taskRef = new AtomicReference<>();
        
        TaskWrapper task = new TaskScheduler(MetadataHandler.PLUGIN).runTimerAsync(() -> {
            int currentCount = counter.get();
            if (player.isValid() && arrow.isValid() && arrow.getWorld().equals(player.getWorld())
                    && player.getLocation().distanceSquared(arrow.getLocation()) < 900 && !arrow.isOnGround()) {
                if (currentCount % 10 == 0)
                    arrow.setVelocity(arrow.getVelocity().add(arrowAdjustmentVector(arrow, player)));
                arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 10, 0.01, 0.01, 0.01, 0.01);
            } else {
                arrow.setGravity(true);
                TaskWrapper t = taskRef.get();
                if (t != null) t.cancel();
            }
            if (currentCount > 20 * 60) {
                arrow.setGravity(true);
                TaskWrapper t = taskRef.get();
                if (t != null) t.cancel();
            }
            counter.incrementAndGet();
        }, 0, 1);
        
        taskRef.set(task);
    }

    private static Vector arrowAdjustmentVector(Arrow arrow, Player player) {
        return player.getEyeLocation().clone().subtract(new Vector(0, 0.5, 0)).subtract(arrow.getLocation()).toVector().normalize().multiply(0.1);
    }

    @EventHandler
    public void targetEvent(EliteMobTargetPlayerEvent event) {
        SkeletonTrackingArrow skeletonTrackingArrow = (SkeletonTrackingArrow) event.getEliteMobEntity().getPower(this);
        if (skeletonTrackingArrow == null) return;
        if (skeletonTrackingArrow.isFiring()) return;

        skeletonTrackingArrow.setFiring(true);
        repeatingTrackingArrowTask(event.getEliteMobEntity(), skeletonTrackingArrow);
    }

    private void repeatingTrackingArrowTask(EliteEntity eliteEntity, SkeletonTrackingArrow skeletonTrackingArrow) {
        AtomicReference<TaskWrapper> taskRef = new AtomicReference<>();
        
        TaskWrapper task = new TaskScheduler(MetadataHandler.PLUGIN).runTimerAsync(() -> {
            if (!eliteEntity.isValid()) {
                skeletonTrackingArrow.setFiring(false);
                TaskWrapper t = taskRef.get();
                if (t != null) t.cancel();
                return;
            }
            for (Entity nearbyEntity : eliteEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                if (nearbyEntity instanceof Player)
                    if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                            ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL)) {
                        Arrow arrow = AttackArrow.shootArrow(eliteEntity.getLivingEntity(), (Player) nearbyEntity);
                        arrow.setVelocity(arrow.getVelocity().multiply(0.2));
                        arrow.setGravity(false);
                        trackingArrowLoop((Player) nearbyEntity, arrow);
                    }
        }, 0, 20 * 8);
        
        taskRef.set(task);
    }

}
