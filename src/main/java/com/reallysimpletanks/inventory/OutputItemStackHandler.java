package com.reallysimpletanks.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class OutputItemStackHandler extends ItemStackHandler {
    private final ItemStackHandler internalSlot;

    public OutputItemStackHandler(ItemStackHandler hidden) {
        super();
        internalSlot = hidden;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return internalSlot.extractItem(slot, amount, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        internalSlot.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return internalSlot.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return internalSlot.getStackInSlot(slot);
    }
}
