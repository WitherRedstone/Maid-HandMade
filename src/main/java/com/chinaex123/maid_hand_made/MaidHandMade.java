package com.chinaex123.maid_hand_made;

import com.chinaex123.maid_hand_made.init.ModAttachments;
import com.chinaex123.maid_hand_made.init.ModDataComponents;
import com.chinaex123.maid_hand_made.init.ModNetwork;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(MaidHandMade.MODID)
public class MaidHandMade {
    public static final String MODID = "maid_hand_made";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidHandMade(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModNetwork.register(modEventBus);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MaidHandMade.MODID, name);
    }
}