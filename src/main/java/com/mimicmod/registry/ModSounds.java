package com.mimicmod.registry;

import com.mimicmod.MimicMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Registry for all mod sound events.
 * Defines custom sounds for mimic entities.
 */
public class ModSounds {

  /**
   * Idle/ambient breathing sound - Plays periodically when mimic is not in
   * combat.
   */
  public static final SoundEvent MIMIC_IDLE = register("entity.mimic.idle");

  /**
   * Reveal sound - Plays when mimic transforms from chest to monster form.
   */
  public static final SoundEvent MIMIC_REVEAL = register("entity.mimic.reveal");

  /**
   * Attack sound - Plays during mimic's attack animation.
   */
  public static final SoundEvent MIMIC_ATTACK = register("entity.mimic.attack");

  /**
   * Registers a sound event with the given identifier.
   *
   * @param id Sound event identifier (e.g., "entity.mimic.idle")
   * @return Registered sound event
   */
  private static SoundEvent register(String id) {
    Identifier identifier = Identifier.of(MimicMod.MODID, id);
    return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
  }

  /**
   * Initializes all mod sounds.
   * Called during mod initialization to trigger static initialization.
   */
  public static void register() {
    MimicMod.LOGGER.info("Registering {} sounds", 3);
  }
}
