package org.mydrugs.mydrugs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MyDrugs.MODID)
public class Config {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();

        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        SERVER = new Server(serverBuilder);
        SERVER_SPEC = serverBuilder.build();
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue enableDrugShaders;
        public final ModConfigSpec.DoubleValue shaderIntensity;
        public final ModConfigSpec.BooleanValue enableCameraShake;
        public final ModConfigSpec.DoubleValue cameraShakeIntensity;
        public final ModConfigSpec.BooleanValue enableHallucinations;
        public final ModConfigSpec.DoubleValue hallucinationIntensity;
        public final ModConfigSpec.BooleanValue enableHeartbeatSounds;
        public final ModConfigSpec.BooleanValue enableDrugSounds;
        public final ModConfigSpec.BooleanValue showAddictionHud;
        public final ModConfigSpec.BooleanValue compactAddictionHud;
        public final ModConfigSpec.BooleanValue reducedMotionMode;
        public final ModConfigSpec.BooleanValue enableBadTripScreamers;
        public final ModConfigSpec.DoubleValue screamerIntensity;

        private Client(ModConfigSpec.Builder builder) {
            builder.push("accessibility");
            enableDrugShaders = builder.define("enableDrugShaders", true);
            shaderIntensity = builder.defineInRange("shaderIntensity", 1.0D, 0.0D, 2.0D);
            enableCameraShake = builder.define("enableCameraShake", true);
            cameraShakeIntensity = builder.defineInRange("cameraShakeIntensity", 1.0D, 0.0D, 2.0D);
            enableHallucinations = builder.define("enableHallucinations", true);
            hallucinationIntensity = builder.defineInRange("hallucinationIntensity", 1.0D, 0.0D, 2.0D);
            enableHeartbeatSounds = builder.define("enableHeartbeatSounds", true);
            enableDrugSounds = builder.define("enableDrugSounds", true);
            showAddictionHud = builder.define("showAddictionHud", true);
            compactAddictionHud = builder.define("compactAddictionHud", false);
            reducedMotionMode = builder.define("reducedMotionMode", false);
            enableBadTripScreamers = builder.define("enableBadTripScreamers", true);
            screamerIntensity = builder.defineInRange("screamerIntensity", 1.0D, 0.0D, 2.0D);
            builder.pop();
        }
    }

    public static final class Server {
        public final ModConfigSpec.BooleanValue addictionEnabled;
        public final ModConfigSpec.BooleanValue overdoseEnabled;
        public final ModConfigSpec.BooleanValue withdrawalEnabled;
        public final ModConfigSpec.DoubleValue addictionGainMultiplier;
        public final ModConfigSpec.DoubleValue toleranceGainMultiplier;
        public final ModConfigSpec.DoubleValue toleranceDecayMultiplier;
        public final ModConfigSpec.DoubleValue withdrawalSeverityMultiplier;
        public final ModConfigSpec.DoubleValue overdoseThresholdMultiplier;
        public final ModConfigSpec.DoubleValue safeZoneRecoveryMultiplier;
        public final ModConfigSpec.DoubleValue therapyCooldownMultiplier;
        public final ModConfigSpec.BooleanValue allowDebugActionPayloads;

        private Server(ModConfigSpec.Builder builder) {
            builder.push("gameplay");
            addictionEnabled = builder.define("addictionEnabled", true);
            overdoseEnabled = builder.define("overdoseEnabled", true);
            withdrawalEnabled = builder.define("withdrawalEnabled", true);
            addictionGainMultiplier = builder.defineInRange("addictionGainMultiplier", 1.0D, 0.0D, 100.0D);
            toleranceGainMultiplier = builder.defineInRange("toleranceGainMultiplier", 1.0D, 0.0D, 100.0D);
            toleranceDecayMultiplier = builder.defineInRange("toleranceDecayMultiplier", 1.0D, 0.0D, 100.0D);
            withdrawalSeverityMultiplier = builder.defineInRange("withdrawalSeverityMultiplier", 1.0D, 0.0D, 100.0D);
            overdoseThresholdMultiplier = builder.defineInRange("overdoseThresholdMultiplier", 1.0D, 0.1D, 100.0D);
            safeZoneRecoveryMultiplier = builder.defineInRange("safeZoneRecoveryMultiplier", 1.0D, 0.0D, 100.0D);
            therapyCooldownMultiplier = builder.defineInRange("therapyCooldownMultiplier", 1.0D, 0.0D, 100.0D);
            builder.pop();

            builder.push("admin");
            allowDebugActionPayloads = builder
                    .comment("Allow privileged players (permission level >= 2) to send debug action payloads that mutate addiction stats. Default false — keep disabled on public servers.")
                    .define("allowDebugActionPayloads", false);
            builder.pop();
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
