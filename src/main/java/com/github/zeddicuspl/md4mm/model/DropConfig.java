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

    public DropConfig() {
    }
    public DropConfig(String mobName, Map<String, BigDecimal> tiers, String passes, String template) {
        this.mobName = mobName;
        this.tiers = tiers;
        this.passes = passes;
        this.template = template;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", mobName);
        args.put("tiers", tiers);
        args.put("passes", passes);
        args.put("template", template);
        return args;
    }

    public static DropConfig deserialize(String mobName, ConfigurationSection dropConfig) {
        ConfigurationSection tiersObj = dropConfig.getConfigurationSection("tiers");
        if (tiersObj == null) return null;

        Map<String, BigDecimal> tiersEntries = tiersObj.getValues(false).entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new BigDecimal(String.valueOf(e.getValue())))
        );

        return new DropConfig(
                mobName,
                tiersEntries,
                dropConfig.getString("passes"),
                dropConfig.getString("template")
        );
    }
}
