package org.mydrugs.mydrugs.progression;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.List;
import java.util.Objects;

public record PsyKnowledgeKey(ResourceLocation id) {
    public static final PsyKnowledgeKey NICOTINIC = create("nicotinic");
    public static final PsyKnowledgeKey CANNABINOID = create("cannabinoid");
    public static final PsyKnowledgeKey FERMENTED = create("fermented");
    public static final PsyKnowledgeKey STIMULANT = create("stimulant");
    public static final PsyKnowledgeKey LYSERGIC = create("lysergic");
    public static final PsyKnowledgeKey OVERCLOCKED = create("overclocked");
    public static final PsyKnowledgeKey MYCELIAL = create("mycelial");

    public static final List<PsyKnowledgeKey> ORDERED = List.of(
            NICOTINIC,
            CANNABINOID,
            FERMENTED,
            STIMULANT,
            LYSERGIC,
            OVERCLOCKED,
            MYCELIAL
    );

    public PsyKnowledgeKey {
        Objects.requireNonNull(id, "id");
    }

    public static PsyKnowledgeKey create(String path) {
        return new PsyKnowledgeKey(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path));
    }

    public String translationKey() {
        return "knowledge." + this.id.getNamespace() + "." + this.id.getPath();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
