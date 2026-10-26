package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.PressHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PressHammerItem.class)
public abstract class PressHammerItemMixin {

    @Invoker("tryPressBasin")
    public static boolean invokeTryPressBasin(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("tryPressTransported")
    public static boolean invokeTryPressTransported(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("findTargetItems")
    public static List<ItemStack> invokeFindTargetItems(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("broadcastParticles")
    public static void invokeBroadcastParticles(Level level, BlockPos pos, List<ItemStack> stacks) {
        throw new AssertionError();
    }

    @Invoker("findPressingRecipe")
    public static RecipeHolder<?> invokeFindPressingRecipe(Level level, ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (player.isShiftKeyDown()) {
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
        }
    }
}