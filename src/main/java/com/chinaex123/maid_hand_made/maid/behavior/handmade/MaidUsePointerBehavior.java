package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.item.PointerItem;
import com.chinaex123.maid_hand_made.maid.behavior.MaidUseItemOnMachineBehavior;
import com.chinaex123.maid_hand_made.util.PointerHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 指杆。
 */
public class MaidUsePointerBehavior extends MaidUseItemOnMachineBehavior {

    public MaidUsePointerBehavior(float movementSpeed) {
        super(movementSpeed);
    }


    @Override
    protected boolean useHandsMode() {
        return true;
    }

    @Override
    protected ItemStack getMainHandStack(EntityMaid maid) {
        return maid.getMainHandItem();
    }

    @Override
    protected ItemStack getOffHandStack(EntityMaid maid) {
        return maid.getOffhandItem();
    }

    @Override
    protected boolean isCorrectItem(ItemStack stack) {
        return stack.getItem() instanceof PointerItem;
    }

    @Override
    protected boolean isMachine(ServerLevel world, BlockPos pos) {
        // 有物品就算“有活”，加工时再判配方
        if (PointerHelper.depotHasItem(world, pos)) return true;
        return PointerHelper.beltHasItem(world, pos);
    }

    @Override
    protected boolean isMachineBlock(ServerLevel world, BlockPos pos) {
        return PointerHelper.isPointerTarget(world, pos);
    }

    @Override
    protected boolean isValidStandPos(ServerLevel world, BlockPos machinePos, BlockPos standPos) {
        // 传送带：不能站带面上/上方，只能站旁边
        if (BlockEntityBehaviour.get(world, machinePos,
                TransportedItemStackHandlerBehaviour.TYPE) != null) {
            return !standPos.equals(machinePos) && !standPos.equals(machinePos.above());
        }
        return true;
    }

    @Override
    protected boolean useItemOnMachine(ServerLevel world, EntityMaid maid, BlockPos pos, ItemStack stack) {
        ItemStack tool = getOffHandStack(maid);
        if (tool.isEmpty()) {
            if (!autoStockOffHand(world, maid, pos)) return true;
            tool = getOffHandStack(maid);
            if (tool.isEmpty()) return true;
        }

        if (world.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            PointerHelper.processDepot(world, pos, maid, tool, depot);
        } else {
            PointerHelper.processBelt(world, pos, maid, tool);
        }
        return true;
    }

    private boolean autoStockOffHand(ServerLevel world, EntityMaid maid, BlockPos machinePos) {
        ItemStack targetOnMachine = getTargetItemOnMachine(world, machinePos);
        if (targetOnMachine.isEmpty()) return false;

        IItemHandler inv = maid.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inv == null) return false;

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack candidate = inv.getStackInSlot(i);
            if (candidate.isEmpty()) continue;

            RecipeHolder<?> recipe = PointerHelper.findRecipe(world, targetOnMachine, candidate);
            if (recipe != null) {
                // 从背包完整取出这件物品，塞进副手
                ItemStack moved = inv.extractItem(i, candidate.getCount(), false);
                if (!moved.isEmpty()) {
                    maid.setItemInHand(InteractionHand.OFF_HAND, moved);
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack getTargetItemOnMachine(ServerLevel world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            return depot.getHeldItem();
        }
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler != null) {
            ItemStack[] target = {ItemStack.EMPTY};
            handler.handleProcessingOnAllItems(item -> {
                if (!item.stack.isEmpty() && target[0].isEmpty()) {
                    target[0] = item.stack;
                }
                return TransportedResult.doNothing();
            });
            return target[0];
        }
        return ItemStack.EMPTY;
    }
}