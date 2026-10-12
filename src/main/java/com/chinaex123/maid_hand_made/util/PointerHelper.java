package com.chinaex123.maid_hand_made.util;

import com.alben.createhandmade.network.PressParticlesPacket;
import com.alben.createhandmade.recipe.HandMadeApplicationRecipe;
import com.chinaex123.maid_hand_made.mixin.PointerItemMixin;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PointerHelper {

    private PointerHelper() { }

    public static @NotNull RecipeHolder<?> findRecipe(Level level, ItemStack target, ItemStack tool) {
        return PointerItemMixin.invokeFindRecipe(level, target, tool);
    }

    public static boolean isPointerTarget(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity) return true;
        return BlockEntityBehaviour.get(level, pos,
                TransportedItemStackHandlerBehaviour.TYPE) != null;
    }

    public static boolean depotHasItem(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            return !depot.getHeldItem().isEmpty();
        }
        return false;
    }

    public static boolean beltHasItem(Level level, BlockPos pos) {
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        boolean[] found = {false};
        handler.handleProcessingOnAllItems(item -> {
            if (!found[0] && !item.stack.isEmpty()) {
                found[0] = true;
            }
            return TransportedResult.doNothing();
        });
        return found[0];
    }

    public static boolean processDepot(ServerLevel level, BlockPos pos, EntityMaid maid,
                                       ItemStack tool, DepotBlockEntity depot) {
        ItemStack held = depot.getHeldItem();
        if (held.isEmpty()) return false;

        RecipeHolder<?> recipe = findRecipe(level, held, tool);
        if (recipe == null) return false;

        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        ItemStack particleItem = held.copy();
        boolean[] success = {false};

        handler.handleProcessingOnAllItems(item -> {
            if (success[0]) return TransportedResult.doNothing();
            ItemStack stack = item.stack;
            if (stack.isEmpty()) return TransportedResult.doNothing();

            List<ItemStack> results = RecipeApplier.applyRecipeOn(
                    level, stack.copyWithCount(1), recipe.value(), true);

            success[0] = true;

            ItemStack left = stack.copy();
            left.shrink(1);

            List<TransportedItemStack> outputs = new ArrayList<>();
            for (ItemStack result : results) {
                if (result.isEmpty()) continue;
                TransportedItemStack copy = item.copy();
                copy.stack = result;
                outputs.add(copy);
            }

            if (outputs.isEmpty()) {
                if (left.isEmpty()) return TransportedResult.removeItem();
                TransportedItemStack leftStack = item.copy();
                leftStack.stack = left;
                return TransportedResult.convertTo(leftStack);
            }

            if (left.isEmpty()) {
                return TransportedResult.convertTo(outputs);
            } else {
                TransportedItemStack leftStack = item.copy();
                leftStack.stack = left;
                return TransportedResult.convertToAndLeaveHeld(outputs, leftStack);
            }
        });

        if (success[0]) {
            depot.notifyUpdate();
            depot.sendData();
            consumeTool(maid, tool, recipe);
            playProcessEffects(level, pos, particleItem);
        }
        return success[0];
    }

    public static boolean processBelt(ServerLevel level, BlockPos pos, EntityMaid maid, ItemStack tool) {
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        ItemStack[] particleItem = {ItemStack.EMPTY};
        boolean[] success = {false};

        handler.handleProcessingOnAllItems(item -> {
            if (success[0]) return TransportedResult.doNothing();
            ItemStack held = item.stack;
            if (held.isEmpty()) return TransportedResult.doNothing();

            RecipeHolder<?> recipe = findRecipe(level, held, tool);
            if (recipe == null) return TransportedResult.doNothing();

            List<ItemStack> results = RecipeApplier.applyRecipeOn(
                    level, held.copyWithCount(1), recipe.value(), true);

            success[0] = true;
            particleItem[0] = held.copy();

            consumeTool(maid, tool, recipe);

            ItemStack left = held.copy();
            left.shrink(1);

            List<TransportedItemStack> outputs = new ArrayList<>();
            for (ItemStack result : results) {
                if (result.isEmpty()) continue;
                TransportedItemStack copy = item.copy();
                copy.stack = result;
                outputs.add(copy);
            }

            if (outputs.isEmpty()) {
                if (left.isEmpty()) return TransportedResult.removeItem();
                TransportedItemStack leftStack = item.copy();
                leftStack.stack = left;
                return TransportedResult.convertTo(leftStack);
            }

            if (left.isEmpty()) {
                return TransportedResult.convertTo(outputs);
            } else {
                TransportedItemStack leftStack = item.copy();
                leftStack.stack = left;
                return TransportedResult.convertToAndLeaveHeld(outputs, leftStack);
            }
        });

        if (success[0]) {
            playProcessEffects(level, pos, particleItem[0]);
        }
        return success[0];
    }

    public static void consumeTool(EntityMaid maid, ItemStack tool, RecipeHolder<?> recipe) {
        if (recipe.value() instanceof HandMadeApplicationRecipe handmade && handmade.shouldKeepHeldItem()) {
            return;
        }
        if (recipe.value() instanceof ItemApplicationRecipe ia && ia.shouldKeepHeldItem()) {
            return;
        }
        if (tool.getMaxDamage() > 0) {
            tool.hurtAndBreak(1, maid, EquipmentSlot.OFFHAND);
        } else {
            tool.shrink(1);
        }
    }

    public static void playProcessEffects(Level level, BlockPos pos, ItemStack particleItem) {
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (particleItem.isEmpty()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        PressParticlesPacket packet = new PressParticlesPacket(
                pos, particleItem, PressParticlesPacket.ParticleStyle.DEPLOY);
        double rangeSq = 64.0 * 64.0;

        for (ServerPlayer p : serverLevel.players()) {
            if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= rangeSq) {
                PacketDistributor.sendToPlayer(p, packet);
            }
        }
    }
}