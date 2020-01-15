package com.reallysimpletanks.blocks;

import com.reallysimpletanks.api.TankMode;
import com.reallysimpletanks.compat.ModTOPDriver;
import com.reallysimpletanks.compat.TOPDriver;
import com.reallysimpletanks.compat.TOPInfoProvider;
import com.reallysimpletanks.utils.EnumUtils;
import com.reallysimpletanks.utils.Tools;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

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
            if (FluidUtil.interactWithFluidHandler(player, handIn, worldIn, pos, hit.getFace())) return true;
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof INamedContainerProvider) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, (buffer) -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeByte((byte) ((BasicTankTileEntity) tileEntity).getTankMode().ordinal());
                });
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return true;
        }
        return true;
    }
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt == null) { return; }
        ITextComponent text = new TranslationTextComponent("%s: %s mB", "Capacity", String.format("%,d", BasicTankTileEntity.CAPACITY));
        Style style = new Style();
        if (compoundnbt.contains("tank")) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compoundnbt.getCompound("tank"));
            text = Tools.formatFluid(fluidStack, BasicTankTileEntity.CAPACITY);
        }
        tooltip.add(text.setStyle(style.setColor(TextFormatting.GREEN)));  //shows tank capacity/fluid contents

        if (compoundnbt.contains("TankMode")) {
            TankMode mode = EnumUtils.byOrdinal(compoundnbt.getByte("TankMode"), TankMode.NORMAL);
            text = new TranslationTextComponent("misc.reallysimpletanks.tankMode", mode.name());
            tooltip.add(text.setStyle(style.setColor(TextFormatting.GREEN)));  //shows tank mode
        }
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
