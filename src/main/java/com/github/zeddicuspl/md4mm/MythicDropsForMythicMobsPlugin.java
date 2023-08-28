package com.github.zeddicuspl.md4mm;

import com.github.zeddicuspl.md4mm.command.ReloadCommand;
import com.github.zeddicuspl.md4mm.listener.MythicMobDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicDropsForMythicMobsPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		ReloadCommand reloadCommand = new ReloadCommand(this);
		this.getCommand("md4mm").setExecutor(reloadCommand);
		this.saveDefaultConfig();
		reloadCommand.readConfigs();
		MythicMobDeathListener deathListener = new MythicMobDeathListener(this);
		Bukkit.getPluginManager().registerEvents(deathListener, this);
	}
}

