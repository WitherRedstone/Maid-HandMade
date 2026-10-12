package com.chinaex123.maid_hand_made.maid.task;

import com.chinaex123.maid_hand_made.MaidHandMade;
import com.chinaex123.maid_hand_made.maid.behavior.handmade.*;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaskCreateHandMade implements IMaidTask {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MaidHandMade.MODID, "hand_made");

    /**
     * 获取任务唯一标识符。
     *
     * @return 任务标识符
     */
    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    /**
     * 获取任务图标。
     * <p>
     * 使用森罗酒馆的果盆物品作为图标。
     *
     * @return 图标物品堆
     */
    @Override
    public @NotNull ItemStack getIcon() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create_hand_made", "pointer")).getDefaultInstance();
    }

    /**
     * 获取女仆执行该任务时的环境音效。
     *
     * @param maid 女仆实体
     * @return 环境音效事件
     */
    @NotNull
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_IDLE.get(), 0.5f);
    }

    /**
     * 创建女仆的 AI 行为任务列表。
     *
     * @param maid 女仆实体实例
     * @return 包含优先级和行为控制的配对列表，优先级 5 表示中等优先级
     */
    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(5, new MaidUsePressHammerBehavior(0.6f)),
                Pair.of(5, new MaidUseMortarBehavior()),
                Pair.of(5, new MaidUseCrusherMortarBehavior()),
                Pair.of(5, new MaidUsePointerBehavior(0.6f)),
                Pair.of(5, new MaidUseStirringStaffBehavior(0.6f)),
                Pair.of(5, new MaidUseInfusionGunBehavior(0.6f)),
                Pair.of(5, new MaidUseBellowsBehavior(0.6f)),
                Pair.of(5, new MaidUseHandSawBehavior())
        );
    }

    /**
     * 获取任务的动作摘要文本。
     *
     * @return 动作摘要
     */
    @Override
    public @NotNull String getMaidActionSummary() {
        return "Operate machines with Create: Hand Made items";
    }
}
