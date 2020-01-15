package com.reallysimpletanks.utils;

import com.reallysimpletanks.api.IUpgradeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotUpgradeItem extends SlotItemHandler {
    public SlotUpgradeItem(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IUpgradeItem) return true;
        return super.isItemValid(stack);
    }
}
