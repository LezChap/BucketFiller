package com.reallysimpletanks.blocks;

import com.reallysimpletanks.api.TankMode;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;


public class CustomTank extends FluidTank {
    private TileEntity tileEntity;

    public CustomTank(int capacity, TileEntity tileEntity) {
        super(capacity);
        this.tileEntity = tileEntity;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        tileEntity.markDirty();
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        this.onContentsChanged();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }

        if (!fluid.isFluidEqual(resource) && !fluid.isEmpty()) {
            return 0;
        }

        if (action.simulate()) {
            if (getTankMode() == TankMode.EXCESS && ((BasicTankTileEntity) tileEntity).getExcessInstalled()) return resource.getAmount();

            if (fluid.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }

        if (fluid.isEmpty()) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            if (getTankMode() == TankMode.EXCESS && ((BasicTankTileEntity) tileEntity).getExcessInstalled()) return resource.getAmount();
            return fluid.getAmount();
        }

        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled)  {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(capacity);
        }

        if (filled > 0)  onContentsChanged();

        if (getTankMode() == TankMode.EXCESS && ((BasicTankTileEntity) tileEntity).getExcessInstalled()) return resource.getAmount();
        return filled;
    }

    private TankMode getTankMode() {
        return ((BasicTankTileEntity) tileEntity).getTankMode();
    }
}
