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
    private final DropCalculator dropCalculator = new DropCalculator();

    public MythicMobDeathListener(MythicDropsForMythicMobsPlugin plugin) {
        log = plugin.getLogger();
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event)	{
        if (shouldAbortHandlingDeathEvent(event)) {
            return;
        }

        DropConfig dropConfigForMob = getDropConfigForMob(event.getMob());

        log.info("MythicMobDeathEvent called for " + event.getMob());
        // Let's get a tier by name
        Tier legendaryTier = MythicDropsApi.getMythicDrops().getTierManager().getByName("legendary");
        if (legendaryTier == null) {
            throw new RuntimeException("We don't have a tier named 'legendary'");
        }
        // Let's make an item from our tier
        ItemStack legendaryItem = MythicDropsApi.getMythicDrops().getProductionLine().getTieredItemFactory().toItemStack(legendaryTier);
        if (legendaryItem == null) {
            throw new RuntimeException("We weren't able to make an item from the 'legendary' tier");
        }
        List<ItemStack> drops = event.getDrops();
        drops.add(legendaryItem);
        event.setDrops(drops);
    }

    private Boolean shouldAbortHandlingDeathEvent(MythicMobDeathEvent event) {
        LivingEntity killer = event.getKiller();
        Entity entity = event.getEntity();
        return !(killer instanceof Player)
            || entity.getLastDamageCause() == null
            || entity.getLastDamageCause().isCancelled();
    }

    private DropConfig getDropConfigForMob(ActiveMob mob) {
        Log.info("Calculating drop table for " + mob.getMobType());
        return dropCalculator.getDropConfigForMobByName(mob.getMobType());
    }
}
