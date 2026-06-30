package me.codexadrian.spirit.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.codexadrian.spirit.Constants;
import me.codexadrian.spirit.Spirit;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpiritConfigImpl {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final FabricSpiritConfig DEFAULT_CONFIG = new FabricSpiritConfig(false, 3, 256, "spirit.soul_cage.tier_0", true, false);

    public static final Codec<FabricSpiritConfig> CODEC = RecordCodecBuilder.create(spiritConfigInstance -> spiritConfigInstance.group(
            Codec.BOOL.fieldOf("collectFromSoulless").orElse(false).forGetter(FabricSpiritConfig::collectFromSoulless),
            Codec.INT.fieldOf("soulPedestalRange").orElse(3).forGetter(FabricSpiritConfig::soulPedestalRange),
            Codec.INT.fieldOf("crudeSoulCrystalCap").orElse(256).forGetter(FabricSpiritConfig::crudeSoulCrystalCap),
            Codec.STRING.fieldOf("initialTierName").orElse("spirit.soul_cage.tier_0").forGetter(FabricSpiritConfig::initialTierName),
            Codec.BOOL.fieldOf("showChippedError").orElse(true).forGetter(FabricSpiritConfig::showChippedError),
            Codec.BOOL.fieldOf("vanillaMode").orElse(false).forGetter(FabricSpiritConfig::vanillaMode)
    ).apply(spiritConfigInstance, FabricSpiritConfig::new));

    public static FabricSpiritConfig config;

    public static boolean isCollectFromCorrupt() {
        return config == null ? DEFAULT_CONFIG.collectFromSoulless() : config.collectFromSoulless();
    }

    public static int getSoulPedestalRadius() {
        return config == null ? DEFAULT_CONFIG.soulPedestalRange() : config.soulPedestalRange();
    }

    public static int getCrudeSoulCrystalCap() {
        return config == null ? DEFAULT_CONFIG.crudeSoulCrystalCap() : config.crudeSoulCrystalCap();
    }

    public static String getInitialTierName() {
        return config == null ? DEFAULT_CONFIG.initialTierName() : config.initialTierName();
    }

    @org.jetbrains.annotations.Contract(pure = true)
    public static boolean showChippedError() {
        return config == null ? DEFAULT_CONFIG.showChippedError() : config.showChippedError();
    }

    @org.jetbrains.annotations.Contract(pure = true)
    public static boolean isVanillaMode() {
        return config == null ? DEFAULT_CONFIG.vanillaMode() : config.vanillaMode();
    }

    public static FabricSpiritConfig loadConfig(Path configFolder) {
        Path configPath = configFolder.resolve(Spirit.MODID + ".json");
        FabricSpiritConfig config = null;

        if (!Files.exists(configPath)) config = DEFAULT_CONFIG;
        else {
            try {
                JsonObject json = GSON.fromJson(new InputStreamReader(Files.newInputStream(configPath)), JsonObject.class);
                config = CODEC.parse(JsonOps.INSTANCE, json).result().orElse(DEFAULT_CONFIG);

            } catch (Exception e) {
                Spirit.LOGGER.error("Error parsing config file for mod " + Spirit.MODID);
            }
        }
        try {
            JsonElement encoded = CODEC.encodeStart(JsonOps.INSTANCE, config).result().orElseThrow();
            FileUtils.write(configPath.toFile(), GSON.toJson(encoded), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            Spirit.LOGGER.error("Error writing config file for mod " + Spirit.MODID);
        }
        SpiritConfigImpl.config = config;
        return config;
    }

    public record FabricSpiritConfig(boolean collectFromSoulless, int soulPedestalRange, int crudeSoulCrystalCap, String initialTierName, boolean showChippedError, boolean vanillaMode) {}
}
