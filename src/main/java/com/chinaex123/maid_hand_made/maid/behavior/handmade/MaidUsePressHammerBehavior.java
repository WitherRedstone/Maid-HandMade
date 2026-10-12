package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.item.PressHammerItem;
import com.chinaex123.maid_hand_made.maid.behavior.MaidUseItemOnMachineBehavior;
import com.chinaex123.maid_hand_made.util.PressHammerHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * 冲压锤。
 */
public class MaidUsePressHammerBehavior extends MaidUseItemOnMachineBehavior {

    public MaidUsePressHammerBehavior(float movementSpeed) {
        super(movementSpeed);
    }

    @Override
    protected boolean isCorrectItem(ItemStack stack) {
        return stack.getItem() instanceof PressHammerItem;
    }

    @Override
    protected boolean isMachine(ServerLevel world, BlockPos pos) {
        return PressHammerHelper.isPressTarget(world, pos);
    }

    @Override
    protected boolean useItemOnMachine(ServerLevel world, EntityMaid maid, BlockPos pos, ItemStack stack) {
        PressHammerHelper.press(world, maid, pos, stack);
        return true;
    }

    @Override
    protected boolean isValidStandPos(ServerLevel world, BlockPos machinePos, BlockPos standPos) {
        // 传送带：不能站带面上/上方，只能站旁边
        if (isBelt(world, machinePos)) {
            return !standPos.equals(machinePos)
                    && !standPos.equals(machinePos.above());
        }
        // 工作盆、置物台：不限
        return true;
    }

    private static boolean isBelt(ServerLevel world, BlockPos pos) {
        return BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE) != null;
    }

    @Override
    protected boolean isMachineBlock(ServerLevel world, BlockPos pos) {
        return PressHammerHelper.isPressBlock(world, pos);
    }
}