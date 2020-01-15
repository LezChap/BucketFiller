package com.reallysimpletanks.network;

import com.reallysimpletanks.ReallySimpleTanks;
import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.blocks.BasicTankContainer;
import com.reallysimpletanks.blocks.BasicTankTileEntity;
import com.reallysimpletanks.utils.EnumUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TankModePacket {
    private TankMode mode;

    public TankModePacket(PacketBuffer buf) {
        mode = EnumUtils.byOrdinal(buf.readByte(), TankMode.NORMAL);
    }

    public TankModePacket(TankMode mode) {
        this.mode = mode;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(mode.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity player = ctx.get().getSender();
                    if (player != null) {
                        if (player.openContainer instanceof BasicTankContainer) {
                            TileEntity te = ((BasicTankContainer) player.openContainer).getTileEntity();
                            if (te instanceof BasicTankTileEntity) {
                                ((BasicTankTileEntity) te).setTankMode(mode);
                            }
                        }
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}

