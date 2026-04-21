package org.mydrugs.mydrugs.core.client.shader;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.Core;
import org.mydrugs.mydrugs.core.client.ClientState;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.*;

// Works ONLY ON CLIENT. NOT SERVER
public abstract class ClientShaderManager<T extends Shader> {
    private final ClientState clientState;

    // All registered shaders by effect type
    private final Map<EffectType, T> shaders = new HashMap<>();

    // Currently active shaders, insertion order = render order
    private final Map<EffectType, ActiveShader<T>> activeShaders = new LinkedHashMap<>();

    public ClientShaderManager(ClientState clientState) {
        this.clientState = clientState;
    }

    public void register(EffectType type, T shader) {
        shaders.put(type, shader);
    }

    public void tick() {
        if (activeShaders.isEmpty()) {
            syncClientState();
            return;
        }

        Iterator<Map.Entry<EffectType, ActiveShader<T>>> iterator = activeShaders.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<EffectType, ActiveShader<T>> entry = iterator.next();
            ActiveShader<T> active = entry.getValue();

            active.ticksLeft--;

            if (active.ticksLeft <= 0) {
                iterator.remove();
            }
        }

        syncClientState();
    }

    public Shader getCurrentShader() {
        if (activeShaders.isEmpty()) {
            return null;
        }

        return activeShaders.values().iterator().next().shader;
    }

    public List<T> getActiveShaders() {
        List<T> result = new ArrayList<>(activeShaders.size());

        for (ActiveShader<T> active : activeShaders.values()) {
            result.add(active.shader);
        }

        return result;
    }

    public boolean hasActiveShaders() {
        return !activeShaders.isEmpty();
    }

    public boolean isActive(EffectType type) {
        return activeShaders.containsKey(type);
    }

    public int getTicksLeft(EffectType type) {
        ActiveShader<T> active = activeShaders.get(type);
        return active == null ? 0 : active.ticksLeft;
    }

    public void add(int durationTicks, EffectType type) {
        if (durationTicks <= 0) {
            return;
        }

        T shader = shaders.get(type);

        if (shader == null) {
            Core.getLOGGER().warning("Shader " + type.name() + " is not initialized!");
            return;
        }

        ActiveShader<T> existing = activeShaders.get(type);

        if (existing != null) {
            existing.ticksLeft = Math.max(existing.ticksLeft, durationTicks);
        } else {
            activeShaders.put(type, new ActiveShader<>(shader, durationTicks));
        }

        syncClientState();
    }

    public void remove(EffectType type) {
        activeShaders.remove(type);
        syncClientState();
    }

    public void clearActive() {
        activeShaders.clear();
        syncClientState();
    }

    public List<T> getShaders() {
        return new ArrayList<>(shaders.values());
    }

    /** Returns the registered shader for the given type, or {@code null} if not registered. */
    protected @Nullable T getRegisteredShader(EffectType type) {
        return shaders.get(type);
    }

    private void syncClientState() {
        if (activeShaders.isEmpty()) {
            clientState.setShader(null);
            return;
        }

        // Compatibility mirror for old code paths that still check ClientState.
        clientState.setShader(activeShaders.values().iterator().next().shader);
    }

    private static final class ActiveShader<T extends Shader> {
        private final T shader;
        private int ticksLeft;

        private ActiveShader(T shader, int ticksLeft) {
            this.shader = shader;
            this.ticksLeft = ticksLeft;
        }
    }
}