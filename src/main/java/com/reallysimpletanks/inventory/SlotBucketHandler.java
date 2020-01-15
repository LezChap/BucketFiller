package com.reallysimpletanks.inventory;

import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotBucketHandler extends SlotItemHandler {
    public SlotBucketHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof BucketItem)) {
            return false;
        } else if (item == Items.BUCKET) return false;
        return super.isItemValid(stack);
    }
}
