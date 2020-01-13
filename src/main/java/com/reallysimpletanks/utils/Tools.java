package com.reallysimpletanks.utils;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class Tools {

    public static ITextComponent formatFluid(FluidStack stack, int maxCapacity) {
        String s1 = String.format("%,d", stack.getAmount());
        String s2 = String.format("%,d", maxCapacity);
        ITextComponent text;
        if (stack.isEmpty()) {
            text = new TranslationTextComponent("%s / %s mB", s1, s2);
        } else {
            ITextComponent fluidName = stack.getDisplayName();
            text = new TranslationTextComponent("%s: %s / %s mB", fluidName, s1, s2);
        }
        return text;
    }
}
