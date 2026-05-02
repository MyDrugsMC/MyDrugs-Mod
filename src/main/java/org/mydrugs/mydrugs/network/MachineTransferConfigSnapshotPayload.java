package org.mydrugs.mydrugs.network;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferConfig;
import org.mydrugs.mydrugs.pipe.machine.MachineLocalSide;
import org.mydrugs.mydrugs.pipe.machine.MachineOrientation;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferPortSpec;

import java.util.ArrayList;
import java.util.List;

public record MachineTransferConfigSnapshotPayload(int menuId, Direction frontDirection, List<PortState> ports) implements CustomPacketPayload {
    public static final Type<MachineTransferConfigSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "machine_transfer_config_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineTransferConfigSnapshotPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, payload.menuId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.frontDirection().getSerializedName());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.ports().size());
                        for (PortState port : payload.ports()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, port.idPath());
                            ByteBufCodecs.STRING_UTF8.encode(buf, port.translationKey());
                            ByteBufCodecs.VAR_INT.encode(buf, port.rules().length);
                            for (int rule : port.rules()) {
                                ByteBufCodecs.VAR_INT.encode(buf, rule);
                            }
                        }
                    },
                    buf -> {
                        int menuId = ByteBufCodecs.VAR_INT.decode(buf);
                        Direction front = Direction.byName(ByteBufCodecs.STRING_UTF8.decode(buf));
                        if (front == null) {
                            front = Direction.NORTH;
                        }
                        int count = ByteBufCodecs.VAR_INT.decode(buf);
                        List<PortState> ports = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) {
                            String idPath = ByteBufCodecs.STRING_UTF8.decode(buf);
                            String translationKey = ByteBufCodecs.STRING_UTF8.decode(buf);
                            int ruleCount = ByteBufCodecs.VAR_INT.decode(buf);
                            int[] rules = new int[ruleCount];
                            for (int ruleIndex = 0; ruleIndex < ruleCount; ruleIndex++) {
                                rules[ruleIndex] = ByteBufCodecs.VAR_INT.decode(buf);
                            }
                            ports.add(new PortState(idPath, translationKey, rules));
                        }
                        return new MachineTransferConfigSnapshotPayload(menuId, front, ports);
                    }
            );

    public static MachineTransferConfigSnapshotPayload from(BlockEntity target, int menuId) {
        MachineTransferConfig config = MachineTransferAttachments.config(target);
        List<PortState> states = new ArrayList<>();
        for (MachineTransferPortSpec port : MachineTransferAttachments.ports(target)) {
            int[] rules = new int[MachineLocalSide.values().length];
            for (MachineLocalSide side : MachineLocalSide.values()) {
                rules[side.networkId()] = config.getRule(port.id(), side).networkId();
            }
            states.add(new PortState(port.id().id().getPath(), port.translationKey(), rules));
        }
        return new MachineTransferConfigSnapshotPayload(menuId, MachineOrientation.front(target.getBlockState()), states);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record PortState(String idPath, String translationKey, int[] rules) {
    }
}
