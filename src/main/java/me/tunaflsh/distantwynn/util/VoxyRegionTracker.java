package me.tunaflsh.distantwynn.util;

import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public final class VoxyRegionTracker implements IRegionTracker {
	private @Nullable BlockBox region;
	private final WorldEngine worldIn;
	private final RandomSource random;

	private static final int NORTH = 0; // -z
	private static final int SOUTH = 1; // +z
	private static final int EAST = 2; // +x
	private static final int WEST = 3; // -x

	private static final int TRIALS = 5;
	private static final int DEPTH = 50;

	public VoxyRegionTracker(WorldEngine worldIn) {
		this.worldIn = worldIn;
		random = RandomSource.create();
	}

	@Override
	public @Nullable BlockBox getRegion() {
		return region;
	}

	@Override
	public Status updateRegion() {
		if (!worldIn.isLive())
			return Status.UNDEFINED;

		if (Status.CHANGED == IRegionTracker.super.updateRegion()) {
			region = null;
			return Status.CHANGED;
		}

		Status status = Status.UNCHANGED;

		if (region == null) {
			var origin = Minecraft.getInstance().player.blockPosition();
			int x = origin.getX() >> 5;
			int z = origin.getZ() >> 5;
			region = new BlockBox(
					new BlockPos(x << 5, -64, z << 5),
					new BlockPos(x + 1 << 5, 319, z + 1 << 5));
			status = Status.CHANGED;
		}

		for (int side : new int[] {NORTH, SOUTH, EAST, WEST}) {
			for (int i = 0; i < TRIALS; i++) {
				var min = region.min();
				var max = region.max();
				int minX = min.getX() >> 5;
				int minZ = min.getZ() >> 5;
				int maxX = max.getX() >> 5;
				int maxZ = max.getZ() >> 5;
				int x, y = random.nextIntBetweenInclusive(-64 >> 5, 319 >> 5), z;
				switch (side) {
					case NORTH -> {
						x = minX - 1;
						z = random.nextIntBetweenInclusive(minZ, maxZ + 1);
					}
					case SOUTH -> {
						x = maxX + 1;
						z = random.nextIntBetweenInclusive(minZ - 1, maxZ);
					}
					case EAST -> {
						x = random.nextIntBetweenInclusive(minX, maxX + 1);
						z = maxZ + 1;
					}
					case WEST -> {
						x = random.nextIntBetweenInclusive(minX - 1, maxX);
						z = minZ - 1;
					}
					default -> { continue; }
				}
				int j = 0;
				for (; tryExpand(x, y, z) && j < DEPTH; j++) {
					status = Status.CHANGED;
					switch (side) {
						case NORTH -> x--;
						case SOUTH -> x++;
						case EAST -> z++;
						case WEST -> z--;
					}
				}
				if (j > 0) break;
			}
		}

		return status;
	}

	private boolean tryExpand(int x, int y, int z) {
		WorldSection section = worldIn.acquireIfExists(0, x, y, z);
		if (section == null) return false;

		try {
			if (section.getNonEmptyBlockCount() == 0)
				return false;

			region = region
					.include(new BlockPos(x << 5, y << 5, z << 5))
					.include(new BlockPos((x << 5) + 31, (y << 5) + 31, (z << 5) + 31));
			return true;
		} finally {
			section.release();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
