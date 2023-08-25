package com.github.zeddicuspl.md4mm;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.zeddicuspl.md4mm.listener.MythicMobDeathListener;
import com.github.zeddicuspl.md4mm.model.Config;
import com.github.zeddicuspl.md4mm.model.DropConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicDropsForMythicMobsPlugin extends JavaPlugin {

	private Logger log;

	@Override
	public void onEnable() {
		log = this.getLogger();
		this.saveDefaultConfig();
		readConfigs();
		MythicMobDeathListener deathListener = new MythicMobDeathListener(this);
		Bukkit.getPluginManager().registerEvents(deathListener, this);
	}

	private void readConfigs() {
		log.info("Reading config files");

		// Ensure "drops" folder exists in config
		File dataFolder = getDataFolder();
		File drops = new File(dataFolder.getAbsolutePath(), "drops");
		if (!drops.exists()) {
			if (!drops.mkdirs()) {
				throw new RuntimeException("Could not create drops directory in plugin config folder");
			}
		}

		// Read and parse all files in "drops" folder
		Map<String, DropConfig> dropConfigs = new HashMap<>();
		for (String filename: Objects.requireNonNull(drops.list())) {
			if (!filename.matches(".+\\.yml$")) continue;
			File drop = new File(drops.getAbsolutePath(), filename);
			YamlConfiguration config = YamlConfiguration.loadConfiguration(drop);
			Set<String> keys = config.getKeys(false);
			for (String mobName: keys) {
				ConfigurationSection section = config.getConfigurationSection(mobName);
				if (section != null) {
					dropConfigs.put(mobName, DropConfig.deserialize(mobName, section));
				}
			}
		}
		Config.getInstance().setDropConfigs(dropConfigs);

		// Read region modifiers
		Map<String, BigDecimal> regionModifiers = new HashMap<>();
		ConfigurationSection regions = getConfig().getConfigurationSection("regions");
		if (regions != null) {
			regionModifiers = regions.getValues(false).entrySet().stream().collect(
					Collectors.toMap(Map.Entry::getKey, e -> new BigDecimal(String.valueOf(e)))
			);
		}
		Config.getInstance().setRegionModifiers(regionModifiers);
	}
}

