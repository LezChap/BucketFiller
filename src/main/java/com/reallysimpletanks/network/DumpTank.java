package com.reallysimpletanks.network;

import com.reallysimpletanks.ReallySimpleTanks;
import com.reallysimpletanks.blocks.BasicTankTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DumpTank {
    private final FluidStack stack;
    private final BlockPos pos;

    public DumpTank(PacketBuffer buf) {
        stack = buf.readFluidStack();
        pos = buf.readBlockPos();
    }

    public DumpTank(FluidStack stackIn, BlockPos posIn) {
        this.stack = stackIn;
        this.pos = posIn;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeFluidStack(stack);
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerWorld world = (ServerWorld)ctx.get().getSender().world;
            if (world.isBlockLoaded(pos)) {
                BasicTankTileEntity te = (BasicTankTileEntity) world.getTileEntity(pos);
                te.dumpTank();
            } else {
                ReallySimpleTanks.LOGGER.error("Attempted to dump fluid in Tank @: " + pos.toString() + " when block is unloaded.");
            }
        });
        ctx.get().setPacketHandled(true);
    }

}

