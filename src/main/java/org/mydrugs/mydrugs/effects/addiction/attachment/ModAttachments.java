package org.mydrugs.mydrugs.effects.addiction.attachment;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.advancement.DrugKnowledgeAttachment;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachment;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachment;
import org.mydrugs.mydrugs.progression.PsyKnowledgeAttachment;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MyDrugs.MODID);
    public static final Supplier<AttachmentType<PlayerAddictionStats>> PLAYER_ADDICTION =
            ATTACHMENTS.register("player_addiction", () ->
                    AttachmentType.serializable(PlayerAddictionStats::new)
                            .build()
            );
    public static final Supplier<AttachmentType<DrugKnowledgeAttachment>> DRUG_KNOWLEDGE =
            ATTACHMENTS.register("drug_knowledge", () ->
                    AttachmentType.serializable(DrugKnowledgeAttachment::new)
                            .build()
            );
    public static final Supplier<AttachmentType<PsyKnowledgeAttachment>> PLAYER_PSY_KNOWLEDGE =
            ATTACHMENTS.register("player_psy_knowledge", () ->
                    AttachmentType.serializable(PsyKnowledgeAttachment::new)
                            .build()
            );
    public static final Supplier<AttachmentType<MachineTransferAttachment>> MACHINE_TRANSFER =
            ATTACHMENTS.register("machine_transfer", () ->
                    AttachmentType.serializable(MachineTransferAttachment::new)
                            .build()
            );
    public static final Supplier<AttachmentType<MachineEnergyAttachment>> MACHINE_ENERGY =
            ATTACHMENTS.register("machine_energy", () ->
                    AttachmentType.serializable(MachineEnergyAttachment::new)
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
