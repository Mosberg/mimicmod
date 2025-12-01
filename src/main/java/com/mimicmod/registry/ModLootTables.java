package com.mimicmod.registry;

import com.mimicmod.MimicMod;

import net.minecraft.util.Identifier;

/**
 * Registry for mod loot tables.
 * Defines identifiers for custom loot tables loaded from JSON.
 */
public class ModLootTables {

    /**
     * Classic mimic loot table - Basic drops with rare cursed tome.
     */
    public static final Identifier MIMIC = Identifier.of(MimicMod.MODID, "entities/mimic");

    /**
     * Corrupted mimic loot table - Enhanced drops with corrupted grimoire.
     */
    public static final Identifier MIMIC_CORRUPTED = Identifier.of(MimicMod.MODID, "entities/mimic_corrupted");

    /**
     * Ender mimic loot table - End-themed drops with enderian codex.
     */
    public static final Identifier MIMIC_ENDER = Identifier.of(MimicMod.MODID, "entities/mimic_ender");

    /**
     * Christmas mimic loot table - Festive drops with Santa's naughty list.
     */
    public static final Identifier MIMIC_CHRISTMAS = Identifier.of(MimicMod.MODID, "entities/mimic_christmas");

    /**
     * Initializes all mod loot tables.
     * Called during mod initialization.
     */
    public static void register() {
        MimicMod.LOGGER.info("Registered {} loot table identifiers", 4);
    }
}
