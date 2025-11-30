package com.mimicmod;

import com.mimicmod.config.MimicConfig;
import com.mimicmod.registry.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Mimic Mod.
 * Handles initialization of all mod components.
 */
public class MimicMod implements ModInitializer {

	public static final String MODID = "mimicmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static MimicConfig CONFIG;

	@Override
	public void onInitialize() {
		long startTime = System.currentTimeMillis();

		LOGGER.info("Initializing Mimic Mod...");

		// Load configuration first
		CONFIG = MimicConfig.load();
		CONFIG.logConfiguration();

		// Register mod content
		ModItems.register();
		ModSounds.register();
		ModEntities.register();
		ModEntities.registerAttributes();
		ModLootTables.register();
		ModCommands.register();

		long duration = System.currentTimeMillis() - startTime;
		LOGGER.info("Mimic Mod initialized successfully in {}ms", duration);
	}
}
