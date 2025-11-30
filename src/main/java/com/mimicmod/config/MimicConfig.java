package com.mimicmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimicmod.MimicMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Configuration system for Mimic Mod.
 * Handles loading, validation, and default configuration values.
 */
public class MimicConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH = Paths.get("config", "mimicmod.json");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

  // Main configuration sections
  public SpawnRates spawn_rates = new SpawnRates();
  public List<String> christmas_dates = new ArrayList<>();
  public CombatScaling combat_scaling = new CombatScaling();
  public Map<String, Double> biome_weights = new HashMap<>();
  public Map<String, VariantMultipliers> variant_multipliers = new HashMap<>();
  public SpawnSettings spawn_settings = new SpawnSettings();
  public Behavior behavior = new Behavior();
  public LootSettings loot_settings = new LootSettings();
  public Debug debug = new Debug();

  /**
   * Spawn rates for each variant (should total 1.0).
   */
  public static class SpawnRates {
    public double classic = 0.70;
    public double corrupted = 0.20;
    public double ender = 0.08;
    public double christmas = 0.02;

    public double getRate(String variant) {
      return switch (variant) {
        case "corrupted" -> corrupted;
        case "ender" -> ender;
        case "christmas" -> christmas;
        default -> classic;
      };
    }
  }

  /**
   * Combat scaling configuration for health and damage.
   */
  public static class CombatScaling {
    public double health_base = 24.0;
    public double health_per_difficulty = 8.0;
    public double damage_base = 4.0;
    public double damage_per_difficulty = 2.0;
    public int experience_base = 10;
  }

  /**
   * Variant-specific multipliers for stats.
   */
  public static class VariantMultipliers {
    public double health = 1.0;
    public double damage = 1.0;
    public double experience = 1.0;

    public VariantMultipliers() {
    }

    public VariantMultipliers(double health, double damage, double experience) {
      this.health = health;
      this.damage = damage;
      this.experience = experience;
    }
  }

  /**
   * Spawn settings for world generation.
   */
  public static class SpawnSettings {
    public int min_group_size = 1;
    public int max_group_size = 1;
    public int spawn_weight = 8;
    public int min_light_level = 0;
    public int max_light_level = 7;
    public boolean spawn_in_dungeon = true;
    public boolean spawn_in_mineshaft = true;
    public boolean spawn_in_stronghold = true;
  }

  /**
   * Behavior settings for mimic entities.
   */
  public static class Behavior {
    public int idle_sound_interval_ticks = 200;
    public boolean reveal_on_attack = true;
    public boolean can_disguise_again = false;
    public double aggro_range = 24.0;
    public double movement_speed = 0.23;
  }

  /**
   * Loot drop settings.
   */
  public static class LootSettings {
    public boolean always_drop_tooth = true;
    public double tooth_drop_chance = 0.8;
    public RareBookDropChance rare_book_drop_chance = new RareBookDropChance();
    public double looting_multiplier = 0.5;

    public static class RareBookDropChance {
      public double classic = 0.15;
      public double corrupted = 0.25;
      public double ender = 0.30;
      public double christmas = 0.50;

      public double getChance(String variant) {
        return switch (variant) {
          case "corrupted" -> corrupted;
          case "ender" -> ender;
          case "christmas" -> christmas;
          default -> classic;
        };
      }
    }
  }

  /**
   * Debug settings for development.
   */
  public static class Debug {
    public boolean enable_spawn_logging = false;
    public boolean enable_combat_logging = false;
    public boolean show_hitboxes = false;
  }

  /**
   * Loads configuration from disk or creates default if not found.
   */
  public static MimicConfig load() {
    try {
      // Create config directory if it doesn't exist
      Files.createDirectories(CONFIG_PATH.getParent());

      if (Files.notExists(CONFIG_PATH)) {
        MimicMod.LOGGER.info("Config file not found, creating default configuration");
        MimicConfig defaultConfig = createDefaults();
        save(defaultConfig);
        return defaultConfig;
      }

      String json = Files.readString(CONFIG_PATH);
      MimicConfig config = GSON.fromJson(json, MimicConfig.class);

      if (config == null || !config.validate()) {
        MimicMod.LOGGER.warn("Invalid configuration detected, using defaults");
        return createDefaults();
      }

      MimicMod.LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
      return config;

    } catch (IOException e) {
      MimicMod.LOGGER.error("Failed to load configuration", e);
      return createDefaults();
    }
  }

  /**
   * Saves configuration to disk.
   */
  public static void save(MimicConfig config) {
    try {
      Files.createDirectories(CONFIG_PATH.getParent());
      String json = GSON.toJson(config);
      Files.writeString(CONFIG_PATH, json);
      MimicMod.LOGGER.info("Configuration saved to {}", CONFIG_PATH);
    } catch (IOException e) {
      MimicMod.LOGGER.error("Failed to save configuration", e);
    }
  }

  /**
   * Creates default configuration with sensible values.
   */
  public static MimicConfig createDefaults() {
    MimicConfig config = new MimicConfig();

    // Spawn rates
    config.spawn_rates = new SpawnRates();

    // Christmas dates
    config.christmas_dates = Arrays.asList("12-24", "12-25", "12-26");

    // Combat scaling
    config.combat_scaling = new CombatScaling();

    // Biome weights
    config.biome_weights.put("minecraft:plains", 1.0);
    config.biome_weights.put("minecraft:forest", 1.2);
    config.biome_weights.put("minecraft:dark_forest", 1.8);
    config.biome_weights.put("minecraft:swamp", 1.5);
    config.biome_weights.put("minecraft:taiga", 1.1);
    config.biome_weights.put("minecraft:jungle", 1.3);
    config.biome_weights.put("minecraft:desert", 0.7);
    config.biome_weights.put("minecraft:savanna", 0.8);
    config.biome_weights.put("minecraft:badlands", 0.9);
    config.biome_weights.put("minecraft:mushroom_fields", 0.3);
    config.biome_weights.put("minecraft:the_nether", 0.0);
    config.biome_weights.put("minecraft:the_end", 0.0);
    config.biome_weights.put("minecraft:deep_dark", 2.5);
    config.biome_weights.put("minecraft:dripstone_caves", 1.6);
    config.biome_weights.put("minecraft:lush_caves", 1.4);

    // Variant multipliers
    config.variant_multipliers.put("classic", new VariantMultipliers(1.0, 1.0, 1.0));
    config.variant_multipliers.put("corrupted", new VariantMultipliers(1.5, 1.4, 2.0));
    config.variant_multipliers.put("ender", new VariantMultipliers(2.0, 1.8, 3.0));
    config.variant_multipliers.put("christmas", new VariantMultipliers(1.2, 1.1, 1.5));

    // Spawn settings
    config.spawn_settings = new SpawnSettings();

    // Behavior
    config.behavior = new Behavior();

    // Loot settings
    config.loot_settings = new LootSettings();

    // Debug
    config.debug = new Debug();

    return config;
  }

  /**
   * Validates configuration values for sanity.
   */
  public boolean validate() {
    // Validate spawn rates sum to approximately 1.0
    double totalSpawnRate = spawn_rates.classic + spawn_rates.corrupted +
        spawn_rates.ender + spawn_rates.christmas;
    if (Math.abs(totalSpawnRate - 1.0) > 0.01) {
      MimicMod.LOGGER.warn("Spawn rates don't sum to 1.0 (got {})", totalSpawnRate);
      return false;
    }

    // Validate combat scaling
    if (combat_scaling.health_base <= 0) {
      MimicMod.LOGGER.warn("Invalid health_base: {}, must be positive", combat_scaling.health_base);
      return false;
    }

    if (combat_scaling.damage_base <= 0) {
      MimicMod.LOGGER.warn("Invalid damage_base: {}, must be positive", combat_scaling.damage_base);
      return false;
    }

    // Validate spawn settings
    if (spawn_settings.min_group_size > spawn_settings.max_group_size) {
      MimicMod.LOGGER.warn("min_group_size cannot be greater than max_group_size");
      return false;
    }

    if (spawn_settings.min_light_level > spawn_settings.max_light_level) {
      MimicMod.LOGGER.warn("min_light_level cannot be greater than max_light_level");
      return false;
    }

    // Validate biome weights
    for (Map.Entry<String, Double> entry : biome_weights.entrySet()) {
      if (entry.getValue() < 0) {
        MimicMod.LOGGER.warn("Biome weight for {} is negative: {}", entry.getKey(), entry.getValue());
        return false;
      }
    }

    return true;
  }

  /**
   * Gets biome spawn weight with fallback.
   */
  public double getBiomeWeight(String biomeId) {
    return biome_weights.getOrDefault(biomeId, 1.0);
  }

  /**
   * Gets variant multipliers with fallback to classic.
   */
  public VariantMultipliers getVariantMultipliers(String variantId) {
    return variant_multipliers.getOrDefault(variantId, new VariantMultipliers(1.0, 1.0, 1.0));
  }

  /**
   * Checks if current date is a Christmas date.
   */
  public boolean isChristmasDate() {
    String currentDate = LocalDate.now().format(DATE_FORMATTER);
    return christmas_dates.contains(currentDate);
  }

  /**
   * Gets the appropriate variant based on spawn rates and current date.
   */
  public String getRandomVariant(Random random) {
    // Boost Christmas spawn rate during Christmas dates
    if (isChristmasDate()) {
      if (random.nextDouble() < 0.5) { // 50% chance during Christmas
        return "christmas";
      }
    }

    double roll = random.nextDouble();
    double cumulative = 0.0;

    cumulative += spawn_rates.classic;
    if (roll < cumulative)
      return "classic";

    cumulative += spawn_rates.corrupted;
    if (roll < cumulative)
      return "corrupted";

    cumulative += spawn_rates.ender;
    if (roll < cumulative)
      return "ender";

    return "christmas";
  }

  /**
   * Gets scaled health based on biome difficulty.
   */
  public double getScaledHealth(String biomeId, String variantId) {
    double baseHealth = combat_scaling.health_base;
    double biomeWeight = getBiomeWeight(biomeId);
    double difficultyBonus = combat_scaling.health_per_difficulty * (biomeWeight - 1.0);
    double variantMultiplier = getVariantMultipliers(variantId).health;

    return (baseHealth + difficultyBonus) * variantMultiplier;
  }

  /**
   * Gets scaled damage based on biome difficulty.
   */
  public double getScaledDamage(String biomeId, String variantId) {
    double baseDamage = combat_scaling.damage_base;
    double biomeWeight = getBiomeWeight(biomeId);
    double difficultyBonus = combat_scaling.damage_per_difficulty * (biomeWeight - 1.0);
    double variantMultiplier = getVariantMultipliers(variantId).damage;

    return (baseDamage + difficultyBonus) * variantMultiplier;
  }

  /**
   * Gets scaled experience based on variant.
   */
  public int getScaledExperience(String variantId) {
    double baseExp = combat_scaling.experience_base;
    double variantMultiplier = getVariantMultipliers(variantId).experience;

    return (int) Math.round(baseExp * variantMultiplier);
  }

  /**
   * Logs current configuration for debugging.
   */
  public void logConfiguration() {
    if (debug.enable_spawn_logging) {
      MimicMod.LOGGER.info("=== Mimic Mod Configuration ===");
      MimicMod.LOGGER.info("Spawn Rates: Classic={}, Corrupted={}, Ender={}, Christmas={}",
          spawn_rates.classic, spawn_rates.corrupted, spawn_rates.ender, spawn_rates.christmas);
      MimicMod.LOGGER.info("Base Stats: Health={}, Damage={}, Experience={}",
          combat_scaling.health_base, combat_scaling.damage_base, combat_scaling.experience_base);
      MimicMod.LOGGER.info("Christmas Dates: {}", christmas_dates);
      MimicMod.LOGGER.info("Is Christmas: {}", isChristmasDate());
      MimicMod.LOGGER.info("===============================");
    }
  }
}
