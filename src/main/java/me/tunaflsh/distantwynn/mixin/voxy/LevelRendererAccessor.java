package me.tunaflsh.distantwynn.mixin.voxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(ClientLevel.class)
public interface LevelRendererAccessor {
	@Accessor("levelRenderer")
	LevelRenderer distantwynn$getLevelRenderer();
}
