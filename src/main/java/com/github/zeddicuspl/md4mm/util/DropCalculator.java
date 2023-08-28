package com.github.zeddicuspl.md4mm.util;

import com.github.zeddicuspl.md4mm.model.Config;
import com.github.zeddicuspl.md4mm.model.DropConfig;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDrops;
import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDropsApi;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.CustomItem;
import com.tealcube.minecraft.bukkit.mythicdrops.api.socketing.SocketGem;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.lib.lang3.ObjectUtils;

import java.math.RoundingMode;
import java.util.logging.Logger;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropCalculator {
    public static String EMPTY_TIER = "empty";
    public static String GEM_TIER = "gems";
    public static String IDENTITY_TOME_TIER = "identityTome";
    public static String SOCKET_EXTENDER_TIER = "socketExtender";
    public static String CUSTOM_ITEMS_TIER = "customItems";
    private final Logger log;

    public DropCalculator(Logger logger) {
        log = logger;
    }
    public List<ItemStack> getDropForEntityAndLocation(ActiveMob mob) {
        DropConfig dropConfig = getDropConfigForMobByName(Config.getInstance().getDropConfigs(), mob.getMobType());
        Integer locationBonus = getLocationBonus(mob);

        if (dropConfig == null) {
            log.info("No drop config for: " + mob.getMobType());
            return null;
        }

        log.info("Drop config: " + dropConfig.getTiers().entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue().toString())
                .collect(Collectors.joining(", ")));
        log.info("Region modifier: " + locationBonus.toString());

        String[] passesParts = dropConfig.getPasses().split("\s*?-\s*");
        int minPasses = passesParts.length > 0 ? Integer.parseInt(passesParts[0]) : 1;
        int maxPasses = passesParts.length > 1 ? Integer.parseInt(passesParts[1]) : 1;
        int randPasses = minPasses + (int) Math.round(Math.random() * (maxPasses - minPasses));

        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < randPasses; ++i) {
            String tierName = getRandomTierName(dropConfig, locationBonus);
            if (tierName == null || tierName.equals(DropCalculator.EMPTY_TIER)) {
                return null;
            }
            ItemStack item = getItemForTier(tierName);
            if (item != null) {
                log.info("Dropping item: " + item);
                drops.add(item);
            }
        }

        getExtraDrop(dropConfig, drops);

        return drops;
    }

    public DropConfig getDropConfigForMobByName(Map<String, DropConfig> config, String mobName) {
        AtomicReference<DropConfig> resultDropConfig = new AtomicReference<>();
        config.forEach((configMobName, dropConfig) -> {
            if (configMobName.equals(mobName)) {
                // do template merging if template field exists
                if (dropConfig.getTemplate() != null) {
                    // merge templates from right to left, then apply the mob config
                    List<String> templates = Arrays.asList(dropConfig.getTemplate().split(","));
                    Collections.reverse(templates);
                    for (String templateName: templates) {
                        DropConfig templateDropConfig = getDropConfigForMobByName(config, templateName.trim());
                        if (templateDropConfig != null) {
                            resultDropConfig.set(mergeDropConfigs(resultDropConfig.get(), templateDropConfig));
                        }
                    }
                }
                resultDropConfig.set(mergeDropConfigs(resultDropConfig.get(), dropConfig));
            }
        });

        DropConfig newDropConfig = resultDropConfig.get();
        if (newDropConfig == null) {
            return null;
        }

        // make sure there's no invalid values
        if (newDropConfig.getPasses() == null || !newDropConfig.getPasses().matches("^\\d+(\\s*-\\s*\\d+)?$")) {
            newDropConfig.setPasses("1");
        }
        if (newDropConfig.getTiers() == null) {
            newDropConfig.setTiers(new HashMap<>());
        }
        if (newDropConfig.getExtraDrops() == null) {
            newDropConfig.setExtraDrops(new HashMap<>());
        }

        return resultDropConfig.get();
    }

    private DropConfig mergeDropConfigs(DropConfig target, DropConfig source) {
        if (target == null) {
            return source;
        }

        DropConfig newDropConfig = new DropConfig();
        newDropConfig.setMobName(source.getMobName());

        // if source contains tiers
        if (source.getTiers() != null && !source.getTiers().isEmpty()) {
            // create new tier list with target initial values
            Map<String, BigDecimal> newTiers = new HashMap<>(
                    target.getTiers() != null ? target.getTiers() : new HashMap<>()
            );
            // merge, with replacing, source tiers
            source.getTiers().forEach((tierName, weight) -> newTiers.merge(tierName, weight, (v1, v2) -> v2));
            newDropConfig.setTiers(newTiers);
        } else {
            // if source tiers are empty, use target tiers
            newDropConfig.setTiers(target.getTiers());
        }

        // if source contains extra drops
        if (source.getExtraDrops() != null && !source.getExtraDrops().isEmpty()) {
            // create new extra drops list with target initial values
            Map<String, BigDecimal> newExtra = new HashMap<>(
                    target.getExtraDrops() != null ? target.getExtraDrops() : new HashMap<>()
            );
            // merge, with replacing, source extra drops
            source.getExtraDrops().forEach((extraName, chance) -> newExtra.merge(extraName, chance, (v1, v2) -> v2));
            newDropConfig.setExtraDrops(newExtra);
        } else {
            // if source tiers are empty, use target tiers
            newDropConfig.setExtraDrops(target.getTiers());
        }

        // if source contains passes, replace them
        newDropConfig.setPasses(source.getPasses() == null ? target.getPasses() : source.getPasses());

        return newDropConfig;
    }

    private Integer getLocationBonus(ActiveMob mob) {
        // get list of regions at location
        AbstractLocation location = mob.getLocation();
        BukkitWorld world = new BukkitWorld(BukkitAdapter.adapt(location.getWorld()));
        Location loc = new Location(world, location.getX(), location.getY(), location.getZ());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        // get list of region bonuses
        Map<String, Integer> regionModifiers = Config.getInstance().getRegionModifiers();
        return set.getRegions().stream()
                .map(region -> ObjectUtils.firstNonNull(regionModifiers.get(region.getId()), 0))
                .max(Integer::compare)
                .orElse(0);
    }

    private String getRandomTierName(DropConfig dropConfig, Integer bottomBoundary) {
        // find out min and max values for randomizer
        int min = (bottomBoundary != null ? bottomBoundary : 0);
        int max = dropConfig.getTiers().values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).intValue();
        if (max < 1) {
            return null;
        }

        // first, sort tiers from most probable to the least probable
        Stream<Map.Entry<String, BigDecimal>> sorted = dropConfig.getTiers().entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // select random integer between min and max
        int rand = min + (int) Math.round(Math.random() * (max - min));
        if (rand < 0) {
            return DropCalculator.EMPTY_TIER;
        }

        // start going through the sorted list, keep adding tier weights and see on which one it is equal of higher
        // to the random value
        AtomicReference<String> resultTier = new AtomicReference<>(DropCalculator.EMPTY_TIER);
        AtomicReference<Integer> current = new AtomicReference<>(0);
        sorted.forEach(e -> {
            if (!resultTier.get().equals("empty")) return;
            log.info("Randomized item tier: " + e.getKey());
            int newValue = e.getValue().intValue() + current.get();
            if (newValue >= rand) {
                resultTier.set(e.getKey());
                return;
            }
            current.set(newValue);
        });

        return resultTier.get();
    }

    private ItemStack getItemForTier(String tierName) {
        boolean unident = Config.getInstance().getUnidentifiedTiers().contains(tierName);
        MythicDrops mythicDrops = MythicDropsApi.getMythicDrops();
        if (tierName.equals(DropCalculator.EMPTY_TIER)) {
            return null;
        } else if (tierName.equals(DropCalculator.GEM_TIER)) {
            SocketGem socketGem = mythicDrops.getSocketGemManager().randomByWeight();
            if (socketGem == null) {
                return null;
            }
            return mythicDrops.getProductionLine().getSocketGemItemFactory().toItemStack(socketGem);
        } else if (tierName.equals(DropCalculator.IDENTITY_TOME_TIER)) {
            return mythicDrops.getProductionLine().getIdentificationItemFactory().buildIdentityTome();
        } else if (tierName.equals(DropCalculator.SOCKET_EXTENDER_TIER)) {
            return mythicDrops.getProductionLine().getSocketGemItemFactory().buildSocketExtender();
        } else if (tierName.equals(DropCalculator.CUSTOM_ITEMS_TIER)) {
            CustomItem customItem = mythicDrops.getCustomItemManager().randomByWeight();
            if (customItem == null) {
                return null;
            }
            return mythicDrops.getProductionLine().getCustomItemFactory().toItemStack(customItem);
        } else {
            Tier tier = mythicDrops.getTierManager().getByName(tierName);
            if (tier == null) {
                log.warning("We don't have a tier named '" + tierName + "'");
                return null;
            }
            ItemStack item = unident
                    ? mythicDrops.getProductionLine().getIdentificationItemFactory().buildUnidentifiedItem(tier)
                    : mythicDrops.getProductionLine().getTieredItemFactory().toItemStack(tier);
            if (item == null) {
                log.warning("We weren't able to make an item from the '" + tierName +  "' tier");
                return null;
            }
            return item;
        }
    }

    private void getExtraDrop(DropConfig dropConfig, List<ItemStack> drops) {
        dropConfig.getExtraDrops().forEach((extraDropName, chance) -> {
            if (chance
                    .setScale(2, RoundingMode.UP)
                    .compareTo(new BigDecimal(
                            Math.round(Math.random() * 100))
                            .setScale(2, RoundingMode.UP)
                    ) < 1
            ) {
                MythicDrops mythicDrops = MythicDropsApi.getMythicDrops();
                CustomItem customItem = mythicDrops.getCustomItemManager().getById(extraDropName);
                if (customItem != null) {
                    // try getting custom item with this name
                    ItemStack item = mythicDrops.getProductionLine().getCustomItemFactory().toItemStack(customItem);
                    log.info("Dropping extra item: " + item);
                    drops.add(item);
                } else {
                    // if no item found, see if there's gem with this name
                    SocketGem socketGem = mythicDrops.getSocketGemManager().getById(extraDropName);
                    if (socketGem != null) {
                        log.info("Dropping gem from extra drop: " + socketGem);
                        drops.add(mythicDrops.getProductionLine().getSocketGemItemFactory().toItemStack(socketGem));
                    } else {
                        log.info("Could not find extra item with name: " + extraDropName);
                    }
                }
            }
        });
    }
}
