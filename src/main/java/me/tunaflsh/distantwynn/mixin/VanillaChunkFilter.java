package me.tunaflsh.distantwynn.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.tunaflsh.distantwynn.util.WynnRegions;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockBox;

@Mixin(SectionOcclusionGraph.class)
public class VanillaChunkFilter {
	@Inject(
	method = "method_62924",
	at = @At(
		value = "INVOKE_ASSIGN",
		target = "Lnet/minecraft/client/renderer/Octree$Node;getSection()Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"),
	locals = LocalCapture.CAPTURE_FAILSOFT,
	cancellable = true)
	private static void onAddSectionsInFrustum(
			final List<SectionRenderDispatcher.RenderSection> visibleSections,
			final List<SectionRenderDispatcher.RenderSection> nearbyVisibleSections,
			final Octree.Node node,
			final boolean shouldRenderSection,
			final int depth,
			final boolean shouldRenderNearbySection,
			final CallbackInfo callbackInfo,
			final SectionRenderDispatcher.RenderSection renderSection) {
		final BlockBox currentRegion = WynnRegions.getCurrent();
		if (currentRegion == null || renderSection == null) return;
		if (currentRegion.contains(renderSection.getRenderOrigin())) return;
		callbackInfo.cancel();
	}
}
