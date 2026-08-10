package me.tunaflsh.distantwynn.mixin.voxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
import me.tunaflsh.distantwynn.util.IVoxyRegionCuller;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(HierarchicalOcclusionTraverser.class)
public class VoxyRegionCuller implements IVoxyRegionCuller {
	@Shadow
	private static int BINDING_COUNTER;
	private static final int REGION_UNIFORM_BINDING = BINDING_COUNTER++;

	private final GlBuffer regionBuffer = new GlBuffer(64).zero();

	@ModifyExpressionValue(
	method = "lateStageCompile",
	at = @At(
		value = "INVOKE",
		target = "Lme/cortex/voxy/client/core/gl/shader/ShaderLoader;parse(Ljava/lang/String;)Ljava/lang/String;",
		ordinal = 0))
	private static String addRegionCheck(String scr) {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		Identifier regionId = DistantWynn.id("shaders/wynn/region.glsl");
		try (InputStream in = resourceManager.open(regionId)) {
			String regionSrc = IOUtils.toString(in, StandardCharsets.UTF_8);
			scr = scr.replaceFirst(
					Pattern.quote("void enqueueSelfForRender(in UnpackedNode node) {"),
					regionSrc + "\n$0"
					+ "\n    if (outsideRegion(node, voxyRegion)) return;"
					);
			scr = scr.replaceFirst(
					Pattern.quote("if (isWithinRenderDistance(node))"),
					"if (!outsideRegion(node, wynnRegion) && isWithinRenderDistance(node))");
		} catch (IOException e) {
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
	private static Builder<AutoBindingShader> defineRegionBinding(Builder<AutoBindingShader> builder) {
		return builder.define("REGION_UNIFORM_BINDING", REGION_UNIFORM_BINDING);
	}

	@ModifyExpressionValue(
	method = "lateStageCompile",
	at = @At(
		value = "FIELD",
		target = "Lme/cortex/voxy/client/core/rendering/hierachical/HierarchicalOcclusionTraverser;traversal:Lme/cortex/voxy/client/core/gl/shader/AutoBindingShader;",
		opcode = Opcodes.GETFIELD))
	private AutoBindingShader bindRegionBuffer(AutoBindingShader traversal) {
		return traversal.ubo("REGION_UNIFORM_BINDING", this.regionBuffer);
	}

	@Inject(method = "lateStageCompile", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void initRegionUniform(
			AbstractRenderPipeline pipeline, CallbackInfo ci,
			String taa, String scr) {
		setWynnRegion(DistantWynn.getRegion());
		DistantWynn.LOGGER.debug("Final traversal_dev.comp:\n{}", scr);
	}

	@Inject(method = "free", at = @At("HEAD"))
	private void freeRegionBuffer(CallbackInfo ci) {
		this.regionBuffer.free();
	}

	@Override
	public void setWynnRegion(BlockBox region) {
		setRegion(region, 0);
		setRegion(null, 32);
	}

	@Override
	public void setVoxyRegion(BlockBox region) {
		setRegion(null, 0);
		setRegion(region, 32);
	}

	private void setRegion(BlockBox region, long offset) {
		long ptr = UploadStream.INSTANCE.upload(this.regionBuffer, offset, 32);

		if (region == null) {
			MemoryUtil.memPutInt(ptr + 3 * 4, 0); // min.w == 0
		} else {
			BlockPos min = region.min();
			BlockPos max = region.max();
			MemoryUtil.memPutInt(ptr, min.getX()); ptr += 4;
			MemoryUtil.memPutInt(ptr, min.getY()); ptr += 4;
			MemoryUtil.memPutInt(ptr, min.getZ()); ptr += 4;
			MemoryUtil.memPutInt(ptr, 1); ptr += 4; // min.w == 1
			MemoryUtil.memPutInt(ptr, max.getX()); ptr += 4;
			MemoryUtil.memPutInt(ptr, max.getY()); ptr += 4;
			MemoryUtil.memPutInt(ptr, max.getZ()); ptr += 4;
		}
	}
}
