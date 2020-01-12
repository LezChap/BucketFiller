package com.reallysimpletanks.compat;

import net.minecraftforge.fml.ModList;

public class MainCompatHandler {

    public static void registerTOP() {
        if (ModList.get().isLoaded("theoneprobe")) {
            TOPCompatibility.register();
        }
    }
}
