package com.chinaex123.maid_hand_made.util;

import com.alben.createhandmade.bellows.BellowsMediaRegistry;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.chinaex123.maid_hand_made.mixin.BellowsItemMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class BellowsHelper {

    private BellowsHelper() {
    }

    public static boolean isBellowsTarget(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            return !depot.getHeldItem().isEmpty();
        }
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        boolean[] found = {false};
        handler.handleProcessingOnAllItems(item -> {
            if (!found[0] && !item.stack.isEmpty()) found[0] = true;
            return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
        });
        return found[0];
    }

    public static boolean canProcess(Level level, BlockPos pos, ItemStack media) {
        if (media.isEmpty()) return false;
        BellowsMediaRegistry.EffectiveTypes effective = BellowsMediaRegistry.resolveEffective(media);
        if (effective.ids().isEmpty()) return false;

        return isBellowsTarget(level, pos);
    }

    public static boolean process(Level level, BlockPos pos, ItemStack media) {
        if (media.isEmpty()) return false;
        BellowsMediaRegistry.EffectiveTypes effective = BellowsMediaRegistry.resolveEffective(media);
        List<ResourceLocation> candidateIds = effective.ids();
        if (candidateIds.isEmpty()) return false;
        @Nullable ResourceLocation preferred = effective.preferred();
        FanProcessingType[] pickedOut = new FanProcessingType[]{null};
        return BellowsItemMixin.invokeApplyToTarget(level, pos, candidateIds, preferred, pickedOut);
    }
}