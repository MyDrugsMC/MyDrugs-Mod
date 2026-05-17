package org.mydrugs.mydrugs.psyche;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerPsycheMapAttachment implements ValueIOSerializable {

    public static final class Node {
        public final String nodeId;
        public final long unlockedAtGameTime;
        public final long unlockedDay;
        public final String trigger;
        public final String dominantDrugId;

        public Node(String nodeId, long unlockedAtGameTime, long unlockedDay, String trigger, String dominantDrugId) {
            this.nodeId = nodeId == null ? "" : nodeId;
            this.unlockedAtGameTime = unlockedAtGameTime;
            this.unlockedDay = unlockedDay;
            this.trigger = trigger == null ? "" : trigger;
            this.dominantDrugId = dominantDrugId == null ? "" : dominantDrugId;
        }
    }

    private final Map<String, Node> nodes = new LinkedHashMap<>();

    public boolean has(ResourceLocation nodeId) {
        return this.nodes.containsKey(nodeId.toString());
    }

    public boolean unlock(ResourceLocation nodeId, long gameTime, long day, String trigger, String dominantDrugId) {
        String key = nodeId.toString();
        if (this.nodes.containsKey(key)) return false;
        this.nodes.put(key, new Node(key, gameTime, day, trigger, dominantDrugId));
        return true;
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(this.nodes.values());
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("count", this.nodes.size());
        int i = 0;
        for (Node n : this.nodes.values()) {
            String p = "node_" + i++;
            output.putString(p + "_id", n.nodeId);
            output.putLong(p + "_gt", n.unlockedAtGameTime);
            output.putLong(p + "_day", n.unlockedDay);
            output.putString(p + "_trig", n.trigger);
            output.putString(p + "_drug", n.dominantDrugId);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        this.nodes.clear();
        int count = input.getIntOr("count", 0);
        for (int i = 0; i < count; i++) {
            String p = "node_" + i;
            String id = input.getStringOr(p + "_id", "");
            if (id.isEmpty()) continue;
            long gt = input.getLongOr(p + "_gt", 0L);
            long day = input.getLongOr(p + "_day", 0L);
            String trig = input.getStringOr(p + "_trig", "");
            String drug = input.getStringOr(p + "_drug", "");
            this.nodes.put(id, new Node(id, gt, day, trig, drug));
        }
    }
}
