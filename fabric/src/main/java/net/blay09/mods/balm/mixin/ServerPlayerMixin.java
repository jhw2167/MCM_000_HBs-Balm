package net.blay09.mods.balm.mixin;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.PlayerChangedDimensionEvent;
import net.blay09.mods.balm.api.event.PlayerOpenMenuEvent;
import net.blay09.mods.balm.api.event.TossItemEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    private static final ThreadLocal<ResourceKey<Level>> fromDimHolder = new ThreadLocal<>();

    @Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At("RETURN"))
    public void openMenu(@Nullable MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> callbackInfo) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        Balm.getEvents().fireEvent(new PlayerOpenMenuEvent(player, player.containerMenu));
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
    public void changeDimensionHead(ServerLevel level, CallbackInfoReturnable<Entity> callbackInfo) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        fromDimHolder.set(player.level().dimension());
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"))
    public void changeDimensionTail(ServerLevel level, CallbackInfoReturnable<Entity> callbackInfo) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        final ResourceKey<Level> fromDim = fromDimHolder.get();
        final ResourceKey<Level> toDim = level.dimension();
        Balm.getEvents().fireEvent(new PlayerChangedDimensionEvent(player, fromDim, toDim));
    }

    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    public void drop(boolean flag, CallbackInfoReturnable<Boolean> callbackInfo) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        Inventory inventory = player.getInventory();
        ItemStack selected = inventory.getSelected();
        TossItemEvent event = new TossItemEvent(player, selected);
        Balm.getEvents().fireEvent(event);
        if (event.isCanceled()) {
            callbackInfo.setReturnValue(false);
        }
    }
}
