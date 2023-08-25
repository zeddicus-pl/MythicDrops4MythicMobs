package com.github.zeddicuspl.md4mm.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class Config {
    private static Config instance;
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Map<String, DropConfig> dropConfigs;
    private Map<String, BigDecimal> regionModifiers;
}

