package com.mimicmod;

import com.mimicmod.entity.client.MimicEntityModel;
import com.mimicmod.entity.client.MimicEntityRenderer;
import com.mimicmod.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.EntityRendererFactories;

/**
 * Client-side initialization for Mimic Mod.
 * Handles all client-only registrations including renderers and model layers.
 */
public class MimicModClient implements ClientModInitializer {

	private static final long START_TIME = System.currentTimeMillis();

	@Override
	public void onInitializeClient() {
		MimicMod.LOGGER.info("Initializing Mimic Mod client...");

		// Register entity model layer
		registerModelLayers();

		// Register entity renderers
		registerRenderers();

		long duration = System.currentTimeMillis() - START_TIME;
		MimicMod.LOGGER.info("Mimic Mod client initialized successfully in {}ms", duration);
	}

	/**
	 * Registers all entity model layers.
	 */
	private void registerModelLayers() {
		EntityModelLayerRegistry.registerModelLayer(
				MimicEntityModel.LAYER_LOCATION,
				MimicEntityModel::getTexturedModelData);
	}

	/**
	 * Registers all entity renderers.
	 */
	private void registerRenderers() {
		EntityRendererFactories.register(
				ModEntities.MIMIC,
				MimicEntityRenderer::new);
	}
}
