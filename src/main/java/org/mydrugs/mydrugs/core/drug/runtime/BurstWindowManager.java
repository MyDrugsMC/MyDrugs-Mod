package org.mydrugs.mydrugs.core.drug.runtime;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.DrugEffectCueKind;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the BURST_WINDOW gameplay: a short attribute-boost window triggered by combat actions
 * (damage taken, dash, melee landed) while the BURST_WINDOW effect is active.
 *
 * Triggering resets the window timer; while the window is active, the underlying BURST_WINDOW
 * active effect drains an extra tick per server tick (so chaining bursts shortens the come-down).
 */
public final class BurstWindowManager {
    public static final int WINDOW_TICKS = 60;
    public static final float MAX_INTENSITY = 1.5F;
    public static final float MOVEMENT_BOOST = 0.35F;
    public static final float ATTACK_DAMAGE_BOOST = 0.40F;
    public static final float ATTACK_SPEED_BOOST = 0.30F;
    public static final int RETRIGGER_COOLDOWN_TICKS = 12;
    public static final int ACTIVE_DRAIN_EXTRA_TICKS = 1;

    private static final ResourceLocation MOVEMENT_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "burst_window_movement_speed");
    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "burst_window_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "burst_window_attack_speed");

    private static final Map<UUID, Window> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    private BurstWindowManager() {
    }

    public static boolean trigger(ServerPlayer player) {
        if (player == null) return false;
        float effectIntensity = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.BURST_WINDOW);
        if (effectIntensity <= 0.0F) return false;
        float clamped = Math.min(MAX_INTENSITY, effectIntensity);
        long now = player.level().getGameTime();
        long lastTrigger = LAST_TRIGGER.getOrDefault(player.getUUID(), -1000L);
        if (now - lastTrigger < RETRIGGER_COOLDOWN_TICKS) {
            return false;
        }
        LAST_TRIGGER.put(player.getUUID(), now);
        ACTIVE.put(player.getUUID(), new Window(now, clamped));
        applyModifiers(player, clamped);
        DrugEffectRuntimeManager.sendCue(player, EffectType.BURST_WINDOW, DrugEffectCueKind.BURST_ACTIVE, clamped);
        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        Window window = ACTIVE.get(id);
        if (window == null) return;

        float effectIntensity = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.BURST_WINDOW);
        long now = player.level().getGameTime();
        long elapsed = now - window.startedAt;

        if (effectIntensity <= 0.0F || elapsed > WINDOW_TICKS) {
            removeModifiers(player);
            ACTIVE.remove(id);
            DrugEffectRuntimeManager.sendCue(player, EffectType.BURST_WINDOW, DrugEffectCueKind.BURST_ENDED, Math.max(0.0F, effectIntensity));
            return;
        }

        // Refresh the modifiers in case effect intensity changed
        float clamped = Math.min(MAX_INTENSITY, effectIntensity);
        if (Math.abs(clamped - window.intensity) > 0.01F) {
            window.intensity = clamped;
            applyModifiers(player, clamped);
        }

        // Drain the underlying BURST_WINDOW active effect by an extra tick: "crash comes faster"
        DrugEffectRuntimeManager.drainEffect(player, EffectType.BURST_WINDOW, ACTIVE_DRAIN_EXTRA_TICKS);
    }

    public static boolean hasActiveWindow(ServerPlayer player) {
        return player != null && ACTIVE.containsKey(player.getUUID());
    }

    public static void cleanup(ServerPlayer player) {
        if (player == null) return;
        ACTIVE.remove(player.getUUID());
        LAST_TRIGGER.remove(player.getUUID());
        removeModifiers(player);
    }

    private static void applyModifiers(ServerPlayer player, float intensity) {
        applyModifier(player, Attributes.MOVEMENT_SPEED, MOVEMENT_ID, MOVEMENT_BOOST * intensity);
        applyModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, ATTACK_DAMAGE_BOOST * intensity);
        applyModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, ATTACK_SPEED_BOOST * intensity);
    }

    private static void applyModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id, double value) {
        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) return;
        instance.removeModifier(id);
        if (Math.abs(value) > 0.005D) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeModifiers(ServerPlayer player) {
        removeModifier(player, Attributes.MOVEMENT_SPEED, MOVEMENT_ID);
        removeModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID);
        removeModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID);
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static final class Window {
        final long startedAt;
        float intensity;

        Window(long startedAt, float intensity) {
            this.startedAt = startedAt;
            this.intensity = intensity;
        }
    }
}
