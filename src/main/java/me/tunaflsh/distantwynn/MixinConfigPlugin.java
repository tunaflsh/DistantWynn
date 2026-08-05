package me.tunaflsh.distantwynn;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.loader.api.FabricLoader;

public class MixinConfigPlugin implements IMixinConfigPlugin {
	static boolean hasSodium = false;
	static boolean hasVoxy = false;

	public MixinConfigPlugin() {
		FabricLoader instance = FabricLoader.getInstance();

		hasSodium = instance.isModLoaded("sodium");
		boolean hasSodiumOcclusionCuller = hasSodium && hasClass("net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller");
		if (hasSodium && !hasSodiumOcclusionCuller)
			DistantWynn.LOGGER.error("Sodium version is incompatible.");
		hasSodium = hasSodiumOcclusionCuller;

		hasVoxy = instance.isModLoaded("voxy");
		boolean hasVoxyRenderSystem = hasVoxy && hasClass("me.cortex.voxy.client.core.VoxyRenderSystem");
		boolean hasVoxyTraverser = hasVoxy && hasClass("me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser");
		if (hasVoxy && !(hasVoxyRenderSystem && hasVoxyTraverser))
			DistantWynn.LOGGER.error("Voxy version is incompatible.");
		hasVoxy = hasVoxyRenderSystem && hasVoxyTraverser;
	}

	private static boolean hasClass(String name) {
		try {
			MixinService.getService().getBytecodeProvider().getClassNode(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (Exception e) {
			DistantWynn.LOGGER.error("Unexpected exception checking whether class exists:", e);
			return false;
		}
	}

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() { return null; }

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		DistantWynn.LOGGER.debug(mixinClassName);
		if (mixinClassName.contains("sodium"))
			return hasSodium;
		if (mixinClassName.contains("voxy"))
			return hasVoxy;
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() { return null; }

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
