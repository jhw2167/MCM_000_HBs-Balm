package net.blay09.mods.balm.mixin;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ImageButton.class)
public interface ImageButtonAccessor {
    @Accessor
    ResourceLocation getResourceLocation();
}
