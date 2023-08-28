package com.github.zeddicuspl.md4mm.command;

import com.github.zeddicuspl.md4mm.MythicDropsForMythicMobsPlugin;
import com.github.zeddicuspl.md4mm.model.Config;
import com.github.zeddicuspl.md4mm.model.DropConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReloadCommand implements CommandExecutor {
    private final Logger log;
    private final MythicDropsForMythicMobsPlugin plugin;

    public ReloadCommand(MythicDropsForMythicMobsPlugin plugin) {
        this.log = plugin.getLogger();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args != null && args[0] != null && args[0].equals("reload")) {
            plugin.reloadConfig();
            readConfigs();
            sender.sendMessage("Reloading MythicDrops4MythicMobs configs");
        }
        return true;
    }

    public void readConfigs() {
        log.info("Reading config files");

        // Ensure "drops" folder exists in config
        File drops = new File(plugin.getDataFolder().getAbsolutePath(), "drops");
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
        Map<String, Integer> regionModifiers = new HashMap<>();
        ConfigurationSection regions = plugin.getConfig().getConfigurationSection("regions");
        if (regions != null) {
            regionModifiers = regions.getValues(false).entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> Integer.valueOf(String.valueOf(e.getValue())))
            );
        }
        Config.getInstance().setRegionModifiers(regionModifiers);

        // Read list of tiers that drop unidentified items
        Config.getInstance().setUnidentifiedTiers(plugin.getConfig().getStringList("unidentifiedTiers"));
    }
}
