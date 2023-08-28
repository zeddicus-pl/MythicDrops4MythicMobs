package com.github.zeddicuspl.md4mm.util;

import com.github.zeddicuspl.md4mm.model.DropConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropCalculator {

    private Map<String, DropConfig> mockConfig;

    @Test
    public void testTypicalMobDropConfig() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"MY_ZOMBIE");

        assertEquals("MY_ZOMBIE", myZombie.getMobName());
        assertEquals("5", myZombie.getPasses());
        assertEquals(2, myZombie.getTiers().size());
        assertEquals(50, myZombie.getTiers().get("foo").intValue());
        assertEquals(50, myZombie.getTiers().get("bar").intValue());
    }

    @Test
    public void testChainedInheritanceMobDropConfig() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"MY_WEIRD_ZOMBIE");

        assertEquals("MY_WEIRD_ZOMBIE", myZombie.getMobName());
        assertEquals("6", myZombie.getPasses());
        assertEquals(3, myZombie.getTiers().size());
        assertEquals(50, myZombie.getTiers().get("foo").intValue());
        assertEquals(50, myZombie.getTiers().get("bar").intValue());
        assertEquals(123, myZombie.getTiers().get("abc").intValue());
    }

    @Test
    public void testMultipleInheritanceMobDropConfig() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"COMPLICATED_ZOMBIE");

        assertEquals("COMPLICATED_ZOMBIE", myZombie.getMobName());
        assertEquals("1", myZombie.getPasses());
        assertEquals(4, myZombie.getTiers().size());
        assertEquals(50, myZombie.getTiers().get("foo").intValue());
        assertEquals(50, myZombie.getTiers().get("bar").intValue());
        assertEquals(123, myZombie.getTiers().get("abc").intValue());
        assertEquals(11, myZombie.getTiers().get("aha").intValue());
    }

    @Test
    public void testMobUsingEmptyTemplate() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"ANOTHER_ZOMBIE");

        assertEquals("ANOTHER_ZOMBIE", myZombie.getMobName());
        assertEquals("1", myZombie.getPasses());
        assertEquals(2, myZombie.getTiers().size());
        assertEquals(10, myZombie.getTiers().get("foo").intValue());
        assertEquals(20, myZombie.getTiers().get("bar").intValue());
    }

    @Test
    public void testEmptyMobUsingCorruptedTemplate() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"YET_ANOTHER_ZOMBIE");

        assertEquals("YET_ANOTHER_ZOMBIE", myZombie.getMobName());
        assertEquals("1", myZombie.getPasses());
        assertEquals(0, myZombie.getTiers().size());
    }

    @Test
    public void testMobUsingCorruptedTemplate() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"NICE_ZOMBIE");

        assertEquals("NICE_ZOMBIE", myZombie.getMobName());
        assertEquals("7", myZombie.getPasses());
        assertEquals(2, myZombie.getTiers().size());
        assertEquals(20, myZombie.getTiers().get("foo").intValue());
        assertEquals(30, myZombie.getTiers().get("bar").intValue());
    }

    @Test
    public void testMobUsingNonExistingTemplate() {
        Map<String, DropConfig> exampleConfig = getExampleConfig();
        DropCalculator instance = new DropCalculator(null);
        DropConfig myZombie = instance.getDropConfigForMobByName(exampleConfig,"SHAKY_SKELETON");

        assertEquals("SHAKY_SKELETON", myZombie.getMobName());
        assertEquals("1-3", myZombie.getPasses());
        assertEquals(3, myZombie.getTiers().size());
        assertEquals(30, myZombie.getTiers().get("blah").intValue());
        assertEquals(20, myZombie.getTiers().get("haha").intValue());
        assertEquals(10, myZombie.getTiers().get("huh").intValue());
    }

    private Map<String, DropConfig> getExampleConfig() {
        if (mockConfig != null) {
            return mockConfig;
        }

        String testDataFile = "testDropConfigs.yml";
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(testDataFile);
        if (inputStream == null) {
            throw new RuntimeException("cannot load input stream for test drops config");
        }
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        Map<String, DropConfig> dropConfigs = new HashMap<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
        Set<String> keys = config.getKeys(false);
        for (String mobName: keys) {
            ConfigurationSection section = config.getConfigurationSection(mobName);
            if (section != null) {
                dropConfigs.put(mobName, DropConfig.deserialize(mobName, section));
            }
        }

        mockConfig = dropConfigs;
        return dropConfigs;
    }
}
