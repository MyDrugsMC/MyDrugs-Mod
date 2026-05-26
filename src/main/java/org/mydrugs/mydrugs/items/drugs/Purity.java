package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

public final class Purity {
    public static final float DEFAULT = 1.0F;
    public static final float STREET_MAX = 0.40F;
    public static final float CUT_MAX = 0.75F;

    private Purity() {
    }

    public static float of(ItemStack stack) {
        Float p = stack.get(ModDataComponents.PURITY.get());
        if (p == null) {
            return DEFAULT;
        }
        return clamp(p);
    }

    public static void set(ItemStack stack, float purity) {
        stack.set(ModDataComponents.PURITY.get(), clamp(purity));
    }

    public static boolean has(ItemStack stack) {
        return stack.has(ModDataComponents.PURITY.get());
    }

    public static void copyIfPresent(ItemStack source, ItemStack target) {
        Float p = source.get(ModDataComponents.PURITY.get());
        if (p != null) {
            target.set(ModDataComponents.PURITY.get(), clamp(p));
        }
    }

    public static Band band(float purity) {
        if (purity <= STREET_MAX) return Band.STREET;
        if (purity < CUT_MAX) return Band.CUT;
        return Band.PURE;
    }

    public static float clamp(float p) {
        if (p < 0.0F) return 0.0F;
        if (p > 1.0F) return 1.0F;
        return p;
    }

    public enum Band {
        STREET("street"),
        CUT("cut"),
        PURE("pure");

        private final String key;

        Band(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
