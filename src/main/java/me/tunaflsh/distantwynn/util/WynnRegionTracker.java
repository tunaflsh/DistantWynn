package me.tunaflsh.distantwynn.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;

public final class WynnRegionTracker implements IRegionTracker {
	public enum Region {
		WYNN(-2512, -145, 1663, -5776),
		REALM_OF_LIGHT(-1040, -5793, -641, -6576),
		// TODO: get complete LOD data for the Void
		// THE_VOID(new BlockBox(new BlockPos(13472, -64, -4384), new BlockPos(14335, 319, -3201))),
		;
		public final BlockBox boundary;
		Region(int x1, int z1, int x2, int z2) {
			this.boundary = new BlockBox(new BlockPos(x1, -64, z1), new BlockPos(x2, 319, z2));
		}
	}

	public static @Nullable Region getRegionAt(BlockPos pos) {
		for (Region region : Region.values())
			if (region.boundary.contains(pos))
				return region;
		return null;
	}

	private @Nullable Region region;
	private boolean disabled = false;

	public void enable() { disabled = false; }
	public void disable() { disabled = true; }

	@Override
	public @Nullable BlockBox getRegion() {
		return disabled || region == null ? null : region.boundary;
	}

	@Override
	public Status updateRegion() {
		if (disabled)
			return Status.UNDEFINED;
		if (Status.UNCHANGED == IRegionTracker.super.updateRegion())
			return Status.UNCHANGED;

		BlockPos origin = Minecraft.getInstance().player.blockPosition();
		Region oldRegion = region;
		region = getRegionAt(origin);
		if (region != oldRegion)
			return Status.CHANGED;
		else if (region != null)
			return Status.UNCHANGED;
		return Status.UNDEFINED;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + (region == null ? "" : "[" + region.name() + "]");
	}
}
