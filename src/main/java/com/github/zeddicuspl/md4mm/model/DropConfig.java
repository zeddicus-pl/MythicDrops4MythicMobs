package com.github.zeddicuspl.md4mm.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class DropConfig implements ConfigurationSerializable {
    private String mobName;
    private Map<String, BigDecimal> tiers;
    private String passes;
    private String template;
    private Map<String, BigDecimal> extraDrops;

    public DropConfig() {
    }
    public DropConfig(String mobName, Map<String, BigDecimal> tiers, String passes, String template, Map<String, BigDecimal> extra) {
        this.mobName = mobName;
        this.tiers = tiers;
        this.passes = passes;
        this.template = template;
        this.extraDrops = extra;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", mobName);
        args.put("tiers", tiers);
        args.put("passes", passes);
        args.put("template", template);
        args.put("extraDrops", extraDrops);
        return args;
    }

    public static DropConfig deserialize(String mobName, ConfigurationSection dropConfig) {

        Map<String, BigDecimal> tiersEntries = readMap(dropConfig.getConfigurationSection("tiers"));
        Map<String, BigDecimal> extraEntries = readMap(dropConfig.getConfigurationSection("extraDrops"));

        return new DropConfig(
                mobName,
                tiersEntries,
                dropConfig.getString("passes"),
                dropConfig.getString("template"),
                extraEntries
        );
    }

    private static Map<String, BigDecimal> readMap(ConfigurationSection section) {
        Map<String, BigDecimal> entries;
        if (section != null) {
            entries = section.getValues(false).entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> new BigDecimal(String.valueOf(e.getValue())))
            );
        } else {
            entries = new HashMap<>();
        }
        return entries;
    }
}
