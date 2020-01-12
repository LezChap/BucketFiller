package com.reallysimpletanks.compat;

import com.reallysimpletanks.ReallySimpleTanks;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import javax.annotation.Nullable;

public class TOPCompatibility {
    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        // @todo make easier!
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new GetTheOneProbe());
    }


    public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {

        public static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            ReallySimpleTanks.LOGGER.info("Enabled support for The One Probe");
            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return "reallysimpletanks:default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
                    if (blockState.getBlock() instanceof TOPInfoProvider) {
                        TOPInfoProvider provider = (TOPInfoProvider) blockState.getBlock();
                        TOPDriver driver = provider.getProbeDriver();
                        if (driver != null) {
                            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                        }
                    }

                }
            });

            return null;
        }
    }
}