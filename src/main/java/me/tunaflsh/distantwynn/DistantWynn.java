package me.tunaflsh.distantwynn;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.tunaflsh.distantwynn.config.DistantWynnConfig;
import me.tunaflsh.distantwynn.core.RegionTrackerManager;
import me.tunaflsh.distantwynn.core.VoxyRegionTracker;
import me.tunaflsh.distantwynn.core.WynnRegionTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.BlockBox;
import net.minecraft.resources.Identifier;

public class DistantWynn implements ModInitializer {
	public static final String MOD_ID = "distantwynn";
	public static final Logger LOGGER = LoggerFactory.getLogger("DistantWynn");
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static @Nullable WynnRegionTracker wynnRegionTracker; // track predefined wynn regions
	private static @Nullable VoxyRegionTracker voxyRegionTracker; // detect region using voxy LOD
	private static @Nullable BlockBox region;

	public static @Nullable BlockBox getRegion() {
		return region;
	}

	@Override
	public void onInitialize() {
		DistantWynnConfig.HANDLER.load();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!handler.getConnection().getRemoteAddress().toString().contains("wynncraft.com"))
				return;
			RegionTrackerManager.enable();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			RegionTrackerManager.disable();
		});

		ClientTickEvents.END_WORLD_TICK.register(RegionTrackerManager::updateRegion);
	}
}
