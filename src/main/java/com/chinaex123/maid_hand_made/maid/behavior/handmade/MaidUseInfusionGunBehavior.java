package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.api.CreateHandMadeApi;
import com.alben.createhandmade.item.InfusionGunContents;
import com.alben.createhandmade.item.InfusionGunItem;
import com.chinaex123.maid_hand_made.maid.behavior.MaidUseItemOnMachineBehavior;
import com.chinaex123.maid_hand_made.util.InfusionGunHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 灌注枪。
 */
public class MaidUseInfusionGunBehavior extends MaidUseItemOnMachineBehavior {

    public MaidUseInfusionGunBehavior(float movementSpeed) {
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
    protected boolean isCorrectItem(ItemStack stack) {
        return stack.getItem() instanceof InfusionGunItem;
    }

    @Override
    protected boolean isMachine(ServerLevel world, BlockPos pos) {
        return isAnyTargetBlock(world, pos);
    }

    @Override
    protected boolean isMachineBlock(ServerLevel world, BlockPos pos) {
        return isAnyTargetBlock(world, pos);
    }

    @Override
    protected boolean isMachineFor(ServerLevel world, EntityMaid maid, BlockPos pos) {
        ItemStack gun = getMainHandStack(maid);
        if (gun.isEmpty() || !(gun.getItem() instanceof InfusionGunItem)) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        boolean isBasin = world.getBlockEntity(pos) instanceof BasinBlockEntity;

        // 枪空：吸流体源 / 抽容器（含 Basin，Basin 可抽不可放）
        if (contents.isEmpty()) {
            if (InfusionGunHelper.isFluidSource(world, pos)) return true;
            return InfusionGunHelper.containerHasFluid(world, pos);
        }

        // 枪有流体：注入传送带 / 置物台物品 / 存容器（Basin 不作为注入目标）
        if (isBasin) return false;
        if (BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE) != null) {
            return InfusionGunHelper.beltHasInjectable(world, pos, gun);
        }
        if (world.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            ItemStack held = depot.getHeldItem();
            return !held.isEmpty() && InfusionGunHelper.canFillItem(world, held, contents.fluid());
        }
        return hasFluidHandler(world, pos);
    }

    @Override
    protected boolean isValidStandPos(ServerLevel world, BlockPos machinePos, BlockPos standPos) {
        // 传送带：不能站带面上/上方
        if (BlockEntityBehaviour.get(world, machinePos,
                TransportedItemStackHandlerBehaviour.TYPE) != null) {
            return !standPos.equals(machinePos) && !standPos.equals(machinePos.above());
        }
        return true;
    }

    @Override
    protected boolean useItemOnMachine(ServerLevel world, EntityMaid maid, BlockPos pos, ItemStack stack) {
        // 锁移动
        maid.getNavigation().stop();
        maid.setDeltaMovement(0, maid.getDeltaMovement().y, 0);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(stack);
        boolean isBasin = world.getBlockEntity(pos) instanceof BasinBlockEntity;

        // 工作盆 只能抽不能放：枪有流体时直接跳过
        if (isBasin && !contents.isEmpty()) return true;

        // 枪空：吸 / 抽
        if (contents.isEmpty()) {
            if (InfusionGunHelper.isFluidSource(world, pos)) {
                if (InfusionGunHelper.tryPickupFluidBlock(world, pos, maid, stack)) {
                    completedWork = true;
                }
                return true;
            }
            if (InfusionGunHelper.containerHasFluid(world, pos)) {
                if (InfusionGunHelper.tryExtractMax(world, pos, maid, stack)) {
                    completedWork = true;
                }
                return true;
            }
            return true;
        }

        // 传送带
        if (BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE) != null) {
            if (InfusionGunHelper.tryInjectItems(world, pos, maid, stack)) {
                completedWork = true;
            }
            return true;
        }

        // 置物台：先注入物品
        if (world.getBlockEntity(pos) instanceof DepotBlockEntity) {
            if (InfusionGunHelper.tryInjectDepot(world, pos, maid, stack)) {
                completedWork = true;
            }
            return true;
        }

        // 其他容器：存
        if (hasFluidHandler(world, pos)) {
            if (InfusionGunHelper.tryStoreFluid(world, pos, maid, stack)) {
                completedWork = true;
            }
        }
        return true;
    }

    private static boolean isAnyTargetBlock(ServerLevel world, BlockPos pos) {
        // 工作盆 有流体能力：枪空时抽（走 isMachineFor 里的分支），枪有流体时 isMachineFor 已排除
        if (InfusionGunHelper.isFluidSource(world, pos)) return true;
        if (world.getBlockEntity(pos) instanceof DepotBlockEntity) return true;
        if (BlockEntityBehaviour.get(world, pos,
                TransportedItemStackHandlerBehaviour.TYPE) != null) return true;
        return hasFluidHandler(world, pos);
    }

    private static boolean hasFluidHandler(ServerLevel world, BlockPos pos) {
        IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        return handler != null;
    }
}