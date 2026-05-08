package org.mydrugs.mydrugs.client.guide;

import net.minecraft.client.Minecraft;

public final class GuideClientOpener {
    private GuideClientOpener() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }
}
