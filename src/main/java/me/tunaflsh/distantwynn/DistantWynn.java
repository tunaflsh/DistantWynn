package me.tunaflsh.distantwynn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.cortex.voxy.common.world.WorldEngine;
import me.tunaflsh.distantwynn.mixin.voxy.LevelRendererAccessor;
import me.tunaflsh.distantwynn.mixin.voxy.VoxyRenderSystemAccessor;
import me.tunaflsh.distantwynn.util.IRegionTracker;
import me.tunaflsh.distantwynn.util.IRegionTracker.Status;
import me.tunaflsh.distantwynn.util.IVoxyRegionCuller;
import me.tunaflsh.distantwynn.util.NullRegionTracker;
import me.tunaflsh.distantwynn.util.VoxyRegionTracker;
import me.tunaflsh.distantwynn.util.WynnRegionTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;

public class DistantWynn implements ModInitializer {
	public static final String MOD_ID = "distantwynn";
	public static final Logger LOGGER = LoggerFactory.getLogger("DistantWynn");
	private static final int INTERVAL = 7;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static NullRegionTracker nullRegionTracker = new NullRegionTracker();
	private static WynnRegionTracker wynnRegionTracker; // track pre-defined wynn regions
	private static VoxyRegionTracker voxyRegionTracker; // dynamically compute region boundaries
	private static IRegionTracker regionTracker = nullRegionTracker;

	public static IRegionTracker getRegionTracker() {
		return regionTracker;
	}

	private int tick = 0;
	private boolean disableWynnTracker = false;
	private boolean disableVoxyTracker = false;

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!handler.getConnection().getRemoteAddress().toString().contains("wynncraft.com"))
				return;
			if (wynnRegionTracker == null)
				wynnRegionTracker = new WynnRegionTracker();
			if (disableWynnTracker)
				wynnRegionTracker.disable();
			regionTracker = wynnRegionTracker;
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			regionTracker = nullRegionTracker;
			voxyRegionTracker = null;
		});

		ClientTickEvents.START_WORLD_TICK.register(world -> {
			if (regionTracker == nullRegionTracker) return;
			if (0 != (tick = (tick + 1) % INTERVAL)) return; // check every Nth tick

			var status = regionTracker.updateRegion();
			if (status == Status.UNCHANGED) return;

			if (!disableWynnTracker && regionTracker != wynnRegionTracker) {
				status = wynnRegionTracker.updateRegion();
				if (status == Status.CHANGED)
					regionTracker = wynnRegionTracker;
			}
			var region = regionTracker.getRegion();

			if (region != null) {
				LOGGER.info("Region updated: {}", regionTracker);
				LOGGER.info("{}x{}x{}", region.sizeX(), region.sizeY(), region.sizeZ());
				LOGGER.info("{}", region);
			}

			if (MixinConfigPlugin.hasVoxy) {
				var worldAccessor = (LevelRendererAccessor) world;
				var levelRenderer = (IGetVoxyRenderSystem) worldAccessor.distantwynn$getLevelRenderer();
				var renderer = (VoxyRenderSystemAccessor) levelRenderer.voxy$getRenderSystem();
				var culler = (IVoxyRegionCuller) renderer.distantwynn$getTraversal();

				switch (regionTracker) {
					case WynnRegionTracker w -> {
						culler.setWynnRegion(region);
						if (disableVoxyTracker || region != null) return;
						if (voxyRegionTracker == null) {
							WorldEngine engine = ((VoxyRenderSystem) renderer).getEngine();
							voxyRegionTracker = new VoxyRegionTracker(engine);
						}
						regionTracker = voxyRegionTracker;
					}
					case VoxyRegionTracker v -> {
						culler.setVoxyRegion(disableVoxyTracker ? null : region);
					}
					default -> {}
				}
			}
		});
	}
}
