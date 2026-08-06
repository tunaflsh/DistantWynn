package me.tunaflsh.distantwynn.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import me.tunaflsh.distantwynn.DistantWynn;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockBox;

@Mixin(SectionOcclusionGraph.class)
public class MinecraftRegionCuller {
	@ModifyExpressionValue(
	method = "method_62924",
	at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/Octree$Node;getSection()Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"))
	private static SectionRenderDispatcher.RenderSection insideRegion(SectionRenderDispatcher.RenderSection renderSection) {
		if (renderSection == null) return null;

		BlockBox region = DistantWynn.getRegionTracker().getRegion();
		if (region == null || region.contains(renderSection.getRenderOrigin()))
			return renderSection;
		return null;
	}
}
