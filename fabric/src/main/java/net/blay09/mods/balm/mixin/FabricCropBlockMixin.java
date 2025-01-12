package net.blay09.mods.balm.mixin;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.CustomFarmBlock;
import net.blay09.mods.balm.api.event.CropGrowEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class FabricCropBlockMixin {

    private static final ThreadLocal<BlockState> getGrowthSpeedBlockState = new ThreadLocal<>();

    @Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), cancellable = true)
    public void randomTickPreGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo) {
        CropGrowEvent.Pre event = new CropGrowEvent.Pre(level, pos, state);
        Balm.getEvents().fireEvent(event);
        if (event.isCanceled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    public void randomTickPostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo) {
        CropGrowEvent.Post event = new CropGrowEvent.Post(level, pos, state);
        Balm.getEvents().fireEvent(event);
    }



    @ModifyVariable(method = "getGrowthSpeed(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private static BlockState getGrowthSpeedCaptureLocals(BlockState state) {
        getGrowthSpeedBlockState.set(state);
        return state;
    }


    @ModifyVariable(method = "getGrowthSpeed(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"), ordinal = 1)
    private static float getGrowthSpeed(float g, Block block, BlockGetter blockGetter, BlockPos pos) {
        BlockState state = getGrowthSpeedBlockState.get();
        if (state.getBlock() instanceof CustomFarmBlock customFarmBlock) {
            if (customFarmBlock.canSustainPlant(state, blockGetter, pos, Direction.UP, block)) {
                if (customFarmBlock.isFertile(state, blockGetter, pos)) {
                    return 3f;
                } else {
                    return 1f;
                }
            }
        }

        return g;
    }

}
