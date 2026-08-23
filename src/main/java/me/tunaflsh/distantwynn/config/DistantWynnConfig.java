package me.tunaflsh.distantwynn.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.tunaflsh.distantwynn.DistantWynn;
import net.fabricmc.loader.api.FabricLoader;

public class DistantWynnConfig {
	public static ConfigClassHandler<DistantWynnConfig> HANDLER = ConfigClassHandler.createBuilder(DistantWynnConfig.class)
			.id(DistantWynn.id("distantwynn_config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("distantwynn.json5"))
					.setJson5(true)
					.build())
			.build();

	@SerialEntry(value = "wynn_tracker_enabled")
	public static boolean wynnTrackerEnabled = true;

	@SerialEntry(value = "voxy_tracker_enabled")
	public static boolean voxyTrackerEnabled = true;
}
