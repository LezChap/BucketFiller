package com.reallysimpletanks.blocks;

import com.reallysimpletanks.ReallySimpleTanks;
import com.reallysimpletanks.api.IUpgradeItem;
import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.network.DumpTank;
import com.reallysimpletanks.network.Networking;
import com.reallysimpletanks.utils.EnumUtils;
import com.reallysimpletanks.utils.SlotBucketHandler;
import com.reallysimpletanks.utils.SlotEmptyBucketHandler;
import com.reallysimpletanks.utils.SlotUpgradeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static com.reallysimpletanks.blocks.ModBlocks.BASICTANK_CONTAINER;

public class BasicTankContainer extends Container {

    private BasicTankTileEntity tileEntity;
    private IItemHandler playerInventory;
    private IIntArray fields;
    private BlockPos pos;


    public BasicTankContainer(int windowId, BlockPos posIn, PlayerInventory inv, TankMode tankMode) {
            this(windowId, posIn, inv, new BasicTankTileEntity(), new IntArray(BasicTankTileEntity.FIELDS_COUNT));
            fields.set(2, tankMode.ordinal());
    }


    public BasicTankContainer(int windowId, BlockPos posIn, PlayerInventory inv, BasicTankTileEntity te, IIntArray field) {
        super(BASICTANK_CONTAINER, windowId);
        assertIntArraySize(field, BasicTankTileEntity.FIELDS_COUNT);
        this.fields = field;
        this.tileEntity = te;
        this.pos = posIn;
        this.playerInventory = new InvWrapper(inv);

        //Legacy Debug code
        //ReallySimpleTanks.LOGGER.info("Fluid Amount: " + tileEntity.getFluidAmount());
        //StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        //ReallySimpleTanks.LOGGER.debug(elements);
        //for (int i = 1; i < elements.length; i++) {
        //    StackTraceElement s = elements[i];
        //    ReallySimpleTanks.LOGGER.debug("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        //}

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            addSlot(new SlotBucketHandler(h, 0, 46, 8));
            addSlot(new SlotEmptyBucketHandler(h, 1, 118, 8));
            addSlot(new SlotEmptyBucketHandler(h, 2, 46, 44));
            addSlot(new SlotBucketHandler(h, 3, 118, 44));

            addSlot(new SlotUpgradeItem(h, 4, 154, 8));
            addSlot(new SlotUpgradeItem(h, 5, 154, 26));
            addSlot(new SlotUpgradeItem(h, 6, 154, 44));
        });
        layoutPlayerInventorySlots(10, 70);

        trackIntArray(this.fields);
    }

    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    public FluidStack getFluidInTank() {
        int fluidID = fields.get(0);
        Fluid fluid = Registry.FLUID.getByValue(fluidID);
        int amount = fields.get(1);
        return new FluidStack(fluid, amount);
    }

    public int getFluidCapacity() {
        return tileEntity.CAPACITY;
    }

    public void dumpTank() {
        Networking.INSTANCE.sendToServer(new DumpTank(FluidStack.EMPTY, pos));
    }

    public IIntArray getFields() {return fields;}

    public TankMode getTankMode() {
        //ReallySimpleTanks.LOGGER.debug(EnumUtils.byOrdinal(fields.get(2), TankMode.NORMAL));
        return EnumUtils.byOrdinal(fields.get(2), TankMode.NORMAL);
    }

    public void setTankMode(TankMode mode) {
        fields.set(2, mode.ordinal());
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()), playerIn, ModBlocks.BASICTANK);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();
            if (index <= 6) {
                if (!this.mergeItemStack(stack, 7, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, itemstack);
            } else {
                if (stack.getItem() == Items.BUCKET) {
                    if (!this.mergeItemStack(stack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack.getItem() instanceof BucketItem && stack.getItem() != Items.BUCKET) {
                    if (!this.mergeItemStack(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack.getItem() instanceof IUpgradeItem) {
                    if (!this.mergeItemStack(stack, 4, 7, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 34) {
                    if (!this.mergeItemStack(stack, 34, 43, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 43 && !this.mergeItemStack(stack, 7, 34, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }
}
