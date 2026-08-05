package me.tunaflsh.distantwynn.mixin.voxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import me.cortex.voxy.client.core.AbstractRenderPipeline;
import me.cortex.voxy.client.core.gl.GlBuffer;
import me.cortex.voxy.client.core.gl.shader.AutoBindingShader;
import me.cortex.voxy.client.core.gl.shader.Shader.Builder;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;
import me.cortex.voxy.client.core.rendering.util.UploadStream;
import me.tunaflsh.distantwynn.DistantWynn;
import me.tunaflsh.distantwynn.util.IVoxyRegionUpdater;
import me.tunaflsh.distantwynn.util.WynnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(HierarchicalOcclusionTraverser.class)
public class VoxyRegionCheck implements IVoxyRegionUpdater {
	@Shadow
	private static int BINDING_COUNTER;
	private static final int REGION_UNIFORM_BINDING = BINDING_COUNTER++;

	private final GlBuffer regionBuffer = new GlBuffer(32).zero();

	@ModifyVariable(
	method = "lateStageCompile",
	at = @At(
		value = "INVOKE_ASSIGN",
		target = "Lme/cortex/voxy/client/core/gl/shader/ShaderLoader;parse(Ljava/lang/String;)Ljava/lang/String;",
		ordinal = 0),
	name = {"scr"})
	private static String addRegionCheck(String scr) {
		final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		final Identifier regionId = DistantWynn.id("shaders/wynn/region.glsl");
		try (InputStream in = resourceManager.open(regionId)) {
			final String regionSrc = IOUtils.toString(in, StandardCharsets.UTF_8);
			scr = scr.replaceFirst("void main", regionSrc + "\n$0");
			scr = scr.replaceFirst(
					"if \\(isWithinRenderDistance\\(node\\)\\)",
					"if (!outsideRegion(node) && isWithinRenderDistance(node))");
		} catch (final IOException e) {
			DistantWynn.LOGGER.error("Failed to read shader source for {}", regionId.toString(), e);
		}
		return scr;
	}

	@ModifyExpressionValue(
	method = "lateStageCompile",
	at = @At(
		value = "INVOKE",
		target = "Lme/cortex/voxy/client/core/gl/shader/Shader$Builder;defineIf(Ljava/lang/String;Z)Lme/cortex/voxy/client/core/gl/shader/Shader$Builder;",
		ordinal = 0))
	private static Builder<AutoBindingShader> defineRegionBinding(final Builder<AutoBindingShader> builder) {
		return builder.define("REGION_UNIFORM_BINDING", REGION_UNIFORM_BINDING);
	}

	@ModifyExpressionValue(
	method = "lateStageCompile",
	at = @At(
		value = "FIELD",
		target = "Lme/cortex/voxy/client/core/rendering/hierachical/HierarchicalOcclusionTraverser;traversal:Lme/cortex/voxy/client/core/gl/shader/AutoBindingShader;",
		opcode = Opcodes.GETFIELD))
	private AutoBindingShader initRegionUniform(final AutoBindingShader traversal) {
		return traversal.ubo("REGION_UNIFORM_BINDING", this.regionBuffer);
	}

	@Inject(method = "free", at = @At("HEAD"))
	private void freeRegionBuffer(final CallbackInfo callback) {
		this.regionBuffer.free();
	}

	public void updateRegion() {
		final BlockBox current = WynnRegions.getCurrent();
		long ptr = UploadStream.INSTANCE.upload(this.regionBuffer, 0, 32);

		if (current == null) {
			ptr += 3 * 4;
			MemoryUtil.memPutInt(ptr, 0); // min.w == 0 for current == null
		} else {
			final BlockPos min = current.min();
			final BlockPos max = current.max();
			MemoryUtil.memPutInt(ptr, min.getX()); ptr += 4;
			MemoryUtil.memPutInt(ptr, min.getY()); ptr += 4;
			MemoryUtil.memPutInt(ptr, min.getZ()); ptr += 4;
			MemoryUtil.memPutInt(ptr, 1); ptr += 4; // min.w == 1 for current != null
			MemoryUtil.memPutInt(ptr, max.getX()); ptr += 4;
			MemoryUtil.memPutInt(ptr, max.getY()); ptr += 4;
			MemoryUtil.memPutInt(ptr, max.getZ()); ptr += 4;
		}
	}

	@Inject(method = "lateStageCompile", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private static void captureScr(
			final AbstractRenderPipeline pipeline, final CallbackInfo callback,
			final String taa, final String scr) {
		DistantWynn.LOGGER.debug("Final traversal_dev.comp:\n{}", scr);
	}
}
