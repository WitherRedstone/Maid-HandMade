package com.chinaex123.maid_hand_made.init;

import com.chinaex123.maid_hand_made.MaidHandMade;
import com.chinaex123.maid_hand_made.client.BoundHighlightHandler;
import com.chinaex123.maid_hand_made.network.BoundHighlightPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {

    private ModNetwork() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(RegisterPayloadHandlersEvent.class, event ->
                event.registrar(MaidHandMade.MODID)
                        .playToClient(BoundHighlightPacket.TYPE, BoundHighlightPacket.CODEC,
                                ModNetwork::handleBoundHighlight)
        );
    }

    private static void handleBoundHighlight(BoundHighlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                BoundHighlightHandler.highlight(packet.pos(), packet.durationTicks()));
    }
}