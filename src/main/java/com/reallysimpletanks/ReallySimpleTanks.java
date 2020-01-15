package com.reallysimpletanks;

import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.blocks.*;
import com.reallysimpletanks.items.ExcessUpgradeItem;
import com.reallysimpletanks.setup.ClientProxy;
import com.reallysimpletanks.setup.IProxy;
import com.reallysimpletanks.setup.ModSetup;
import com.reallysimpletanks.setup.ServerProxy;
import com.reallysimpletanks.utils.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ReallySimpleTanks.MODID)
public class ReallySimpleTanks {
    public static final String MODID = "reallysimpletanks";

    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    public static final Logger LOGGER = LogManager.getLogger();

    public static ModSetup setup = new ModSetup();

    public ReallySimpleTanks() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)    {
        setup.init();
        proxy.init();
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new BasicTankBlock().setRegistryName("basictank"));
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new BlockItem(ModBlocks.BASICTANK, new Item.Properties().maxStackSize(1).group(ItemGroup.MISC)).setRegistryName("basictank"));
            event.getRegistry().register(new ExcessUpgradeItem());
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(TileEntityType.Builder.create(BasicTankTileEntity::new, ModBlocks.BASICTANK).build(null).setRegistryName("basictank"));
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(IForgeContainerType.create((windowID, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                TankMode tankMode = EnumUtils.byOrdinal(data.readByte(), TankMode.NORMAL);
                return new BasicTankContainer(windowID, pos, inv, tankMode);
            }).setRegistryName("basictank"));
        }
    }
}
