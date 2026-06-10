package org.mydrugs.mydrugs.client.accessibility;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class AccessibilityKeybindClient {
    private static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.mydrugs.open_accessibility",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC
    );

    private AccessibilityKeybindClient() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null) {
                minecraft.setScreen(new AccessibilityScreen(null));
            }
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AccessibilityScreen)
                && !OPEN_KEY.isUnbound()
                && OPEN_KEY.matches(event.getKeyEvent())) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new AccessibilityScreen(event.getScreen()));
            event.setCanceled(true);
        }
    }
}
