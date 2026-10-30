package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.item.StirringStaffItem;
import com.alben.createhandmade.network.PressParticlesPacket;
import com.chinaex123.maid_hand_made.config.ServerConfig;
import com.chinaex123.maid_hand_made.maid.behavior.MaidUseItemOnMachineBehavior;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 搅拌杖。
 */
public class MaidUseStirringStaffBehavior extends MaidUseItemOnMachineBehavior {

    /** 搅拌时长 */
    private int stirDuration;

    /** 搅拌倒计时 */
    private int stirTicks;
    /** 缓存的配方 */
    private RecipeHolder<?> cachedRecipe;

    public MaidUseStirringStaffBehavior(float movementSpeed) {
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
        return stack.getItem() instanceof StirringStaffItem;
    }

    @Override
    protected boolean isMachine(ServerLevel world, BlockPos pos) {
        // 只看盆非空
        if (!(world.getBlockEntity(pos) instanceof BasinBlockEntity basin)) return false;
        return !basin.isEmpty();
    }

    @Override
    protected boolean isMachineBlock(ServerLevel world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof BasinBlockEntity;
    }

    @Override
    protected boolean useItemOnMachine(ServerLevel world, EntityMaid maid,
                                       BlockPos pos, ItemStack stack) {
        // 锁住移动
        maid.getNavigation().stop();
        maid.setDeltaMovement(0, maid.getDeltaMovement().y, 0);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        if (!(world.getBlockEntity(pos) instanceof BasinBlockEntity basin)) return true;

        // 第一次进时找配方并缓存
        if (stirTicks <= 0) {
            cachedRecipe = StirringStaffItem.findMatchingRecipe(world, basin);
            if (cachedRecipe == null) return true;   // 没配方，结束
            stirDuration = ServerConfig.STIRRING_STAFF_PROCESSING_TICKS.get();
            stirTicks = stirDuration;
        }

        stirTicks--;

        int used = stirDuration - stirTicks;
        int windup = stirDuration / 5;
        if (used >= windup && used % 4 == 0) {
            playStirEffects(world, pos, basin);
        }

        // 还没搅完
        if (stirTicks > 0) {
            return false;
        }

        // 搅完
        stirTicks = 0;
        if (cachedRecipe != null && BasinRecipe.apply(basin, cachedRecipe.value())) {
            basin.notifyChangeOfContents();
            stack.hurtAndBreak(1, maid, EquipmentSlot.MAINHAND);
            completedWork = true;
        }
        cachedRecipe = null;
        return true;
    }

    private void playStirEffects(ServerLevel world, BlockPos pos, BasinBlockEntity basin) {
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (int i = 0; i < basin.inputInventory.getSlots(); i++) {
            ItemStack s = basin.inputInventory.getItem(i);
            if (!s.isEmpty()) stacks.add(s.copy());
        }
        if (stacks.isEmpty()) return;

        AllSoundEvents.MIXING.playOnServer(world, pos, 3.0f, 2f);

        PressParticlesPacket packet = new PressParticlesPacket(pos, stacks, PressParticlesPacket.ParticleStyle.STIR);
        double rangeSq = 64.0 * 64.0;

        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= rangeSq) {
                PacketDistributor.sendToPlayer(p, packet);
            }
        }
    }

    @Override
    protected void onStart(ServerLevel world, EntityMaid maid, BlockPos pos) {
        stirTicks = 0;
        cachedRecipe = null;
    }

    @Override
    protected void stop(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        super.stop(world, maid, time);
        stirTicks = 0;
        cachedRecipe = null;
    }
}