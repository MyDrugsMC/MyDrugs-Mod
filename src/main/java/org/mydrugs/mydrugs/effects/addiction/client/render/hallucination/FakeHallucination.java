package org.mydrugs.mydrugs.effects.addiction.client.render.hallucination;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FakeHallucination {
    public final Vec3 position;
    public final long spawnAt;
    public final float scale;
    public final float yOffset;
    public final float phase;
    public final boolean hasEyes;
    public long expireAt;
    public int staredAtTicks;

    public FakeHallucination(
            Vec3 position,
            long spawnAt,
            long expireAt,
            float scale,
            float yOffset,
            float phase,
            boolean hasEyes
    ) {
        this.position = position;
        this.spawnAt = spawnAt;
        this.expireAt = expireAt;
        this.scale = scale;
        this.yOffset = yOffset;
        this.phase = phase;
        this.hasEyes = hasEyes;
    }

    public boolean expired(long gameTime) {
        return gameTime >= expireAt;
    }

    public float alpha(long gameTime) {
        float now = (float) gameTime;

        float fadeIn = Mth.clamp((now - spawnAt) / 8.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((expireAt - now) / 10.0F, 0.0F, 1.0F);

        return fadeIn * fadeOut;
    }

    public AABB bounds() {
        double halfWidth = 0.42D * scale;
        double height = 2.15D * scale;

        return new AABB(
                position.x - halfWidth,
                position.y + yOffset,
                position.z - halfWidth,
                position.x + halfWidth,
                position.y + yOffset + height,
                position.z + halfWidth
        ).inflate(0.15D);
    }
}