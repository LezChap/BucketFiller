package com.reallysimpletanks.compat;

import com.reallysimpletanks.blocks.BasicTankTileEntity;
import com.reallysimpletanks.blocks.ModBlocks;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static mcjty.theoneprobe.api.IProbeInfo.ENDLOC;
import static mcjty.theoneprobe.api.IProbeInfo.STARTLOC;

public class ModTOPDriver implements TOPDriver{
    public static final ModTOPDriver DRIVER = new ModTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = blockState.getBlock().getRegistryName();
        if (!drivers.containsKey(id)) {
            if (blockState.getBlock() == ModBlocks.BASICTANK) {
                drivers.put(id, new BasicTankDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    public static <INPUT extends BASE, BASE> void safeConsume(BASE o, Consumer<INPUT> consumer, String error) {
        try {
            consumer.accept((INPUT) o);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(error, e);
        }
    }

    public static <INPUT extends BASE, BASE> void safeConsume(BASE o, Consumer<INPUT> consumer) {
        try {
            consumer.accept((INPUT) o);
        } catch (ClassCastException ignore) {
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            DRIVER.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class BasicTankDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            safeConsume(world.getTileEntity(data.getPos()), (BasicTankTileEntity te) -> {
                FluidStack stack = te.getFluidFromTank();
                int fluidSize = stack.getAmount();
                int tankSize = te.getFluidCapacity();
                String amount = String.format("%,d", fluidSize);
                String capacity = String.format("%,d", tankSize);
                if (stack.isEmpty()) {
                    probeInfo.text(TextFormatting.GREEN + amount + " / " + capacity + " mB");
                } else {
                    probeInfo.text(TextFormatting.GREEN + STARTLOC + stack.getTranslationKey() + ENDLOC + ": " + amount + " / " + capacity + " mB");
                }
                /* TODO: Add tank progress bar
                int color = stack.getFluid().getAttributes().getColor();
                int tmp = 0xff555555;
                ReallySimpleTanks.LOGGER.debug(color + " vs " + tmp);
                probeInfo.progress(fluidSize, tankSize,
                        probeInfo.defaultProgressStyle()
                                .filledColor(color)
                                .alternateFilledColor(color)
                                .borderColor(0xff555555)
                                .numberFormat(NumberFormat.NONE)); */
            }, "Bad tile entity!");
        }
    }
}
