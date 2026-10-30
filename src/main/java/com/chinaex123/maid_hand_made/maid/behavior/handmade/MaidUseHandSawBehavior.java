package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.item.HandSawItem;
import com.chinaex123.maid_hand_made.config.ServerConfig;
import com.chinaex123.maid_hand_made.util.HandSawHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 手锯切削。
 */
public class MaidUseHandSawBehavior extends MaidCheckRateTask {

    private int cutDuration;

    private int cutTicks;

    public MaidUseHandSawBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                InitEntities.TARGET_POS.get(), MemoryStatus.REGISTERED
        ));
        setMaxCheckRate(20);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel world, @NotNull EntityMaid maid) {
        if (!super.checkExtraStartConditions(world, maid)) return false;

        // 主手必须是手锯
        ItemStack main = maid.getMainHandItem();
        if (!(main.getItem() instanceof HandSawItem)) return false;

        // 副手必须有可切削物品
        ItemStack off = maid.getOffhandItem();
        if (off.isEmpty()) return false;

        return HandSawHelper.hasCuttingRecipe(world, off);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long time) {
        // 锁移动
        maid.getNavigation().stop();
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        cutDuration = ServerConfig.HAND_SAW_PROCESSING_TICKS.get();
        cutTicks = 0;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        // 主手还是手锯
        ItemStack main = maid.getMainHandItem();
        if (!(main.getItem() instanceof HandSawItem)) return false;

        // 副手还有可切削物品
        ItemStack off = maid.getOffhandItem();
        if (off.isEmpty()) return false;

        return HandSawHelper.hasCuttingRecipe(world, off);
    }

    @Override
    protected boolean timedOut(long time) {
        return false;
    }

    @Override
    protected void tick(ServerLevel world, @NotNull EntityMaid maid, long time) {
        // 锁移动
        maid.getNavigation().stop();
        maid.setDeltaMovement(0, maid.getDeltaMovement().y, 0);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        ItemStack main = maid.getMainHandItem();
        if (!(main.getItem() instanceof HandSawItem)) return;

        ItemStack off = maid.getOffhandItem();
        if (off.isEmpty() || !HandSawHelper.hasCuttingRecipe(world, off)) return;

        cutTicks++;

        // 起手阶段过后开始音效 + 粒子
        if (cutTicks < cutDuration) {
            int windup = cutDuration / 3;
            if (cutTicks >= windup && cutTicks % 4 == 0) {
                playCutEffects(world, maid, off);
            }
            return;
        }

        // 切一份
        cutTicks = 0;
        HandSawHelper.executeCut(world, maid, main);
    }

    @Override
    protected void stop(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        super.stop(world, maid, time);
        cutTicks = 0;
    }

    private void playCutEffects(ServerLevel world, EntityMaid maid, ItemStack input) {
        world.playSound(null, maid.blockPosition(), SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                SoundSource.PLAYERS, 0.8f,
                1.4f + world.random.nextFloat() * 0.2f);

        Vec3 eye = maid.getEyePosition(1f);
        Vec3 look = maid.getLookAngle();
        Vec3 spawnPos = eye.add(look.scale(0.5)).add(0, -0.3, 0);

        for (int i = 0; i < 2; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double speed = 0.05 + world.random.nextDouble() * 0.1;

            world.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, input),
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    1,
                    Math.cos(angle) * speed,
                    0.15 + world.random.nextDouble() * 0.15,
                    Math.sin(angle) * speed,
                    0.0
            );
        }
    }
}