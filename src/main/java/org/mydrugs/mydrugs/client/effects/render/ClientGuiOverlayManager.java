package org.mydrugs.mydrugs.client.effects.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.mydrugs.mydrugs.client.BiomeFinderCompassOverlay;
import org.mydrugs.mydrugs.client.InnerEntrySequence;
import org.mydrugs.mydrugs.client.InnerRegionCrossingController;
import org.mydrugs.mydrugs.client.effects.hud.ActiveVisualEffectsPanel;
import org.mydrugs.mydrugs.client.effects.hud.AddictionHudRenderer;
import org.mydrugs.mydrugs.client.psy_mixer.PsyMixerRitualOverlay;
import org.mydrugs.mydrugs.client.recovery.RecoveryRoomOverlay;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader;

public final class ClientGuiOverlayManager {
    private ClientGuiOverlayManager() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();

        // World/post visual passes that currently render from the GUI event.
        if (WithdrawalTunnelShader.INSTANCE.shouldRender()) {
            WithdrawalTunnelShader.INSTANCE.render(minecraft);
        }
        if (InnerAtmosphereShader.INSTANCE.shouldRender()) {
            InnerAtmosphereShader.INSTANCE.render(minecraft);
        }

        // Background and full-screen overlays.
        InnerRegionCrossingController.render(graphics);
        FlexibleDrugVisualOverlay.render(graphics);
        VomitOverlayClientState.render(graphics);
        BadTripScreamerOverlay.render(graphics);

        // Readable HUD overlays.
        AddictionHudRenderer.render(graphics);
        ActiveVisualEffectsPanel.render(graphics);
        BiomeFinderCompassOverlay.render(graphics);
        PsyMixerRitualOverlay.render(graphics);
        RecoveryRoomOverlay.render(graphics);

        // Intentionally last: the entry veil covers the HUD while the world resolves.
        InnerEntrySequence.render(graphics);
    }
}
