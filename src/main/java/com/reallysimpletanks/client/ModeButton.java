package com.reallysimpletanks.client;

import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.blocks.BasicTankContainer;
import com.reallysimpletanks.utils.EnumUtils;
import net.minecraft.client.gui.widget.button.Button;

public class ModeButton extends Button {
    private final BasicTankContainer container;

    public ModeButton(BasicTankContainer containerIn, int x, int y, int width, int height, IPressable onPress) {
        super(x, y, width, height, "", button -> {
            ((ModeButton) button).cycleMode();
            onPress.onPress(button);
        });
        this.container = containerIn;
        setLabel();
    }

    public TankMode getMode() {
        return container.getTankMode();
    }

    private void setLabel() {
        //String label = new TranslationTextComponent("misc.reallysimpletanks.tankMode", getMode().name()).getFormattedText();
        setMessage(getMode().name());
    }


    private void cycleMode() {
        int ordinal = container.getTankMode().ordinal() + 1;
        if (ordinal >= TankMode.values().length)
            ordinal = 0;
        container.setTankMode(EnumUtils.byOrdinal(ordinal, TankMode.NORMAL));
        setLabel();
    }
}
