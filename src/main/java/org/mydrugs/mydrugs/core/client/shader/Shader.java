package org.mydrugs.mydrugs.core.client.shader;

public abstract class Shader {
    private final String name;
    private final String formattedUniformName;

    protected float time = 0.0F;
    protected float deltaTime = 0.05F;

    public Shader(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Shader name cannot be null or blank.");
        }

        this.name = name;
        this.formattedUniformName = formatUniformName(name);
    }

    private static String formatUniformName(String name) {
        StringBuilder result = new StringBuilder();

        for (String word : name.split("_")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }

        return result.append("Uniforms").toString();
    }

    public String getName() {
        return name;
    }

    public String getFormattedUniformName() {
        return formattedUniformName;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }
}