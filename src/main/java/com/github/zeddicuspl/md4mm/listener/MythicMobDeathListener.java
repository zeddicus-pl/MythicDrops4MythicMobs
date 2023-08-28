package com.github.zeddicuspl.md4mm.listener;

import com.github.zeddicuspl.md4mm.MythicDropsForMythicMobsPlugin;
import com.github.zeddicuspl.md4mm.model.DropConfig;
import com.github.zeddicuspl.md4mm.util.DropCalculator;
import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDropsApi;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.utils.logging.Log;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Logger;

public class MythicMobDeathListener implements Listener {
    private final Logger log;
    private final DropCalculator dropCalculator;

    public MythicMobDeathListener(MythicDropsForMythicMobsPlugin plugin) {
        log = plugin.getLogger();
        dropCalculator = new DropCalculator(log);
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event)	{
        if (shouldAbortHandlingDeathEvent(event)) {
            return;
        }
        log.info("MythicMobDeathEvent called for " + event.getMob().getMobType());

        List<ItemStack> items = dropCalculator.getDropForEntityAndLocation(event.getMob());
        List<ItemStack> drops = event.getDrops();
        if (items != null && !items.isEmpty()) {
            drops.addAll(items);
        }
        event.setDrops(drops);
    }

    private Boolean shouldAbortHandlingDeathEvent(MythicMobDeathEvent event) {
        LivingEntity killer = event.getKiller();
        Entity entity = event.getEntity();
        return !(killer instanceof Player)
            || entity.getLastDamageCause() == null
            || entity.getLastDamageCause().isCancelled();
    }
}
