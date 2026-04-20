package org.mydrugs.mydrugs.core.client;

import org.mydrugs.mydrugs.core.client.shader.Shader;

public class ClientState {
    private Shader shader;

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public boolean hasShader() {
        return shader != null;
    }
}
