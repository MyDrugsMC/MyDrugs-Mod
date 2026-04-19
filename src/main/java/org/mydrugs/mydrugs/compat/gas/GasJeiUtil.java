package org.mydrugs.mydrugs.compat.gas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class GasJeiUtil {
    private GasJeiUtil() {
    }

    public static List<GasJeiIngredient> allIngredients() {
        return ModGases.values().stream()
                .map(GasType::id)
                .map(id -> new GasJeiIngredient(id, GasJeiIngredient.NORMALIZED_AMOUNT))
                .collect(Collectors.toList());
    }

    public static String displayName(ResourceLocation id) {
        GasType gas = ModGases.get(id);
        if (gas != null) {
            Component component = tryComponent(gas, "displayName", "name", "description");
            if (component != null) {
                return component.getString();
            }

            String translationKey = tryString(gas, "descriptionId", "translationKey");
            if (translationKey != null && translationKey.contains(".")) {
                String translated = Component.translatable(translationKey).getString();
                if (!translated.equals(translationKey)) {
                    return translated;
                }
            }
        }

        return humanize(id.getPath());
    }

    public static int color(ResourceLocation id) {
        GasType gas = ModGases.get(id);
        if (gas != null) {
            Integer reflected = gas.tint();
            if (reflected != null) {
                int color = reflected;
                if ((color >>> 24) == 0) {
                    color |= 0xFF000000;
                }
                return color;
            }
        }

        int hash = id.toString().hashCode();
        int r = 90 + (hash & 0x3F);
        int g = 110 + ((hash >> 6) & 0x3F);
        int b = 140 + ((hash >> 12) & 0x3F);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static List<Component> tooltip(GasJeiIngredient ingredient, boolean advanced) {
        ResourceLocation id = ingredient.id();

        var lines = new java.util.ArrayList<Component>();
        lines.add(Component.literal(displayName(id)));
        lines.add(Component.literal(ingredient.amount() + " units").withStyle(ChatFormatting.GRAY));

        if (advanced) {
            lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private static String humanize(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.isEmpty() ? path : sb.toString();
    }

    @Nullable
    private static Component tryComponent(Object target, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method method = target.getClass().getMethod(name);
                Object value = method.invoke(target);
                if (value instanceof Component component) {
                    return component;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private static String tryString(Object target, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method method = target.getClass().getMethod(name);
                Object value = method.invoke(target);
                if (value instanceof String s) {
                    return s;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private static Integer tryInt(Object target, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method method = target.getClass().getMethod(name);
                Object value = method.invoke(target);
                if (value instanceof Number n) {
                    return n.intValue();
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}