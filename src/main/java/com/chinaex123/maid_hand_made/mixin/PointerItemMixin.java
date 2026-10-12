package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.PointerItem;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PointerItem.class)
public interface PointerItemMixin {

    @Invoker("findRecipe")
    static RecipeHolder<?> invokeFindRecipe(Level level, ItemStack target, ItemStack tool) {
        throw new AssertionError();
    }

    @Invoker("executeDepotProcess")
    static void invokeExecuteDepotProcess(Player player, Level level, BlockPos pos, ItemStack tool, ItemStack held, RecipeHolder<?> recipe, DepotBlockEntity depot) {
        throw new AssertionError();
    }

    @Invoker("tryProcessBelt")
    static boolean invokeTryProcessBelt(Player player, Level level, BlockPos pos, ItemStack tool) {
        throw new AssertionError();
    }
}