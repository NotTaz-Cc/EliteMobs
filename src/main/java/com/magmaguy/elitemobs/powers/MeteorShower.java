package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import me.MinhTaz.FoliaLib.TaskScheduler;
import me.MinhTaz.FoliaLib.TaskScheduler.TaskWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MeteorShower extends BossPower implements Listener {
    public MeteorShower() {
        super(PowersConfig.getPower("meteor_shower.yml"));
    }

    public static void doMeteorShower(EliteEntity eliteEntity) {
         eliteEntity.getLivingEntity().setAI(false);
         final Location initialLocation = eliteEntity.getLivingEntity().getLocation().clone();
         AtomicInteger counter = new AtomicInteger(0);
         AtomicReference<TaskWrapper> taskRef = new AtomicReference<>();

         TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
         Runnable timerTask = () -> {
             if (!eliteEntity.isValid()) {
                 if (taskRef.get() != null) taskRef.get().cancel();
                 return;
             }

             if (counter.get() > 10 * 20) {
                 if (taskRef.get() != null) taskRef.get().cancel();
                 eliteEntity.getLivingEntity().setAI(true);
                 eliteEntity.getLivingEntity().teleport(initialLocation);
                 return;
             }

             counter.incrementAndGet();

             doCloudEffect(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));

             if (counter.get() > 2 * 20) {
                 doFireballs(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)), eliteEntity);
             }
         };
         taskRef.set(scheduler.runTimerAsync(timerTask, 0, 1));
     }

    public static void doCloudEffect(Location location) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            location.getWorld().spawnParticle(Particle.EXPLOSION, newLocation, 1, 0, 0, 0, 0);
        }
    }

    private static void doFireballs(Location location, EliteEntity eliteEntity) {
        for (int i = 0; i < 1; i++) {
            int randX = ThreadLocalRandom.current().nextInt(30) - 15;
            int randY = ThreadLocalRandom.current().nextInt(2);
            int randZ = ThreadLocalRandom.current().nextInt(30) - 15;
            Location newLocation = location.clone().add(new Vector(randX, randY, randZ));
            newLocation = newLocation.setDirection(new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, -0.5, ThreadLocalRandom.current().nextDouble() - 0.5));
            Fireball fireball = (Fireball) location.getWorld().spawnEntity(newLocation, EntityType.FIREBALL);
            fireball.setShooter(eliteEntity.getLivingEntity());
            fireball.setDirection(fireball.getDirection().multiply(0.5));
        }
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        MeteorShower meteorShower = (MeteorShower) event.getEliteMobEntity().getPower(this);
        if (meteorShower == null) return;
        if (!eventIsValid(event, meteorShower)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        meteorShower.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doMeteorShower(event.getEliteMobEntity());

    }

}
