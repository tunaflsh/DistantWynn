package me.tunaflsh.distantwynn.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.tunaflsh.distantwynn.DistantWynn;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.BlockBox;

@Mixin(OcclusionCuller.class)
public class SodiumRegionCuller {
	@Inject(
	method = "isSectionVisible",
	at = @At("HEAD"),
	cancellable = true)
	private static void insideRegion(
			RenderSection section, Viewport viewport, float maxDistance,
			CallbackInfoReturnable<Boolean> cir) {
		BlockBox region = DistantWynn.getRegion();
		if (region == null) return;
		if (region.contains(section.getPosition().origin())) return;
		cir.setReturnValue(false);
	}
}
