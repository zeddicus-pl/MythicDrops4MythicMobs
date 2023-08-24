package com.github.zeddicuspl.md4mm;

import java.util.List;
import java.util.logging.Logger;

import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDropsApi;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.bukkit.events.*;
import io.lumine.mythic.core.drops.Drop;
import io.lumine.mythic.core.drops.DropMetadataImpl;
import io.lumine.mythic.core.drops.LootBag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicDropsForMythicMobsPlugin extends JavaPlugin implements Listener {

	private Logger log;

	@Override
	public void onEnable() {
		log = this.getLogger();
		Bukkit.getPluginManager().registerEvents(this, this);
		log.info("MythicDropsForMythicMobs plugin enabled");
	}

	public void onDisable(){
		log.info("MythicDropsForMythicMobs plugin enabled");
	}

	@EventHandler
	public void onMythicSpawn(MythicMobSpawnEvent event)	{
		//log.info("MythicMobSpawnEvent called for " + event.getMob());
	}

	@EventHandler
	public void onMythicLootDrop(MythicMobLootDropEvent event)	{
		log.info("MythicMobLootDropEvent called for " + event.getMob());
	}

	@EventHandler
	public void onMythicLootDrop(MythicMobDeathEvent event)	{
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

	public void readConfigs() {
		FileConfiguration config = getConfig();

	}
}

