package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.item.BellowsItem;
import com.chinaex123.maid_hand_made.maid.behavior.MaidUseItemOnMachineBehavior;
import com.chinaex123.maid_hand_made.util.BellowsHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;

/**
 * 风箱。
 */
public class MaidUseBellowsBehavior extends MaidUseItemOnMachineBehavior {

    public MaidUseBellowsBehavior(float movementSpeed) {
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
        return stack.getItem() instanceof BellowsItem;
    }

    @Override
    protected boolean isMachine(ServerLevel world, BlockPos pos) {
        return BellowsHelper.isBellowsTarget(world, pos);
    }

    @Override
    protected boolean isMachineBlock(ServerLevel world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof DepotBlockEntity) {
            return true;
        }
        return BlockEntityBehaviour.get(world, pos,
                TransportedItemStackHandlerBehaviour.TYPE) != null;
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

        // 副手介质
        ItemStack media = getOffHandStack(maid);
        if (media.isEmpty()) return true;

        // 加工
        if (BellowsHelper.process(world, pos, media)) {
            // 消耗风箱耐久
            stack.hurtAndBreak(1, maid, EquipmentSlot.MAINHAND);
            completedWork = true;
        }
        return true;
    }
}