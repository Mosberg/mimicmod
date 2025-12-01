package com.mimicmod.registry;

import com.mimicmod.MimicMod;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Registry for all mod items.
 * Handles item registration using the 1.21.10+ registry key pattern.
 */
public class ModItems {

    /**
     * Mimic Tooth - Primary crafting material dropped by mimics.
     * Used in future crafting recipes and decorative items.
     */
    public static final Item MIMIC_TOOTH = register(
            "mimic_tooth",
            Item::new,
            new Item.Settings());

    /**
     * Registers an item with registry key (required in 1.21.2+).
     *
     * @param name     Item identifier name
     * @param factory  Function to create the item from settings
     * @param settings Initial item settings
     * @param <T>      Item type
     * @return Registered item instance
     */
    private static <T extends Item> T register(String name, Function<Item.Settings, T> factory,
            Item.Settings settings) {
        Identifier id = Identifier.of(MimicMod.MODID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        // CRITICAL: Set registry key BEFORE creating item in 1.21+
        settings.registryKey(key);
        T item = factory.apply(settings);

        return Registry.register(Registries.ITEM, key, item);
    }

    /**
     * Initializes all mod items.
     * Called during mod initialization to trigger static initialization.
     */
    public static void register() {
        MimicMod.LOGGER.info("Registering {} items", 1);
    }
}
