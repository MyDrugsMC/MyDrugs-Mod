package org.mydrugs.mydrugs.effects.addiction.attachment;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;

import java.util.function.Supplier;

public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MyDrugs.MODID);

    public static final Supplier<AttachmentType<PlayerAddictionStats>> PLAYER_ADDICTION =
            ATTACHMENTS.register("player_addiction", () ->
                    AttachmentType.serializable(PlayerAddictionStats::new)
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}