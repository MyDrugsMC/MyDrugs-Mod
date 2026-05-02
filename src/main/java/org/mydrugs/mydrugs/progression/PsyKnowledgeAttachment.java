package org.mydrugs.mydrugs.progression;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PsyKnowledgeAttachment implements ValueIOSerializable {
    private final Set<ResourceLocation> known = new LinkedHashSet<>();

    public boolean has(PsyKnowledgeKey key) {
        return this.known.contains(key.id());
    }

    public boolean grant(PsyKnowledgeKey key) {
        return this.known.add(key.id());
    }

    public Set<PsyKnowledgeKey> getKnown() {
        Set<PsyKnowledgeKey> result = new LinkedHashSet<>();
        for (ResourceLocation id : this.known) {
            result.add(new PsyKnowledgeKey(id));
        }
        return Set.copyOf(result);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("count", this.known.size());
        int index = 0;
        for (ResourceLocation id : this.known) {
            output.putString("knowledge_" + index++, id.toString());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        this.known.clear();

        int count = input.getIntOr("count", 0);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = ResourceLocation.tryParse(input.getStringOr("knowledge_" + i, ""));
            if (id != null) {
                this.known.add(id);
            }
        }
    }
}
