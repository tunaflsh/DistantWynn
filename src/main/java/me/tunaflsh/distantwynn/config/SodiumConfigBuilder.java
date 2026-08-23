package me.tunaflsh.distantwynn.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.tunaflsh.distantwynn.DistantWynn;
import me.tunaflsh.distantwynn.core.WynnRegionTracker;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SodiumConfigBuilder implements ConfigEntryPoint {
	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		var debugPage = builder.createOptionPage()
				.setName(Component.literal("Debug"))
				.addOption(builder.createBooleanOption(DistantWynn.id("wynn_tracker_enabled"))
						.setName(Component.literal("Predefined Region Check"))
						.setTooltip(Component.literal("Check against the predefined list of regions: "
								+ Arrays.stream(WynnRegionTracker.Region.values())
										.map(Enum::name)
										.collect(Collectors.joining(", "))))
						.setStorageHandler(DistantWynnConfig.HANDLER::save)
						.setBinding(value -> { DistantWynnConfig.wynnTrackerEnabled = value; },
								() -> DistantWynnConfig.wynnTrackerEnabled)
						.setDefaultValue(false))
				.addOption(builder.createBooleanOption(DistantWynn.id("voxy_tracker_enabled"))
						.setName(Component.literal("Voxy Region Detection"))
						.setTooltip(Component.literal("Detect regions using Voxy's LOD data"))
						.setStorageHandler(DistantWynnConfig.HANDLER::save)
						.setBinding(value -> { DistantWynnConfig.voxyTrackerEnabled = value; },
								() -> DistantWynnConfig.voxyTrackerEnabled)
						.setDefaultValue(false)
						.setEnabled(MixinConfigPlugin.hasVoxy()));

		builder.registerOwnModOptions()
				.setIcon(Identifier.parse("distantwynn:icon_monochrome.png"))
				.addPage(debugPage);
	}
}
