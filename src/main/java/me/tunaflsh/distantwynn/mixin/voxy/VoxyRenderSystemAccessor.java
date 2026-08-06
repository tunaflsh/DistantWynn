package me.tunaflsh.distantwynn.mixin.voxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.cortex.voxy.client.core.rendering.hierachical.AsyncNodeManager;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;

@Mixin(VoxyRenderSystem.class)
public interface VoxyRenderSystemAccessor {
	@Accessor("traversal")
	HierarchicalOcclusionTraverser distantwynn$getTraversal();

	@Accessor("nodeManager")
	AsyncNodeManager distantwynn$getAsyncNodeManager();
}
