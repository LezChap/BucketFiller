package com.reallysimpletanks.blocks;

import com.reallysimpletanks.ReallySimpleTanks;
import com.reallysimpletanks.api.IUpgradeItem;
import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.items.ExcessUpgradeItem;
import com.reallysimpletanks.utils.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.reallysimpletanks.blocks.ModBlocks.BASICTANK_TILEENTITY;

public class BasicTankTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public static final int FIELDS_COUNT = 3;
    public static final int CAPACITY = FluidAttributes.BUCKET_VOLUME * 16;
    protected ItemStackHandler inputSlots;
    protected ItemStackHandler outputSlots;
    private ItemStackHandler upgradeSlots;
    private ItemStackHandler inputSlotsWrapper;
    private ItemStackHandler outputSlotsWrapper;
    private final LazyOptional<IItemHandler> externalHandler = LazyOptional.of(() -> new CustomCombinedInvWrapper(inputSlotsWrapper, outputSlotsWrapper));
    private final LazyOptional<IItemHandler> internalHandler = LazyOptional.of(() -> new CustomCombinedInvWrapper(inputSlots, outputSlots, upgradeSlots));

    protected  TankMode tankMode = TankMode.NORMAL;
    private boolean isExcessInstalled;
    private boolean isPumpInstalled;

    protected CustomTank internalTank = new CustomTank(CAPACITY, this){
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            markDirty();
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(stack);
            this.onContentsChanged();
        }

    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> internalTank);



    protected final IIntArray fields = new IIntArray() {
        @SuppressWarnings("deprecation")
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return Registry.FLUID.getId(internalTank.getFluid().getFluid());
                case 1:
                    return internalTank.getFluid().getAmount();
                case 2:
                    return tankMode.ordinal();
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 2:
                    tankMode = EnumUtils.byOrdinal(value, TankMode.NORMAL);
                    break;
            }

        }

        @Override
        public int size() {
            return FIELDS_COUNT;
        }
    };


    public BasicTankTileEntity() {
        super(BASICTANK_TILEENTITY);
        inputSlots = createInputHandler();
        outputSlots = createOutputHandler();
        upgradeSlots = createUpgradeHandler();
        inputSlotsWrapper = new InputItemStackHandler(inputSlots);
        outputSlotsWrapper = new OutputItemStackHandler(outputSlots);
        getInstalledUpgrades();
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        internalHandler.ifPresent(h ->{
            ItemStack inputStack = h.getStackInSlot(0);
            ItemStack outputStack = h.getStackInSlot(2);
            Item item = inputStack.getItem();
            if (item instanceof BucketItem && item != Items.BUCKET) {
                if (outputStack.isEmpty() || ((outputStack.getItem() == Items.BUCKET) && outputStack.getCount() <outputStack.getMaxStackSize()))
                {
                    FluidActionResult fillResult = FluidUtil.tryEmptyContainer(inputStack, internalTank, Integer.MAX_VALUE, null, true);
                    if (fillResult.isSuccess()) {
                        h.extractItem(0, 1, false);
                        h.insertItem(2, fillResult.getResult(), false);
                    }
                }
            }
            inputStack = h.getStackInSlot(1);
            outputStack = h.getStackInSlot(3);
            item = inputStack.getItem();
            if (item == Items.BUCKET && outputStack.isEmpty()) {
                FluidActionResult fillResult = FluidUtil.tryFillContainer(inputStack, internalTank, Integer.MAX_VALUE, null, true);
                if (fillResult.isSuccess()) {
                    h.extractItem(1, 1, false);
                    h.insertItem(3, fillResult.getResult(), false);
                }
            }

            //legacy debug code
            /*stack = h.getStackInSlot(3);
            item = stack.getItem();
            if (item == Items.BUCKET) {
                ReallySimpleTanks.LOGGER.info("Fluid Amount:" + internalTank.getFluidAmount());
            }*/
        });
    }

    public FluidStack getFluidFromTank() {
        return internalTank.getFluid();
    }

    public int getFluidCapacity() {
        return CAPACITY;
    }

    public void dumpTank() {
        internalTank.setFluid(FluidStack.EMPTY);
    }

    public TankMode getTankMode() {
        return tankMode;
    }

    public void setTankMode(TankMode tankMode) {
        this.tankMode = tankMode;
    }

    public IIntArray getFields() { return fields; }

    public boolean getExcessInstalled() { return isExcessInstalled; }

    public boolean getPumpInstalled() { return isPumpInstalled; }

    public void getInstalledUpgrades() {
        isPumpInstalled = false;
        isExcessInstalled = false;
        int count = upgradeSlots.getSlots();
        for (int i = 0; i < count; i++) {
            Item item = upgradeSlots.getStackInSlot(i).getItem();
            if (item instanceof ExcessUpgradeItem) isExcessInstalled = true;
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        internalHandler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        super.read(tag);
        if (tag.contains("tank")) {
            internalTank.readFromNBT(tag.getCompound("tank"));
        }
        if (tag.contains("TankMode")) {
            tankMode = EnumUtils.byOrdinal(tag.getByte("TankMode"), TankMode.NORMAL);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        internalHandler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });
        tag.put("tank", internalTank.writeToNBT(new CompoundNBT()));
        tag.putByte("TankMode", (byte) tankMode.ordinal());
        return tag;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tags = super.getUpdateTag();
        internalHandler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tags.put("inv", compound);
        });
        tags.put("tank", internalTank.writeToNBT(new CompoundNBT()));
        tags.putByte("TankMode", (byte) tankMode.ordinal());
        return tags;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        if (tag.contains("tank")) {
            internalTank.readFromNBT(tag.getCompound("tank"));
        }
        CompoundNBT invTag = tag.getCompound("inv");
        internalHandler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        if (tag.contains("TankMode")) {
            tankMode = EnumUtils.byOrdinal(tag.getByte("TankMode"), TankMode.NORMAL);
        }

    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("TankMode", (byte) tankMode.ordinal());
        return new SUpdateTileEntityPacket(getPos(), 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundNBT tag = pkt.getNbtCompound();
        if (tag.contains("TankMode")) {
            tankMode = EnumUtils.byOrdinal(tag.getByte("TankMode"), TankMode.NORMAL);
        }
    }

    private ItemStackHandler createInputHandler() {
        return new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                Item item = stack.getItem();
                if (!(item instanceof BucketItem)) return false;
                if (slot == 0) {
                    if (item != Items.BUCKET) {
                        return true;
                    }
                }
                if (slot == 1) {
                    return item == Items.BUCKET;
                }
                return false;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                Item item = stack.getItem();
                if (!(item instanceof BucketItem)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private ItemStackHandler createOutputHandler() {
        return new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                Item item = stack.getItem();
                if (!(item instanceof BucketItem)) return false;
                if (slot == 0) {
                    return item == Items.BUCKET;
                }
                if (slot == 1) {
                    return (item != Items.BUCKET);
                }
                return false;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                Item item = stack.getItem();
                if (!(item instanceof BucketItem)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private ItemStackHandler createUpgradeHandler() {
        return new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                getInstalledUpgrades();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof IUpgradeItem;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                Item item = stack.getItem();
                if (!(item instanceof IUpgradeItem)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (world != null && world.getBlockState(pos).getBlock() != this.getBlockState().getBlock()) {//if something is broken
                ReallySimpleTanks.LOGGER.debug("reallysimpletanks:basictank at X:" + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ() + "Throwing Block Mismatch error when getting ItemHandler Capability.");
                return internalHandler.cast();
            }
            if (side == null) {
                return internalHandler.cast();
            }
            return externalHandler.cast();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BasicTankContainer(id, getPos(), playerInventory, this, this.fields);
    }
}