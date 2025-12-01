package com.mimicmod.registry;

import com.mimicmod.MimicMod;
import com.mimicmod.entity.MimicEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for mod entities.
 * Handles entity type registration and attribute setup.
 */
public class ModEntities {

    /**
     * Create registry key for the mimic entity.
     */
    private static final RegistryKey<EntityType<?>> MIMIC_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            Identifier.of(MimicMod.MODID, "mimic"));

    /**
     * Register entity type with RegistryKey parameter in build().
     * CRITICAL: Must pass RegistryKey to build() in 1.21.10+
     */
    public static final EntityType<MimicEntity> MIMIC = Registry.register(
            Registries.ENTITY_TYPE,
            MIMIC_KEY.getValue(),
            EntityType.Builder.create(MimicEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.875F, 0.875F) // Chest-sized hitbox
                    .maxTrackingRange(8)
                    .trackingTickInterval(3)
                    .build(MIMIC_KEY) // CRITICAL: Pass RegistryKey to build()
    );

    /**
     * Registers entity default attributes.
     * Must be called during mod initialization.
     * Called separately from registration to ensure proper setup order.
     */
    public static void registerAttributes() {
        try {
            FabricDefaultAttributeRegistry.register(MIMIC, MimicEntity.createAttributes());
            MimicMod.LOGGER.info("Registered entity attributes for MIMIC entity type");
        } catch (Exception e) {
            MimicMod.LOGGER.error("Failed to register entity attributes", e);
        }
    }

    /**
     * Main registry method - called during mod initialization.
     * Ensures all entity-related registrations are complete.
     */
    public static void register() {
        MimicMod.LOGGER.info("Registering entities for Mimic Mod");
        // Attributes are registered via registerAttributes() which is called separately
        MimicMod.LOGGER.info("Entity registration completed successfully");
    }
}
