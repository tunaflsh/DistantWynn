package me.tunaflsh.distantwynn.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;

public interface IRegionTracker {
	/**
	 * @return The current region.
	 */
	default @Nullable BlockBox getRegion() {
		return null;
	}

	/**
	 * @return Whether the region was changed.
	 */
	default boolean updateRegion() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;

		BlockPos pos = player.blockPosition();
		BlockBox region = getRegion();
		return region != null && !region.contains(pos);
	}
}
