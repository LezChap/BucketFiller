package com.reallysimpletanks.blocks;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {
    @ObjectHolder("reallysimpletanks:basictank")
    public static BasicTankBlock BASICTANK;
    @ObjectHolder("reallysimpletanks:basictank")
    public static TileEntityType<BasicTankTileEntity> BASICTANK_TILEENTITY;
    @ObjectHolder("reallysimpletanks:basictank")
    public static ContainerType<BasicTankContainer> BASICTANK_CONTAINER;

}
