package com.mimicmod.registry;

import com.mimicmod.MimicMod;
import com.mimicmod.entity.MimicEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;

import java.util.Collection;

/**
 * Debug commands for testing and managing Mimic entities.
 */
public class ModCommands {

  // Suggestion provider for mimic variants
  private static final SuggestionProvider<ServerCommandSource> VARIANT_SUGGESTIONS = (context, builder) -> CommandSource
      .suggestMatching(new String[] { "classic", "corrupted", "ender", "christmas" }, builder);

  public static void register() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      registerDebugCommands(dispatcher);
    });
    MimicMod.LOGGER.info("Debug commands registered");
  }

  private static void registerDebugCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(CommandManager.literal("mimic")
        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2

        // /mimic spawn [variant]
        .then(CommandManager.literal("spawn")
            .then(CommandManager.argument("variant", StringArgumentType.string())
                .suggests(VARIANT_SUGGESTIONS)
                .executes(context -> spawnMimic(context, StringArgumentType.getString(context, "variant"))))
            .executes(context -> spawnMimic(context, "classic")))

        // /mimic info [entity]
        .then(CommandManager.literal("info")
            .then(CommandManager.argument("entity", EntityArgumentType.entity())
                .executes(ModCommands::getMimicInfo)))

        // /mimic setvariant <entity> <variant>
        .then(CommandManager.literal("setvariant")
            .then(CommandManager.argument("entity", EntityArgumentType.entity())
                .then(CommandManager.argument("variant", StringArgumentType.string())
                    .suggests(VARIANT_SUGGESTIONS)
                    .executes(ModCommands::setMimicVariant))))

        // /mimic reveal <entity>
        .then(CommandManager.literal("reveal")
            .then(CommandManager.argument("entity", EntityArgumentType.entity())
                .executes(ModCommands::revealMimic)))

        // /mimic hide <entity>
        .then(CommandManager.literal("hide")
            .then(CommandManager.argument("entity", EntityArgumentType.entity())
                .executes(ModCommands::hideMimic)))

        // /mimic spawnmany unt> [variant]
        .then(CommandManager.literal("spawnmany")
            .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 50))
                .then(CommandManager.argument("variant", StringArgumentType.string())
                    .suggests(VARIANT_SUGGESTIONS)
                    .executes(context -> spawnManyMimics(
                        context,
                        IntegerArgumentType.getInteger(context, "count"),
                        StringArgumentType.getString(context, "variant"))))
                .executes(context -> spawnManyMimics(
                    context,
                    IntegerArgumentType.getInteger(context, "count"),
                    "classic"))))

        // /mimic killall
        .then(CommandManager.literal("killall")
            .executes(ModCommands::killAllMimics))

        // /mimic config reload
        .then(CommandManager.literal("config")
            .then(CommandManager.literal("reload")
                .executes(ModCommands::reloadConfig)))

        // /mimic biome
        .then(CommandManager.literal("biome")
            .executes(ModCommands::getBiomeInfo)));
  }

  /**
   * Spawns a mimic at the player's location.
   */
  private static int spawnMimic(CommandContext<ServerCommandSource> context, String variant) {
    ServerCommandSource source = context.getSource();
    ServerPlayerEntity player = source.getPlayer();

    if (player == null) {
      source.sendError(Text.literal("This command must be run by a player"));
      return 0;
    }

    try {
      Vec3d pos = player.getEntityPos();
      MimicEntity mimic = new MimicEntity(ModEntities.MIMIC, player.getEntityWorld());
      mimic.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), 0.0F);
      mimic.setVariant(variant);

      // Get current biome for stat scaling
      BlockPos blockPos = player.getBlockPos();
      RegistryEntry<Biome> biomeEntry = player.getEntityWorld().getBiome(blockPos);
      String biomeId = biomeEntry.getKey()
          .map(key -> key.getValue().toString())
          .orElse("minecraft:plains");

      mimic.applyScaledStats(biomeId, variant);

      player.getEntityWorld().spawnEntity(mimic);

      source.sendFeedback(() -> Text.literal("Spawned ")
          .append(Text.literal(variant).formatted(Formatting.AQUA))
          .append(" mimic at your location"), true);

      return 1;
    } catch (Exception e) {
      MimicMod.LOGGER.error("Failed to spawn mimic", e);
      source.sendError(Text.literal("Failed to spawn mimic: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Gets detailed information about a mimic entity.
   */
  private static int getMimicInfo(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      Entity entity = EntityArgumentType.getEntity(context, "entity");

      if (!(entity instanceof MimicEntity mimic)) {
        source.sendError(Text.literal("Target entity is not a mimic"));
        return 0;
      }

      BlockPos pos = mimic.getBlockPos();
      RegistryEntry<Biome> biomeEntry = mimic.getEntityWorld().getBiome(pos);
      String biomeId = biomeEntry.getKey()
          .map(key -> key.getValue().toString())
          .orElse("unknown");

      // Get experience points via reflection or direct access if made public
      int xp = getExperiencePoints(mimic);

      source.sendFeedback(() -> Text.literal("=== Mimic Information ===").formatted(Formatting.GOLD), false);
      source.sendFeedback(() -> Text.literal("Variant: ").formatted(Formatting.GRAY)
          .append(Text.literal(mimic.getVariant()).formatted(Formatting.AQUA)), false);
      source.sendFeedback(() -> Text.literal("Revealed: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.valueOf(mimic.isRevealed())).formatted(Formatting.YELLOW)), false);
      source.sendFeedback(() -> Text.literal("Health: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.format("%.1f/%.1f", mimic.getHealth(), mimic.getMaxHealth()))
              .formatted(Formatting.RED)),
          false);
      source.sendFeedback(() -> Text.literal("Attack Damage: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.format("%.1f",
              mimic.getAttributeValue(EntityAttributes.ATTACK_DAMAGE)))
              .formatted(Formatting.DARK_RED)),
          false);
      source.sendFeedback(() -> Text.literal("Experience: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.valueOf(xp)).formatted(Formatting.GREEN)), false);
      source.sendFeedback(() -> Text.literal("Biome: ").formatted(Formatting.GRAY)
          .append(Text.literal(biomeId).formatted(Formatting.GREEN)), false);
      source.sendFeedback(() -> Text.literal("Position: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.format("%.1f, %.1f, %.1f",
              pos.getX() + 0.0, pos.getY() + 0.0, pos.getZ() + 0.0))
              .formatted(Formatting.WHITE)),
          false);

      return 1;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to get mimic info: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Helper method to get experience points from mimic.
   */
  private static int getExperiencePoints(MimicEntity mimic) {
    try {
      // Use reflection to access protected field
      var field = mimic.getClass().getSuperclass().getSuperclass().getDeclaredField("experiencePoints");
      field.setAccessible(true);
      return field.getInt(mimic);
    } catch (Exception e) {
      MimicMod.LOGGER.warn("Could not access experiencePoints field", e);
      return 0;
    }
  }

  /**
   * Sets the variant of a mimic entity.
   */
  private static int setMimicVariant(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      Entity entity = EntityArgumentType.getEntity(context, "entity");
      String variant = StringArgumentType.getString(context, "variant");

      if (!(entity instanceof MimicEntity mimic)) {
        source.sendError(Text.literal("Target entity is not a mimic"));
        return 0;
      }

      mimic.setVariant(variant);

      // Reapply stats with new variant
      BlockPos pos = mimic.getBlockPos();
      RegistryEntry<Biome> biomeEntry = mimic.getEntityWorld().getBiome(pos);
      String biomeId = biomeEntry.getKey()
          .map(key -> key.getValue().toString())
          .orElse("minecraft:plains");

      mimic.applyScaledStats(biomeId, variant);

      source.sendFeedback(() -> Text.literal("Set mimic variant to ")
          .append(Text.literal(variant).formatted(Formatting.AQUA)), true);

      return 1;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to set variant: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Reveals a hidden mimic.
   */
  private static int revealMimic(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      Entity entity = EntityArgumentType.getEntity(context, "entity");

      if (!(entity instanceof MimicEntity mimic)) {
        source.sendError(Text.literal("Target entity is not a mimic"));
        return 0;
      }

      mimic.setRevealed(true);
      mimic.playSound(ModSounds.MIMIC_REVEAL, 1.0F, 1.0F);

      source.sendFeedback(() -> Text.literal("Revealed the mimic").formatted(Formatting.YELLOW), true);

      return 1;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to reveal mimic: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Hides a revealed mimic.
   */
  private static int hideMimic(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      Entity entity = EntityArgumentType.getEntity(context, "entity");

      if (!(entity instanceof MimicEntity mimic)) {
        source.sendError(Text.literal("Target entity is not a mimic"));
        return 0;
      }

      mimic.setRevealed(false);

      source.sendFeedback(() -> Text.literal("Hid the mimic").formatted(Formatting.YELLOW), true);

      return 1;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to hide mimic: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Spawns multiple mimics in a radius around the player.
   */
  private static int spawnManyMimics(CommandContext<ServerCommandSource> context, int count, String variant) {
    ServerCommandSource source = context.getSource();
    ServerPlayerEntity player = source.getPlayer();

    if (player == null) {
      source.sendError(Text.literal("This command must be run by a player"));
      return 0;
    }

    try {
      Vec3d centerPos = player.getEntityPos();
      int spawned = 0;

      for (int i = 0; i < count; i++) {
        // Spawn in a circle around the player
        double angle = (2 * Math.PI * i) / count;
        double radius = 5.0 + (i % 3) * 2.0; // Vary radius slightly
        double x = centerPos.x + Math.cos(angle) * radius;
        double z = centerPos.z + Math.sin(angle) * radius;
        double y = centerPos.y;

        MimicEntity mimic = new MimicEntity(ModEntities.MIMIC, player.getEntityWorld());
        mimic.refreshPositionAndAngles(x, y, z, (float) Math.toDegrees(angle), 0.0F);
        mimic.setVariant(variant);

        BlockPos blockPos = mimic.getBlockPos();
        RegistryEntry<Biome> biomeEntry = player.getEntityWorld().getBiome(blockPos);
        String biomeId = biomeEntry.getKey()
            .map(key -> key.getValue().toString())
            .orElse("minecraft:plains");

        mimic.applyScaledStats(biomeId, variant);

        if (player.getEntityWorld().spawnEntity(mimic)) {
          spawned++;
        }
      }

      final int finalSpawned = spawned;
      source.sendFeedback(() -> Text.literal("Spawned ")
          .append(Text.literal(String.valueOf(finalSpawned)).formatted(Formatting.GOLD))
          .append(" ")
          .append(Text.literal(variant).formatted(Formatting.AQUA))
          .append(" mimics"), true);

      return spawned;
    } catch (Exception e) {
      MimicMod.LOGGER.error("Failed to spawn multiple mimics", e);
      source.sendError(Text.literal("Failed to spawn mimics: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Kills all mimic entities in the world.
   */
  private static int killAllMimics(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      // Create a large bounding box for the entire world
      Box worldBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);

      Collection<MimicEntity> mimics = source.getWorld().getEntitiesByClass(
          MimicEntity.class,
          worldBox,
          entity -> true);

      int count = 0;
      for (MimicEntity mimic : mimics) {
        mimic.discard();
        count++;
      }

      final int finalCount = count;
      source.sendFeedback(() -> Text.literal("Killed ")
          .append(Text.literal(String.valueOf(finalCount)).formatted(Formatting.RED))
          .append(" mimic(s)"), true);

      return count;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to kill mimics: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Reloads the mod configuration.
   */
  private static int reloadConfig(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    try {
      MimicMod.CONFIG = com.mimicmod.config.MimicConfig.load();
      source.sendFeedback(() -> Text.literal("Configuration reloaded successfully")
          .formatted(Formatting.GREEN), true);
      return 1;
    } catch (Exception e) {
      MimicMod.LOGGER.error("Failed to reload config", e);
      source.sendError(Text.literal("Failed to reload config: " + e.getMessage()));
      return 0;
    }
  }

  /**
   * Gets information about the current biome.
   */
  private static int getBiomeInfo(CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();
    ServerPlayerEntity player = source.getPlayer();

    if (player == null) {
      source.sendError(Text.literal("This command must be run by a player"));
      return 0;
    }

    try {
      BlockPos pos = player.getBlockPos();
      RegistryEntry<Biome> biomeEntry = player.getEntityWorld().getBiome(pos);
      String biomeId = biomeEntry.getKey()
          .map(key -> key.getValue().toString())
          .orElse("unknown");

      double spawnChance = MimicMod.CONFIG.getBiomeWeight(biomeId);

      source.sendFeedback(() -> Text.literal("=== Biome Information ===").formatted(Formatting.GOLD), false);
      source.sendFeedback(() -> Text.literal("Biome: ").formatted(Formatting.GRAY)
          .append(Text.literal(biomeId).formatted(Formatting.GREEN)), false);
      source.sendFeedback(() -> Text.literal("Mimic Spawn Weight: ").formatted(Formatting.GRAY)
          .append(Text.literal(String.format("%.2f", spawnChance)).formatted(Formatting.YELLOW)), false);

      return 1;
    } catch (Exception e) {
      source.sendError(Text.literal("Failed to get biome info: " + e.getMessage()));
      return 0;
    }
  }
}
