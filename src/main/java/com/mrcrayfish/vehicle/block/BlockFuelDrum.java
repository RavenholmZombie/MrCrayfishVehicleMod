package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.item.ItemJerryCan;
import com.mrcrayfish.vehicle.tileentity.TileEntityFuelDrum;
import com.mrcrayfish.vehicle.util.Bounds;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class BlockFuelDrum extends BlockRotatedObject
{
    private static final AxisAlignedBB BOUNDING_BOX = new Bounds(1, 0, 1, 15, 16, 15).toAABB();

    private int capacity;

    public BlockFuelDrum(String id, int capacity)
    {
        super(Material.IRON, id);
        this.capacity = capacity;
        this.setHardness(1.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced)
    {
        if(GuiScreen.isShiftKeyDown())
        {
        	if(stack.getItem().getUnlocalizedName().contains("creative_"))
        	{
        		String info = "An infinite source of fluids. Can only be spawned in through Creative Mode or by commands.";
        		tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(info, 150));
        	}
        	else
        	{
        		String info = I18n.format("tile.vehicle.fuel_drum.info");
        		tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(info, 150));
        	}
        }
        else
        {
            tooltip.add(TextFormatting.YELLOW + I18n.format("vehicle.info_help"));
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BOUNDING_BOX;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            ItemStack stack = playerIn.getHeldItem(hand);

            if(FluidUtil.interactWithFluidHandler(playerIn, hand, worldIn, pos, facing))
            {
                return true;
            }
            
        	if(state.getBlock() == ModBlocks.CREATIVE_FUEL_DRUM)
        	{
        		TileEntity tileEntity = worldIn.getTileEntity(pos);
        		if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
        		{
        			IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        			if(handler != null)
        			{
        				if(handler instanceof FluidTank)
        				{
        					FluidTank tank = (FluidTank) handler;
        					FluidStack fluidStack = tank.getFluid();
        					fluidStack.amount = Integer.MAX_VALUE;
        					if(fluidStack.amount < Integer.MAX_VALUE)
        					{
        						fluidStack.amount = Integer.MAX_VALUE;
        						tank.fill(fluidStack, true);
        						playerIn.sendMessage(new TextComponentString("Fill"));
        					}
        				}
        			}
        		}
        	}

            if(stack.getItem() instanceof ItemJerryCan)
            {
                ItemJerryCan jerryCan = (ItemJerryCan) stack.getItem();

                if(jerryCan.isFull(stack))
                    return false;

                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                {
                    IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                    if(handler != null)
                    {
                        if(handler instanceof FluidTank)
                        {
                            FluidTank tank = (FluidTank) handler;
                            if(tank.getFluid() != null && tank.getFluid().getFluid() != FluidRegistry.getFluid("gasoline") && tank.getFluid().getFluid() != FluidRegistry.getFluid("diesel") && tank.getFluid().getFluid() != FluidRegistry.getFluid("biodiesel"))
                                return false;

                            FluidStack fluidStack = handler.drain(50, true);
                            if(fluidStack != null)
                            {
                                int remaining = jerryCan.fill(stack, fluidStack.amount);
                                if(remaining > 0)
                                {
                                    fluidStack.amount = remaining;
                                    handler.fill(fluidStack, true);
                                }
                            }
                        }
                    }
                }
            }
            else
            {
            	TileEntity tileEntity = worldIn.getTileEntity(pos);
            	if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
            	{
            		IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            		
            		if(handler != null)
            		{
            			if(handler instanceof FluidTank)
            			{
            				FluidTank tank = (FluidTank) handler;
            				if(tank.getFluidAmount() == 0)
            				{
            					playerIn.sendMessage(new TextComponentString("Drum is empty."));
            				}
            				else
            				{
            					String fuelName = tank.getFluid().getLocalizedName();
            					int fuelAmount = tank.getFluidAmount();
            					if(fuelAmount >= Integer.MAX_VALUE)
            					{
            						playerIn.sendMessage(new TextComponentString(fuelName + ": " + "Infinite (" + fuelAmount + " mB)"));
            					}
            					else
            					{
            						playerIn.sendMessage(new TextComponentString(fuelName + ": " + fuelAmount + "mB"));
            					}
            				}
            			}
            		}
            	}
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityFuelDrum(capacity);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if(!world.isRemote && !player.capabilities.isCreativeMode)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileEntityFuelDrum)
            {
                ItemStack drop = new ItemStack(Item.getItemFromBlock(this));
                if(((TileEntityFuelDrum) tileEntity).getAmount() > 0)
                {
                    NBTTagCompound tileEntityTag = new NBTTagCompound();
                    tileEntity.writeToNBT(tileEntityTag);
                    tileEntityTag.removeTag("x");
                    tileEntityTag.removeTag("y");
                    tileEntityTag.removeTag("z");
                    tileEntityTag.removeTag("id");

                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setTag("BlockEntityTag", tileEntityTag);
                    drop.setTagCompound(compound);
                }
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                return world.setBlockToAir(pos);
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
}
