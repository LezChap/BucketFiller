package com.reallysimpletanks.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class InputItemStackHandler extends ItemStackHandler {
    private final ItemStackHandler internalSlot;

    public InputItemStackHandler(ItemStackHandler hidden) {
        super();
        internalSlot = hidden;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return internalSlot.insertItem(slot, stack, simulate);
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
