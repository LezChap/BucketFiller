package com.reallysimpletanks.items;

import com.reallysimpletanks.api.IUpgradeItem;
import net.minecraft.item.Item;

public class ExcessUpgradeItem extends Item implements IUpgradeItem {

    public ExcessUpgradeItem() {
        super(new Item.Properties()
            .maxStackSize(64));
        setRegistryName("excess_upgrade");
    }
}
