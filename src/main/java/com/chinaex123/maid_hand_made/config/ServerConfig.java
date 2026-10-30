package com.chinaex123.maid_hand_made.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MORTAR_PROCESSING_TICKS;
    public static final ModConfigSpec.IntValue CRUSHER_MORTAR_PROCESSING_TICKS;
    public static final ModConfigSpec.IntValue STIRRING_STAFF_PROCESSING_TICKS;
    public static final ModConfigSpec.IntValue HAND_SAW_PROCESSING_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("女仆加工单次消耗时长").push("Maid Processing Duration");
        MORTAR_PROCESSING_TICKS = builder
                .comment("研钵研磨时长（tick）")
                .comment("Mortar grinding duration in ticks")
                .defineInRange("mortarProcessingTicks", 100, 1, Integer.MAX_VALUE);
        CRUSHER_MORTAR_PROCESSING_TICKS = builder
                .comment("碾钵研磨时长（tick）")
                .comment("Crusher mortar grinding duration in ticks")
                .defineInRange("crusherMortarProcessingTicks", 100, 1, Integer.MAX_VALUE);
        STIRRING_STAFF_PROCESSING_TICKS = builder
                .comment("搅拌杖搅拌时长（tick）")
                .comment("Stirring staff duration in ticks")
                .defineInRange("stirringStaffProcessingTicks", 60, 1, Integer.MAX_VALUE);
        HAND_SAW_PROCESSING_TICKS = builder
                .comment("手工锯切割时长（tick）")
                .comment("Hand saw cutting duration in ticks")
                .defineInRange("handSawProcessingTicks", 30, 1, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }
}