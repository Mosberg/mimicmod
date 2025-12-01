package com.mimicmod.entity;

import java.util.HashMap;
import java.util.Map;

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
     * Static lookup map for O(1) variant resolution from string IDs.
     */
    private static final Map<String, MimicVariant> ID_MAP = new HashMap<>();

    static {
        for (MimicVariant variant : values()) {
            ID_MAP.put(variant.id, variant);
        }
    }

    /**
     * Gets variant from string ID with O(1) lookup.
     * Prefers direct enum comparison when possible.
     *
     * @param variantId String identifier for the variant
     * @return Matching variant, or CLASSIC if not found
     */
    public static MimicVariant fromId(String variantId) {
        if (variantId == null) {
            return CLASSIC;
        }
        return ID_MAP.getOrDefault(variantId, CLASSIC);
    }

    /**
     * Gets the string identifier for this variant.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the health multiplier for this variant.
     */
    public double getHealthMultiplier() {
        return healthMultiplier;
    }

    /**
     * Gets the damage multiplier for this variant.
     */
    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Gets the experience multiplier for this variant.
     */
    public double getExperienceMultiplier() {
        return experienceMultiplier;
    }

    @Override
    public String toString() {
        return id;
    }
}
