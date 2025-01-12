package net.blay09.mods.balm.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.client.BlockHighlightDrawEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true)
    public void renderHitOutline(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double x, double y, double z, BlockPos pos, BlockState state, CallbackInfo callbackInfo) {
        if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
            BlockHighlightDrawEvent event = new BlockHighlightDrawEvent(blockHitResult, poseStack, renderBuffers.bufferSource(), minecraft.gameRenderer.getMainCamera());
            Balm.getEvents().fireEvent(event);
            if (event.isCanceled()) {
                callbackInfo.cancel();
            }
        }
    }

}
