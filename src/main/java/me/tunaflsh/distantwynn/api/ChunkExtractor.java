package me.tunaflsh.distantwynn.api;

import java.util.List;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;

public interface ChunkExtractor {
	public void addSectionsInFrustum(
			Frustum frustum,
			List<SectionRenderDispatcher.RenderSection> list,
			List<SectionRenderDispatcher.RenderSection> list2);
}
