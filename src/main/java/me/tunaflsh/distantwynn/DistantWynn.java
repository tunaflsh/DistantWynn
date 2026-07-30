package me.tunaflsh.distantwynn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.tunaflsh.distantwynn.util.WynnRegions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;

public class DistantWynn implements ModInitializer {
	public static final String MOD_ID = "distantwynn";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (handler.getConnection().getRemoteAddress().toString().contains("wynncraft"))
				WynnRegions.enable(client);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			WynnRegions.disable();
		});

		ClientTickEvents.START_WORLD_TICK.register(world -> {
			if (WynnRegions.updateRegion())
				LOGGER.info("Entered {}.", WynnRegions.getCurrentName());
		});
	}
}
