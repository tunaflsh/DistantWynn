package me.tunaflsh.distantwynn.core;

import org.jspecify.annotations.Nullable;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.tunaflsh.distantwynn.DistantWynn;
import me.tunaflsh.distantwynn.config.DistantWynnConfig;
import me.tunaflsh.distantwynn.config.MixinConfigPlugin;
import me.tunaflsh.distantwynn.mixin.voxy.LevelRendererAccessor;
import me.tunaflsh.distantwynn.mixin.voxy.VoxyRenderSystemAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockBox;

public class RegionTrackerManager {
	private static @Nullable BlockBox region;

	public static @Nullable BlockBox getRegion() {
		return region;
	}

	private enum Tracker {
		WYNN, // highest priority
		VOXY,
		NULL  // lowest priority
	}

	private static final WynnRegionTracker WYNN_TRACKER = WynnRegionTracker.getInstance();
	private static final VoxyRegionTracker VOXY_TRACKER = VoxyRegionTracker.getInstance();
	private static Tracker tracker = Tracker.NULL;
	private static boolean enabled = false;
	private static VoxyRenderSystem voxyRenderer;

	public static void enable() {
		enabled = true;
	}

	public static void disable() {
		enabled = false;
		tracker = Tracker.NULL;
	}

	public static void updateRegion(ClientLevel world) {
		if (!enabled) return;

		boolean updated = updateTrackerAndRegion(Tracker.WYNN, world);

		if (MixinConfigPlugin.hasVoxy()) {
			updated |= updateVoxyRenderer(world);

			if (voxyRenderer != null) {
				updated |= updateTrackerAndRegion(Tracker.VOXY, world);

				if (updated) {
					var culler = (IVoxyRegionCuller) ((VoxyRenderSystemAccessor) voxyRenderer).distantwynn$getTraversal();

					switch (tracker) {
						case WYNN -> culler.setWynnRegion(region);
						case VOXY -> culler.setVoxyRegion(region);
						case NULL -> { culler.setWynnRegion(null); culler.setVoxyRegion(null); }
					}
				}
			}
		}

		if (updated) {
			DistantWynn.LOGGER.debug("Region Updated");
			DistantWynn.LOGGER.debug("Tracker {}", tracker);
			if (Tracker.WYNN == tracker)
				DistantWynn.LOGGER.debug("Region Name {}", WYNN_TRACKER.getRegionName());
			if (null != region)
				DistantWynn.LOGGER.debug("Size {}x{}x{}", region.sizeX(), region.sizeY(), region.sizeZ());
			DistantWynn.LOGGER.debug("Box {}", region);
		}
	}

	/**
	 * Change the tracker to a better one if applicable, and update the region
	 *
	 * NULL, VOXY -> WYNN
	 * NULL -> VOXY
	 *
	 * @return true if the tracker or region changed
	 */
	private static boolean updateTrackerAndRegion(Tracker TRACKER, ClientLevel world) {
		boolean updated = false;
		boolean enabled = switch (TRACKER) {
			case WYNN -> DistantWynnConfig.wynnTrackerEnabled;
			case VOXY -> DistantWynnConfig.voxyTrackerEnabled;
			case NULL -> false;
		};

		// only lower priority trackers can be changed to higher priority trackers
		if (tracker.compareTo(TRACKER) > 0 && enabled) {
			tracker = TRACKER;
			updated = true;
		}

		if (tracker == TRACKER)
			if (enabled) {
				switch (TRACKER) {
					case WYNN -> {
						updated |= WYNN_TRACKER.updateRegion();
						region = WYNN_TRACKER.getRegion();
					}
					case VOXY -> {
						VOXY_TRACKER.updateWorld(voxyRenderer.getEngine());
						updated |= VOXY_TRACKER.updateRegion();
						region = VOXY_TRACKER.getRegion();
					}
					case NULL -> {}
				}

				if (region == null)
					tracker = Tracker.NULL;
			} else {
				tracker = Tracker.NULL;
				updated = true;
			}

		return updated;
	}

	private static boolean updateVoxyRenderer(ClientLevel world) {
		var oldRenderer = voxyRenderer;
		var worldAccessor = (LevelRendererAccessor) world;
		var levelRenderer = (IGetVoxyRenderSystem) worldAccessor.distantwynn$getLevelRenderer();
		voxyRenderer = levelRenderer.voxy$getRenderSystem();

		return oldRenderer != voxyRenderer;
	}
}
