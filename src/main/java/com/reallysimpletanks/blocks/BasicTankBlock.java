package com.reallysimpletanks.blocks;

import com.reallysimpletanks.compat.ModTOPDriver;
import com.reallysimpletanks.compat.TOPDriver;
import com.reallysimpletanks.compat.TOPInfoProvider;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class BasicTankBlock extends Block implements TOPInfoProvider {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    private final TOPDriver topDriver = ModTOPDriver.DRIVER;

    public BasicTankBlock() {
        super(Properties.create(Material.IRON)
            .sound(SoundType.METAL)
            .hardnessAndResistance(2.0f)
            .harvestTool(ToolType.PICKAXE)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

   @Nullable
   @Override
   public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BasicTankTileEntity();
   }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            LazyOptional<IFluidHandler> fluidhandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            boolean buckedUsed = fluidhandler.map(h -> {
                ItemStack heldItem = player.getHeldItem(handIn);
                FluidActionResult fillResult = FluidUtil.tryEmptyContainerAndStow(heldItem, h, null, Integer.MAX_VALUE, player, true);
                if (fillResult.isSuccess()) {
                    player.setHeldItem(handIn, fillResult.getResult());
                    return true;
                }
                fillResult = FluidUtil.tryFillContainerAndStow(heldItem, h, null, Integer.MAX_VALUE, player, true);
                if (fillResult.isSuccess()) {
                    player.setHeldItem(handIn, fillResult.getResult());
                    return true;
                }
                return false;
            }).orElse(false);
            if (buckedUsed) { return true; }
            if (tileEntity instanceof INamedContainerProvider) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return true;
        }
        return true;
    }

    public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        return Direction.getFacingFromVector((float) (entity.posX - clickedBlock.getX()), (float) (entity.posY - clickedBlock.getY()), (float) (entity.posZ - clickedBlock.getZ()));
    }

    @Override
    public TOPDriver getProbeDriver() {
        return topDriver;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

}
