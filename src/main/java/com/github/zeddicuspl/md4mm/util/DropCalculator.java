package com.github.zeddicuspl.md4mm.util;

import com.github.zeddicuspl.md4mm.model.Config;
import com.github.zeddicuspl.md4mm.model.DropConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DropCalculator {

    public DropConfig getDropConfigForMobByName(String mobName) {
        AtomicReference<DropConfig> resultDropConfig = new AtomicReference<>();
        Config.getInstance().getDropConfigs().forEach((configMobName, dropConfig) -> {
            if (configMobName.equals(mobName)) {
                // do template merging if template field exists
                if (dropConfig.getTemplate() != null) {
                    DropConfig templateDropConfig = getDropConfigForMobByName(configMobName);
                    if (templateDropConfig != null) {
                        resultDropConfig.set(mergeDropConfigs(templateDropConfig, dropConfig));
                    }
                }
            }
        });
        return resultDropConfig.get();
    }

    private DropConfig mergeDropConfigs(DropConfig config1, DropConfig config2) {
        DropConfig newDropConfig = new DropConfig();

        // if config2 contains tiers
        if (config2.getTiers() != null && !config2.getTiers().isEmpty()) {
            // create new tier list with config1 initial values
            Map<String, BigDecimal> newTiers = new HashMap<>(
                    config1.getTiers() != null ? config1.getTiers() : new HashMap<>()
            );
            // merge, with replacing, config2 tiers
            config2.getTiers().forEach((tierName, weight) -> newTiers.merge(tierName, weight, (v1, v2) -> v2));
            newDropConfig.setTiers(newTiers);
        } else {
            // if config2 tiers are empty, use config1 tiers
            newDropConfig.setTiers(config1.getTiers());
        }

        // if config2 contains passes, replace them
        newDropConfig.setPasses(config2.getPasses() == null ? config1.getPasses() : config2.getPasses());

        return newDropConfig;
    }
}
