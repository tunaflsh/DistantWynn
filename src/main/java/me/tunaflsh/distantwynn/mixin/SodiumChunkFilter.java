package me.tunaflsh.distantwynn.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.tunaflsh.distantwynn.util.WynnRegions;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.BlockBox;

@Mixin(OcclusionCuller.class)
public class SodiumChunkFilter {
	@Inject(
	method = "isSectionVisible",
	at = @At("HEAD"),
	cancellable = true)
	private static void onIsSectionVisible(
			final RenderSection section,
			final Viewport viewport,
			final float maxDistance,
			final CallbackInfoReturnable<Boolean> callbackInfo) {
		final BlockBox currentRegion = WynnRegions.getCurrent();
		if (currentRegion == null) return;
		if (currentRegion.contains(section.getPosition().origin())) return;
		callbackInfo.setReturnValue(false);
	}
}
