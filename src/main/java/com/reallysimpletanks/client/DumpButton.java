package com.reallysimpletanks.client;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;

public class DumpButton extends Button {
    private final Container container;


    public DumpButton(Container container, int widthIn, int heightIn, int width, int height, String text, IPressable onPress) {
        super(widthIn, heightIn, width, height, "", onPress);
        this.container = container;
    }


}
