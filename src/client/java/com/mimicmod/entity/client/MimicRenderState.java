package com.mimicmod.entity.client;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;

/**
 * Render state for Mimic entities.
 * Stores client-side rendering information synced from the server entity.
 *
 * <p>
 * This class bridges the gap between entity data and rendering,
 * providing animation-related fields and variant information.
 */
public class MimicRenderState extends LivingEntityRenderState {

  /**
   * Current variant of the mimic.
   * Options: classic, corrupted, ender, christmas
   */
  public String variant = "classic";

  /**
   * Whether the mimic is in its true form or disguised as a chest.
   */
  public boolean revealed = false;

  /**
   * Whether the mimic is currently attacking a player.
   */
  public boolean attacking = false;

  /**
   * Movement speed for jump animation calculations.
   * Inherited from LivingEntityRenderState but explicitly tracked.
   */
  public float movementSpeed = 0.0F;

  /**
   * Limb swing animation value.
   * Used for jumping/bouncing motion timing.
   */
  public float limbPose = 0.0F;
}
