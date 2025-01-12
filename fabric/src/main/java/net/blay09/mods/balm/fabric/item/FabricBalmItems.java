package net.blay09.mods.balm.fabric.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class FabricBalmItems implements BalmItems {

    private final Set<ResourceLocation> managedCreativeTabs = Collections.synchronizedSet(new HashSet<>());
    private final Multimap<ResourceLocation, ItemLike> creativeTabContents = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

    @Override
    public Item.Properties itemProperties() {
        return new FabricItemSettings();
    }

    @Override
    public DeferredObject<Item> registerItem(Supplier<Item> supplier, ResourceLocation identifier, @Nullable ResourceLocation creativeTab) {
        return new DeferredObject<>(identifier, () -> {
            Item item = supplier.get();
            item = Registry.register(BuiltInRegistries.ITEM, identifier, item);
            if (creativeTab != null) {
                manageCreativeModeTab(creativeTab);
                creativeTabContents.put(creativeTab, item);
            }
            return item;
        }).resolveImmediately();
    }

    @Override
    public DeferredObject<CreativeModeTab> registerCreativeModeTab(Supplier<ItemStack> iconSupplier, ResourceLocation identifier) {
        return new DeferredObject<>(identifier, () -> {
            Component displayName = Component.translatable("itemGroup." + identifier.toString().replace(':', '.'));
            CreativeModeTab creativeModeTab = FabricItemGroup.builder()
                    .title(displayName)
                    .icon(iconSupplier)
                    .build();
            creativeModeTab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, identifier, creativeModeTab);
            return creativeModeTab;
        }).resolveImmediately();
    }

    @Override
    public void addToCreativeModeTab(ResourceLocation tabIdentifier, Supplier<ItemLike[]> itemsSupplier) {
        manageCreativeModeTab(tabIdentifier);
        for (ItemLike itemStack : itemsSupplier.get()) {
            creativeTabContents.put(tabIdentifier, itemStack);
        }
    }

    private void manageCreativeModeTab(ResourceLocation creativeTab) {
        if (!managedCreativeTabs.contains(creativeTab)) {
            ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, creativeTab)).register(entries -> {
                Collection<ItemLike> itemStacks = creativeTabContents.get(creativeTab);
                synchronized (creativeTabContents) {
                    itemStacks.forEach(entries::accept);
                }
            });
            managedCreativeTabs.add(creativeTab);
        }
    }
}
