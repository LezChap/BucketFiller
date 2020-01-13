package com.reallysimpletanks.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class CustomCombinedInvWrapper extends CombinedInvWrapper implements INBTSerializable<CompoundNBT> {

        public CustomCombinedInvWrapper(IItemHandlerModifiable... itemHandler) {
            super(itemHandler);
        }

        @Override
        public CompoundNBT serializeNBT() {

            ListNBT nbtTagList = new ListNBT();
            for (int i = 0; i < getSlots(); i++)
            {
                if (!getStackInSlot(i).isEmpty())
                {
                    CompoundNBT itemTag = new CompoundNBT();
                    itemTag.putInt("Slot", i);
                    //itemTag.putInt("Index", getIndexForSlot(i));
                    getStackInSlot(i).write(itemTag);
                    nbtTagList.add(itemTag);
                }
            }
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("Items", nbtTagList);
            nbt.putInt("Size", getSlots());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            //setSize(nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size") : stacks.size());
            ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++)
            {
                CompoundNBT itemTags = tagList.getCompound(i);
                int slot = itemTags.getInt("Slot");
                setStackInSlot(slot, ItemStack.read(itemTags));
            }
        }

}