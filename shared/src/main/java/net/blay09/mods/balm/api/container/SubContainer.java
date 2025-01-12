package net.blay09.mods.balm.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SubContainer implements Container, ExtractionAwareContainer {
    private final Container container;
    private final int minSlot;
    private final int maxSlot;

    public SubContainer(Container container, int minSlot, int maxSlot) {
        this.container = container;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
    }

    @Override
    public int getContainerSize() {
        return maxSlot - minSlot;
    }

    @Override
    public ItemStack getItem(int slot) {
        return containsSlot(slot) ? container.getItem(slot + minSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return containsSlot(slot) ? container.removeItem(slot + minSlot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return containsSlot(slot) ? container.removeItemNoUpdate(slot + minSlot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (containsSlot(slot)) {
            container.setItem(slot + minSlot, itemStack);
        }
    }

    @Override
    public void startOpen(Player player) {
        container.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        container.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return containsSlot(slot) && container.canPlaceItem(slot + minSlot, itemStack);
    }

    @Override
    public boolean isEmpty() {
        for (int i = minSlot; i < maxSlot; i++) {
            if (!container.getItem(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public int getMaxStackSize() {
        return container.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        container.setChanged();
    }

    /**
     * Will be made private. Use containsOuterSlot with an absolute slot number instead because this method probably never did what you thought it did.
     */
    @Deprecated(since = "1.20")
    public boolean containsSlot(int slot) {
        return slot + minSlot < maxSlot;
    }

    public boolean containsOuterSlot(int slot) {
        return slot >= minSlot && slot < maxSlot;
    }

    @Override
    public void clearContent() {
        for (int i = minSlot; i < maxSlot; i++) {
            container.setItem(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canExtractItem(int slot) {
        if (container instanceof ExtractionAwareContainer extractionAwareContainer) {
            return containsSlot(slot) && extractionAwareContainer.canExtractItem(slot + minSlot);
        }
        return containsSlot(slot);
    }
}
