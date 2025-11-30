package com.mimicmod.entity;

import com.mimicmod.MimicMod;
import com.mimicmod.config.MimicConfig;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import java.util.Objects;

/**
 * Optimized utility class for calculating scaled entity stats.
 *
 * Key optimizations:
 * - Single config instance caching (eliminated 10+ null checks)
 * - Config validation happens once at startup
 * - Reusable static multiplier switch statements
 * - Parameterized logging (lazy evaluation)
 */
public class BalanceUtils {
    private static volatile MimicConfig cachedConfig;
    private static boolean configValidated = false;

    /**
     * Gets cached config with lazy initialization.
     * Called once per method invocation instead of 10+ times.
     */
    private static MimicConfig getConfig() {
        // Double-checked locking pattern for thread safety
        if (cachedConfig == null) {
            synchronized (BalanceUtils.class) {
                if (cachedConfig == null) {
                    MimicConfig config = MimicMod.CONFIG;
                    if (config == null) {
                        throw new IllegalStateException("Config must be initialized before game start");
                    }
                    if (!configValidated) {
                        if (!config.validate()) {
                            throw new IllegalStateException("Invalid configuration detected");
                        }
                        configValidated = true;
                    }
                    cachedConfig = config;
                }
            }
        }
        return cachedConfig;
    }

    /**
     * Resets config cache (call when config is reloaded).
     */
    public static void resetConfigCache() {
        cachedConfig = null;
        configValidated = false;
    }

    /**
     * Calculates scaled health for a mimic entity.
     *
     * @param world    World instance for difficulty
     * @param biomeId  Biome identifier
     * @param variant  Variant identifier
     * @return Scaled health value (minimum 1.0)
     */
    public static double getScaledHealth(World world, String biomeId, MimicVariant variant) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(biomeId, "Biome ID cannot be null");
        Objects.requireNonNull(variant, "Variant cannot be null");

        MimicConfig config = getConfig();
        double result = config.getScaledHealth(biomeId, variant.getId());
        result *= getDifficultyHealthMultiplier(world.getDifficulty());

        if (config.debug.enable_combat_logging) {
            MimicMod.LOGGER.debug(
                "Scaled health for {} in {}: result={}",
                variant, biomeId, result);
        }

        return Math.max(1.0, result);
    }

    /**
     * Calculates scaled damage for a mimic entity.
     *
     * @param world    World instance for difficulty
     * @param biomeId  Biome identifier
     * @param variant  Variant identifier
     * @return Scaled damage value (minimum 0.5)
     */
    public static double getScaledDamage(World world, String biomeId, MimicVariant variant) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(biomeId, "Biome ID cannot be null");
        Objects.requireNonNull(variant, "Variant cannot be null");

        MimicConfig config = getConfig();
        double result = config.getScaledDamage(biomeId, variant.getId());
        result *= getDifficultyDamageMultiplier(world.getDifficulty());

        if (config.debug.enable_combat_logging) {
            MimicMod.LOGGER.debug(
                "Scaled damage for {} in {}: result={}",
                variant, biomeId, result);
        }

        return Math.max(0.5, result);
    }

    /**
     * Calculates scaled experience points for a variant.
     *
     * @param variant Variant identifier
     * @param baseXp  Base experience (typically 10)
     * @return Scaled experience value (minimum 1)
     */
    public static int getScaledExperience(MimicVariant variant, int baseXp) {
        Objects.requireNonNull(variant, "Variant cannot be null");

        MimicConfig config = getConfig();
        int result = config.getScaledExperience(variant.getId());

        if (config.debug.enable_combat_logging) {
            MimicMod.LOGGER.debug(
                "Scaled experience for {}: result={}",
                variant, result);
        }

        return Math.max(1, result);
    }

    /**
     * Gets difficulty multiplier for health.
     * Inlined for performance.
     */
    private static double getDifficultyHealthMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.5;
            case EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.5;
        };
    }

    /**
     * Gets difficulty multiplier for damage.
     * Inlined for performance.
     */
    private static double getDifficultyDamageMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.0;
            case EASY -> 0.5;
            case NORMAL -> 1.0;
            case HARD -> 1.5;
        };
    }

    /**
     * Calculates total loot multiplier based on variant and looting level.
     *
     * @param variant       Variant identifier
     * @param lootingLevel  Looting enchantment level (0-3)
     * @return Total loot multiplier
     */
    public static double getLootMultiplier(MimicVariant variant, int lootingLevel) {
        MimicConfig config = getConfig();
        double baseMultiplier = 1.0;
        double lootingBonus = lootingLevel * config.loot_settings.looting_multiplier;
        return baseMultiplier + lootingBonus;
    }

    /**
     * Determines if a rare book should drop based on variant.
     *
     * @param variant Variant identifier
     * @param random  Random value between 0 and 1
     * @return True if rare book should drop
     */
    public static boolean shouldDropRareBook(MimicVariant variant, double random) {
        MimicConfig config = getConfig();
        double chance = config.loot_settings.rare_book_drop_chance.getChance(variant.getId());
        return random < chance;
    }

    /**
     * Determines if a mimic tooth should drop.
     *
     * @param random Random value between 0 and 1
     * @return True if tooth should drop
     */
    public static boolean shouldDropTooth(double random) {
        MimicConfig config = getConfig();
        if (config.loot_settings.always_drop_tooth) {
            return true;
        }
        return random < config.loot_settings.tooth_drop_chance;
    }

    /**
     * Gets spawn weight for a biome.
     * Reduced logging overhead by using short-circuit evaluation.
     *
     * @param biomeId Biome identifier
     * @return Spawn weight (0 = don't spawn)
     */
    public static double getBiomeSpawnWeight(String biomeId) {
        MimicConfig config = getConfig();
        double weight = config.getBiomeWeight(biomeId);

        if (config.debug.enable_spawn_logging) {
            MimicMod.LOGGER.debug("Biome spawn weight for {}: {}", biomeId, weight);
        }

        return weight;
    }

    /**
     * Checks if mimics should spawn in the current light level.
     *
     * @param lightLevel Current light level (0-15)
     * @return True if spawn is allowed
     */
    public static boolean canSpawnInLight(int lightLevel) {
        MimicConfig config = getConfig();
        return lightLevel >= config.spawn_settings.min_light_level
            && lightLevel <= config.spawn_settings.max_light_level;
    }

    /**
     * Gets the idle sound interval in ticks.
     * Cache result in entity to avoid repeated lookups.
     *
     * @return Ticks between idle sounds
     */
    public static int getIdleSoundInterval() {
        MimicConfig config = getConfig();
        return config.behavior.idle_sound_interval_ticks;
    }

    /**
     * Checks if the mimic should reveal when attacking.
     *
     * @return True if should reveal
     */
    public static boolean shouldRevealOnAttack() {
        MimicConfig config = getConfig();
        return config.behavior.reveal_on_attack;
    }

    /**
     * Validates all scaling calculations for a given configuration.
     * Useful for testing and debugging.
     *
     * @param world   World instance
     * @param biomeId Biome to test
     * @param variant Variant to test
     */
    public static void validateScaling(World world, String biomeId, MimicVariant variant) {
        MimicMod.LOGGER.info("=== Validating Scaling for {} in {} ===", variant, biomeId);
        double health = getScaledHealth(world, biomeId, variant);
        double damage = getScaledDamage(world, biomeId, variant);
        int experience = getScaledExperience(variant, 10);

        MimicMod.LOGGER.info("Results: Health={}, Damage={}, Experience={}", health, damage, experience);

        if (health < 1.0) {
            MimicMod.LOGGER.warn("Health is below minimum (1.0)!");
        }
        if (damage < 0.5) {
            MimicMod.LOGGER.warn("Damage is below minimum (0.5)!");
        }
        if (experience < 1) {
            MimicMod.LOGGER.warn("Experience is below minimum (1)!");
        }

        MimicMod.LOGGER.info("==========================================");
    }
}
