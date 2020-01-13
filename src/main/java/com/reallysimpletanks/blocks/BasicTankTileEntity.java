package com.reallysimpletanks.blocks;

import com.reallysimpletanks.ReallySimpleTanks;
import com.reallysimpletanks.utils.CustomCombinedInvWrapper;
import com.reallysimpletanks.utils.InputItemStackHandler;
import com.reallysimpletanks.utils.OutputItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.fluids.capability.templates.FluidTank;
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
    private ItemStackHandler inputSlotsWrapper;
    private ItemStackHandler outputSlotsWrapper;
    private final LazyOptional<IItemHandler> externalHandler = LazyOptional.of(() -> new CustomCombinedInvWrapper(inputSlotsWrapper, outputSlotsWrapper));
    private final LazyOptional<IItemHandler> internalHandler = LazyOptional.of(() -> new CustomCombinedInvWrapper(inputSlots, outputSlots));


    protected FluidTank internalTank = new FluidTank(CAPACITY);
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
                    return internalTank.getCapacity();
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {

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
        inputSlotsWrapper = new InputItemStackHandler(inputSlots);
        outputSlotsWrapper = new OutputItemStackHandler(outputSlots);
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

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        internalHandler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        super.read(tag);
        if (tag.contains("tank")) {
            internalTank.readFromNBT(tag.getCompound("tank"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        internalHandler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });
        tag.put("tank", internalTank.writeToNBT(new CompoundNBT()));
        return super.write(tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tags = super.getUpdateTag();
        tags.put("tank", this.internalTank.writeToNBT(new CompoundNBT()));
        return tags;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        if (tag.contains("tank")) {
            internalTank.readFromNBT(tag.getCompound("tank"));
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
        return new BasicTankContainer(id, playerInventory,this, this.fields);
    }
}