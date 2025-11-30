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
 */
public class ModEntities {

  // Create registry key for the mimic entity
  private static final RegistryKey<EntityType<?>> MIMIC_KEY = RegistryKey.of(
      RegistryKeys.ENTITY_TYPE,
      Identifier.of(MimicMod.MODID, "mimic"));

  // Register entity type with RegistryKey parameter in build()
  public static final EntityType<MimicEntity> MIMIC = Registry.register(
      Registries.ENTITY_TYPE,
      MIMIC_KEY.getValue(),
      EntityType.Builder.create(MimicEntity::new, SpawnGroup.MONSTER)
          .dimensions(0.875F, 0.875F) // Chest-sized hitbox
          .maxTrackingRange(8)
          .trackingTickInterval(3)
          .build(MIMIC_KEY) // CRITICAL: Pass RegistryKey to build() in 1.21.10
  );

  /**
   * Registers entity default attributes.
   * Must be called during mod initialization.
   */
  public static void registerAttributes() {
    FabricDefaultAttributeRegistry.register(MIMIC, MimicEntity.createAttributes());
    MimicMod.LOGGER.info("Registered entity attributes for {}", 1);
  }

  public static void register() {
    MimicMod.LOGGER.info("Registering entities");

    // Register attributes
    FabricDefaultAttributeRegistry.register(MIMIC, MimicEntity.createAttributes());

    MimicMod.LOGGER.info("Entities registered successfully");
  }
}
