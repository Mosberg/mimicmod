package com.mimicmod.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Type-safe variant enumeration for Mimic entities.
 * Replaces string-based variant handling with compile-time safety.
 *
 * Benefits:
 * - Type safety (compile-time verification)
 * - Faster comparisons (enum reference equality)
 * - IDE autocomplete
 * - Reduced memory allocations
 */
public enum MimicVariant {
  CLASSIC("classic", 1.0, 1.0, 1.0),
  CORRUPTED("corrupted", 1.5, 1.4, 2.0),
  ENDER("ender", 2.0, 1.8, 3.0),
  CHRISTMAS("christmas", 1.2, 1.1, 1.5);

  private final String id;
  private final double healthMultiplier;
  private final double damageMultiplier;
  private final double experienceMultiplier;

  MimicVariant(String id, double healthMult, double damageMult, double experienceMult) {
    this.id = id;
    this.healthMultiplier = healthMult;
    this.damageMultiplier = damageMult;
    this.experienceMultiplier = experienceMult;
  }

  /**
   * Gets variant from string ID with O(1) lookup.
   * Prefer direct enum comparison when possible.
   */
  private static final Map<String, MimicVariant> ID_MAP = new HashMap<>();
  static {
    for (MimicVariant variant : values()) {
      ID_MAP.put(variant.id, variant);
    }
  }

  public static MimicVariant fromId(Optional<String> variantId) {
    return ID_MAP.getOrDefault(variantId, CLASSIC);
  }

  public String getId() {
    return id;
  }

  public double getHealthMultiplier() {
    return healthMultiplier;
  }

  public double getDamageMultiplier() {
    return damageMultiplier;
  }

  public double getExperienceMultiplier() {
    return experienceMultiplier;
  }

  @Override
  public String toString() {
    return id;
  }

  MimicVariant fromId(Optional<String> variantId) {
    throw new UnsupportedOperationException("Unimplemented method 'fromId'");
  }
}
