package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemQualityColorizer;
import me.MinhTaz.FoliaLib.TaskScheduler;
import me.MinhTaz.FoliaLib.TaskScheduler.TaskWrapper;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RareDropEffect implements Listener {

    public static void runEffect(Item item) {

        if (!ItemSettingsConfig.isEnableRareItemParticleEffects()) return;
        if (!(ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.LIGHT_BLUE) ||
                ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.GOLD)))
            return;

        TaskScheduler scheduler = new TaskScheduler(MetadataHandler.PLUGIN);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<TaskWrapper> taskRef = new AtomicReference<>();

        Runnable timerTask = () -> {
            if (item == null || !item.isValid() || item.isDead()) {
                if (taskRef.get() != null) taskRef.get().cancel();
                return;
            }

            item.getWorld().spawnParticle(Particle.PORTAL, item.getLocation(), 5, 0.01, 0.01, 0.01, 0.5);

            counter.addAndGet(20);
            if (counter.get() > 20 * 60 * 2) {
                if (taskRef.get() != null) taskRef.get().cancel();
            }
        };

        taskRef.set(scheduler.runTimerAsync(timerTask, 0, 20));
    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {
        if (!ItemTagger.isEliteItem(event.getEntity().getItemStack())) return;
        runEffect(event.getEntity());
    }

}
