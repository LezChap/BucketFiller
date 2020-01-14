package com.reallysimpletanks.setup;

import com.reallysimpletanks.compat.MainCompatHandler;
import com.reallysimpletanks.network.Networking;
import net.minecraftforge.common.MinecraftForge;

public class ModSetup {

    public void init() {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        Networking.registerMessages();

        MainCompatHandler.registerTOP();
    }
}
