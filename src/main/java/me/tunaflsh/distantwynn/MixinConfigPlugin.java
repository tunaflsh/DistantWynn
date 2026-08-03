package me.tunaflsh.distantwynn;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = DistantWynn.LOGGER;
	private final boolean hasSodium = FabricLoader.getInstance().isModLoaded("sodium");
	private final boolean hasSodiumOC = hasSodium && hasClass("net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller");

	private static boolean hasClass(String name) {
		try {
			MixinService.getService().getBytecodeProvider().getClassNode(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (Exception e) {
			LOGGER.error("Unexpected exception checking whether class exists:", e);
			return false;
		}
	}

	{
		if (hasSodium && !hasSodiumOC)
			LOGGER.error("Sodium version is incompatible.");
	}

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() { return null; }

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.contains("Sodium"))
			return hasSodiumOC;
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
