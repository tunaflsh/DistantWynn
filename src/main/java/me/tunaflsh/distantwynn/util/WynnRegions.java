package me.tunaflsh.distantwynn.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;

public final class WynnRegions {
	public static final BlockBox WYNN           = new BlockBox(new BlockPos(-2512, -64, -145),  new BlockPos(1663, 319, -5776));
	public static final BlockBox REALM_OF_LIGHT = new BlockBox(new BlockPos(-1040, -64, -5793), new BlockPos(-641, 319, -6576));

	public static final Map<BlockBox, String> REGION_NAMES = Collections.unmodifiableMap(
			Arrays.stream(WynnRegions.class.getDeclaredFields())
					.filter(field -> {
						final int mod = field.getModifiers();
						return Modifier.isPublic(mod)
								&& Modifier.isStatic(mod)
								&& Modifier.isFinal(mod)
								&& field.getType() == BlockBox.class;
					})
					.collect(Collectors.toMap(
							field -> {
								try {
									return (BlockBox) field.get(null);
								} catch (IllegalArgumentException | IllegalAccessException e) {
									throw new ExceptionInInitializerError(e);
								}
							},
							Field::getName)));
	public static final BlockBox[] REGIONS = REGION_NAMES.keySet().toArray(new BlockBox[0]);

	private static boolean enabled;
	private static @Nullable Minecraft minecraft;
	private static @Nullable BlockBox current = null;

	public static @Nullable BlockBox getRegionAt(final BlockPos pos) {
		for (final BlockBox region : REGIONS)
			if (region.contains(pos))
				return region;
		return null;
	}

	public static void enable(final Minecraft client) {
		minecraft = client;
		enabled = true;
	}

	public static void disable() {
		enabled = false;
	}

	public static @Nullable BlockBox getCurrent() {
		return current;
	}

	public static String getCurrentName() {
		return current == null ? "UNDEFINED" : REGION_NAMES.get(current);
	}

	/**
	 * @return true if the region was updated.
	 */
	public static boolean updateRegion() {
		final BlockBox oldRegion = current;
		final LocalPlayer player = minecraft.player;
		current = enabled && player != null ? getRegionAt(player.blockPosition()) : null;
		return current != oldRegion;
	}
}
