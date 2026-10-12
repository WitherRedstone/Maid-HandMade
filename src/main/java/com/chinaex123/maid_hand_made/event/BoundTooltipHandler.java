package com.chinaex123.maid_hand_made.event;

import com.alben.createhandmade.item.BellowsItem;
import com.alben.createhandmade.item.PointerItem;
import com.alben.createhandmade.item.PressHammerItem;
import com.alben.createhandmade.item.StirringStaffItem;
import com.chinaex123.maid_hand_made.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber
public class BoundTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isBindableTool(stack)) return;

        String text = stack.get(ModDataComponents.BOUND_TEXT.get());
        if (text == null) return;

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(
                Component.translatable("tooltip.maid_hand_made.bound", text)
                        .withStyle(ChatFormatting.GOLD));
    }

    private static boolean isBindableTool(ItemStack stack) {
        return stack.getItem() instanceof PressHammerItem
                || stack.getItem() instanceof StirringStaffItem
                || stack.getItem() instanceof BellowsItem
                || stack.getItem() instanceof PointerItem;
    }
}