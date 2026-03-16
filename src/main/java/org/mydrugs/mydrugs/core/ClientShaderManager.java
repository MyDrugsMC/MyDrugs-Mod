package org.mydrugs.mydrugs.core;

import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.List;

// Works ONLY ON CLIENT. NOT SERVER
public abstract class ClientShaderManager<T extends Shader> {
    private float ticksLeft = 0.0F;
    private T currentShader = null;
    private final List<T> shaders = new ArrayList<>();

    public void register(T shader) {
        shaders.add(shader);
    }

    public void tick() {
        if (ticksLeft <= 0) {
            return;
        }

        ticksLeft--;

        if (currentShader == null) {
            return;
        }

        if (ticksLeft <= 0) {
            currentShader.setEnabled(false);
            currentShader = null;
        }
    }

    public T getCurrentShader() {
        return currentShader;
    }

    public void start(int durationTicks, Class<? extends T> clazz) {
        ticksLeft = durationTicks;

        T shader = shaders.stream()
                .filter(sh -> sh.getClass() == clazz)
                .findFirst()
                .orElse(null);

        if (shader == null) {
            MyDrugs.getLOGGER().warn("Shader {} is not initialized!", clazz.getName());
            return;
        }

        if (currentShader != null) {
            currentShader.setEnabled(false);
        }

        currentShader = shader;
        currentShader.setEnabled(true);
    }

    public List<T> getShaders() {
        return shaders;
    }
}