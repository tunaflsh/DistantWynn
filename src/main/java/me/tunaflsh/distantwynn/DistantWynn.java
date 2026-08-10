package me.tunaflsh.distantwynn;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.tunaflsh.distantwynn.mixin.voxy.LevelRendererAccessor;
import me.tunaflsh.distantwynn.mixin.voxy.VoxyRenderSystemAccessor;
import me.tunaflsh.distantwynn.util.IVoxyRegionCuller;
import me.tunaflsh.distantwynn.util.VoxyRegionTracker;
import me.tunaflsh.distantwynn.util.WynnRegionTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.BlockBox;
import net.minecraft.resources.Identifier;

public class DistantWynn implements ModInitializer {
	public static final String MOD_ID = "distantwynn";
	public static final Logger LOGGER = LoggerFactory.getLogger("DistantWynn");
	private static final int INTERVAL = 7;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private enum RegionTracker { WYNN, VOXY }
	private static @Nullable RegionTracker regionTracker;
	private static @Nullable WynnRegionTracker wynnRegionTracker; // track pre-defined wynn regions
	private static @Nullable VoxyRegionTracker voxyRegionTracker; // dynamically compute region boundaries
	private static @Nullable BlockBox region;

	public static @Nullable BlockBox getRegion() {
		return region;
	}

	private boolean disable = true;
	private boolean disableWynnTracker = false;
	private boolean disableVoxyTracker = false;

	private int tick = 0;

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!handler.getConnection().getRemoteAddress().toString().contains("wynncraft.com"))
				return;
			disable = false;
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			disable = true;
			regionTracker = null;
		});

		ClientTickEvents.START_WORLD_TICK.register(world -> {
			if (disable) return;
			if (0 != (tick = (tick + 1) % INTERVAL)) return; // check every Nth tick

			boolean changed = false;

			if (!disableWynnTracker && null == regionTracker) {
				if (wynnRegionTracker == null)
					wynnRegionTracker = new WynnRegionTracker();
				regionTracker = RegionTracker.WYNN;
			}

			if (RegionTracker.WYNN == regionTracker) {
				changed = wynnRegionTracker.updateRegion();
				region = wynnRegionTracker.getRegion();
				if (null == region)
					regionTracker = null;
			}

			if (MixinConfigPlugin.hasVoxy) {
				if (!disableVoxyTracker && null == regionTracker) {
					if (voxyRegionTracker == null)
						voxyRegionTracker = new VoxyRegionTracker();
					regionTracker = RegionTracker.VOXY;
				}

				var worldAccessor = (LevelRendererAccessor) world;
				var levelRenderer = (IGetVoxyRenderSystem) worldAccessor.distantwynn$getLevelRenderer();
				var renderer = (VoxyRenderSystemAccessor) levelRenderer.voxy$getRenderSystem();
				var culler = (IVoxyRegionCuller) renderer.distantwynn$getTraversal();

				if (RegionTracker.VOXY == regionTracker) {
					voxyRegionTracker.updateWorld(((VoxyRenderSystem) renderer).getEngine());
					changed = voxyRegionTracker.updateRegion();
					region = voxyRegionTracker.getRegion();
					if (null == region)
						regionTracker = null;
				}

				if (changed) {
					switch (regionTracker) {
						case null -> culler.setWynnRegion(null);
						case WYNN -> culler.setWynnRegion(region);
						case VOXY -> culler.setVoxyRegion(region);
					}
				}
			}

			if (changed) {
				LOGGER.debug("Region Updated");
				LOGGER.debug("Tracker {}", regionTracker);
				if (RegionTracker.WYNN == regionTracker)
					LOGGER.debug("Region Name {}", wynnRegionTracker.getRegionName());
				if (null != region)
					LOGGER.debug("Size {}x{}x{}", region.sizeX(), region.sizeY(), region.sizeZ());
				LOGGER.debug("Box {}", region);
			}
		});
	}
}
