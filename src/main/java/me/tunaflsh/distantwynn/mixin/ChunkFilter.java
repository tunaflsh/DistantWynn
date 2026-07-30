package me.tunaflsh.distantwynn.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import me.tunaflsh.distantwynn.api.ChunkExtractor;
import me.tunaflsh.distantwynn.util.WynnRegions;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockBox;

@Mixin(SectionOcclusionGraph.class)
@Implements(@Interface(iface = ChunkExtractor.class, prefix = "extractor$"))
public abstract class ChunkFilter {
	@Shadow(prefix = "original$")
	public abstract void original$addSectionsInFrustum(
			Frustum frustum,
			List<SectionRenderDispatcher.RenderSection> list,
			List<SectionRenderDispatcher.RenderSection> list2);

	// @Inject(method = "addSectionsInFrustum", at = @At("HEAD"))
	// private void init(CallbackInfo info) {
	// }

	@Intrinsic(displace = true)
	public void extractor$addSectionsInFrustum(
			Frustum frustum,
			List<SectionRenderDispatcher.RenderSection> list,
			List<SectionRenderDispatcher.RenderSection> list2) {
		this.original$addSectionsInFrustum(frustum, list, list2);

		BlockBox currentRegion = WynnRegions.getCurrent();
		if (currentRegion != null) {
			list.removeIf((section) -> !currentRegion.contains(section.getRenderOrigin()));
			list2.removeIf((section) -> !currentRegion.contains(section.getRenderOrigin()));
		}
	}
}
