package com.chinaex123.maid_hand_made.util;

import com.alben.createhandmade.api.CreateHandMadeApi;
import com.alben.createhandmade.item.PressHammerItem;
import com.alben.createhandmade.recipe.HandMadeRecipePool;
import com.alben.createhandmade.recipe.HandMadeTool;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class PressHammerHelper {

    private PressHammerHelper() {}

    public static boolean isPressTarget(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BasinBlockEntity basin) {
            return hasBasinRecipe(level, basin);
        }

        if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            ItemStack held = depot.getHeldItem();
            return !held.isEmpty()
                    && PressHammerItem.findPressingRecipe(level, held) != null;
        }

        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler != null) {
            return hasTransportedRecipe(level, handler);
        }

        return false;
    }

    private static boolean hasBasinRecipe(ServerLevel level, BasinBlockEntity basin) {
        if (basin.isEmpty()) return false;
        try {
            List<RecipeHolder<?>> merged = HandMadeRecipePool.mergeInGlobalOrder(level,
                    CreateHandMadeApi.recipesFor(HandMadeTool.PRESS_HAMMER_BASIN, level),
                    CreateHandMadeApi.recipesFor(HandMadeTool.PRESS_HAMMER_AUTO_SQUARE, level));
            for (RecipeHolder<?> holder : merged) {
                if (BasinRecipe.match(basin, holder.value())) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean hasTransportedRecipe(ServerLevel level, TransportedItemStackHandlerBehaviour handler) {
        boolean[] found = {false};
        handler.handleProcessingOnAllItems(item -> {
            if (found[0]) return TransportedResult.doNothing();
            if (!item.stack.isEmpty()) {
                RecipeHolder<?> recipe = PressHammerItem.findPressingRecipe(level, item.stack);
                if (recipe != null) {
                    found[0] = true;
                }
            }
            return TransportedResult.doNothing();
        });
        return found[0];
    }

    public static boolean press(ServerLevel level, LivingEntity user, BlockPos pos, ItemStack stack) {
        List<ItemStack> particleItems = PressHammerItem.findTargetItems(level, pos);

        boolean worked = false;
        if (PressHammerItem.tryPressBasin(level, pos)) {
            worked = true;
        } else if (PressHammerItem.tryPressTransported(level, pos)) {
            worked = true;
        }

        if (!particleItems.isEmpty()) {
            PressHammerItem.broadcastParticles(level, pos, particleItems);
        }
        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, pos, 1f, 0.8f);

        stack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);

        return worked;
    }

    public static boolean isPressBlock(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BasinBlockEntity) return true;
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity) return true;
        if (BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE) != null) return true;
        return false;
    }
}