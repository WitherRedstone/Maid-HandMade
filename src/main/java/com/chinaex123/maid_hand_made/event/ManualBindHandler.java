package com.chinaex123.maid_hand_made.event;

import com.alben.createhandmade.item.BellowsItem;
import com.alben.createhandmade.item.PointerItem;
import com.alben.createhandmade.item.PressHammerItem;
import com.alben.createhandmade.item.StirringStaffItem;
import com.chinaex123.maid_hand_made.init.ModDataComponents;
import com.chinaex123.maid_hand_made.util.PointerHelper;
import com.chinaex123.maid_hand_made.util.PressHammerHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class ManualBindHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (!player.isShiftKeyDown()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack stack = player.getMainHandItem();
        if (!isBindableTool(stack)) return;

        if (clearIfBound(stack)) {
            onCleared(player);
            if (!isValidBindTarget(stack.getItem(), serverLevel, event.getPos())) {
                event.setCanceled(true);
                return;
            }
        } else {
            if (!isValidBindTarget(stack.getItem(), serverLevel, event.getPos())) return;
        }

        doBind(event, player, serverLevel, stack);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (!player.isShiftKeyDown()) return;

        ItemStack stack = event.getItemStack();
        if (!isBindableTool(stack)) return;

        if (clearIfBound(stack)) {
            onCleared(player);
            event.setCanceled(true);
        }
    }

    private static boolean isBindableTool(ItemStack stack) {
        return stack.getItem() instanceof PressHammerItem
                || stack.getItem() instanceof StirringStaffItem
                || stack.getItem() instanceof BellowsItem
                || stack.getItem() instanceof PointerItem;
    }

    private static boolean isValidBindTarget(Object item, ServerLevel level, BlockPos pos) {
        if (item instanceof StirringStaffItem) {
            return level.getBlockEntity(pos) instanceof BasinBlockEntity;
        }
        if (item instanceof PressHammerItem) {
            return PressHammerHelper.isPressBlock(level, pos);
        }
        if (item instanceof BellowsItem) {
            return isBellowsTarget(level, pos);
        }
        if (item instanceof PointerItem) {
            return PointerHelper.isPointerTarget(level, pos);
        }
        return false;
    }

    private static void doBind(PlayerInteractEvent.RightClickBlock event,
                               Player player, ServerLevel level, ItemStack stack) {
        BlockPos pos = event.getPos();
        setBound(player, level, pos, stack);
        if (!(stack.getItem() instanceof PointerItem)) {
            event.setCanceled(true);
        }
    }

    private static boolean clearIfBound(ItemStack stack) {
        BlockPos bound = stack.get(ModDataComponents.BOUND_POS.get());
        if (bound == null) return false;
        stack.remove(ModDataComponents.BOUND_POS.get());
        stack.remove(ModDataComponents.BOUND_TEXT.get());
        return true;
    }

    private static void onCleared(Player player) {
        player.displayClientMessage(
                Component.translatable("message.maid_hand_made.bound_cleared"), true);
    }

    private static boolean isBellowsTarget(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity) return true;
        return BlockEntityBehaviour.get(level, pos,
                TransportedItemStackHandlerBehaviour.TYPE) != null;
    }

    private static void setBound(Player player, ServerLevel level, BlockPos pos, ItemStack stack) {
        BlockState state = level.getBlockState(pos);
        Component name = state.getBlock().getName();

        stack.set(ModDataComponents.BOUND_POS.get(), pos.immutable());
        stack.set(ModDataComponents.BOUND_TEXT.get(),
                name.getString() + "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]");

        player.displayClientMessage(
                Component.translatable("message.maid_hand_made.bound_success",
                        name.getString(), pos.toShortString()), true);
    }
}