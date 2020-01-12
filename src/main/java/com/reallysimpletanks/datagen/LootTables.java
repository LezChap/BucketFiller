package com.reallysimpletanks.datagen;

import com.reallysimpletanks.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModBlocks.BASICTANK, createStandardTable("basictank", ModBlocks.BASICTANK));
    }
}