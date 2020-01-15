package com.reallysimpletanks.api;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public enum  TankMode {
    NORMAL(new ResourceLocation("textures/item/blah.png")),
    EXCESS(new ResourceLocation("textures/item/blah.png")),
    PUMP(new ResourceLocation("textures/item/blah.png"));

    private final ResourceLocation texture;

    TankMode(ResourceLocation texture) {
        this.texture = texture;
    }

    @Nullable
    public static TankMode byName(String name) {
        for (TankMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
