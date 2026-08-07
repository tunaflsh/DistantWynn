package me.tunaflsh.distantwynn.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;

public sealed interface IRegionTracker
permits NullRegionTracker, WynnRegionTracker, VoxyRegionTracker {
	/**
	 * @return The current region.
	 */
	default @Nullable BlockBox getRegion() {
		return null;
	}

	/**
	 * @return {@link Status#CHANGED}, {@link Status#UNCHANGED}, or {@link Status#UNDEFINED}
	 */
	default Status updateRegion() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return Status.UNCHANGED;

		BlockPos pos = player.blockPosition();
		BlockBox region = getRegion();
		if (region != null)
			return region.contains(pos) ? Status.UNCHANGED : Status.CHANGED;

		return Status.UNDEFINED;
	}

	public enum Status { CHANGED, UNCHANGED, UNDEFINED }
}
