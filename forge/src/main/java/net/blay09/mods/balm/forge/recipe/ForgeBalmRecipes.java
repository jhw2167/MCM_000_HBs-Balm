package net.blay09.mods.balm.forge.recipe;

import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.recipe.BalmRecipes;
import net.blay09.mods.balm.forge.DeferredRegisters;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ForgeBalmRecipes implements BalmRecipes {
    @Override
    public <T extends Recipe<?>> DeferredObject<RecipeType<T>> registerRecipeType(Supplier<RecipeType<T>> typeSupplier, Supplier<RecipeSerializer<T>> serializerSupplier, ResourceLocation identifier) {
        DeferredRegister<RecipeSerializer<?>> serializerRegister = DeferredRegisters.get(ForgeRegistries.RECIPE_SERIALIZERS, identifier.getNamespace());
        RegistryObject<RecipeSerializer<T>> serializerObject = serializerRegister.register(identifier.getPath(), serializerSupplier);

        DeferredRegister<RecipeType<?>> typeRegister = DeferredRegisters.get(ForgeRegistries.RECIPE_TYPES, identifier.getNamespace());
        RegistryObject<RecipeType<T>> typeObject = typeRegister.register(identifier.getPath(), typeSupplier);

        return new DeferredObject<>(identifier, typeObject, () -> typeObject.isPresent() && serializerObject.isPresent());
    }

}
