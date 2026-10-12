package com.chinaex123.maid_hand_made.init;

import com.chinaex123.maid_hand_made.MaidHandMade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MaidHandMade.MODID);

    /** 锤子绑定的机器描述文本（给玩家看的） */
    public static final Supplier<DataComponentType<String>> BOUND_TEXT =
            DATA_COMPONENTS.register("bound_text",
                    () -> DataComponentType.<String>builder()
                            .persistent(com.mojang.serialization.Codec.STRING)
                            .build());

    /** 绑定的机器坐标（给逻辑用） */
    public static final Supplier<DataComponentType<BlockPos>> BOUND_POS =
            DATA_COMPONENTS.register("bound_pos",
                    () -> DataComponentType.<BlockPos>builder()
                            .persistent(BlockPos.CODEC)
                            .build());
}