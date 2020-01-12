package com.reallysimpletanks.datagen;

import com.reallysimpletanks.blocks.ModBlocks;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(ModBlocks.BASICTANK)
                .patternLine("I#I")
                .patternLine("BGB")
                .patternLine("I#I")
                .key('I', Items.IRON_INGOT)
                .key('#', Items.IRON_BARS)
                .key('B', Items.BUCKET)
                .key('G', Blocks.GLASS)
                .setGroup("reallysimpletanks")
                .addCriterion("iron_ingot", InventoryChangeTrigger.Instance.forItems(Items.IRON_INGOT))
                .build(consumer);
    }
}