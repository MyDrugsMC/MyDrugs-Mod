package org.mydrugs.mydrugs.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.Map;

public final class ModArmorMaterials {
    public static final ResourceKey<EquipmentAsset> ALUMINIUM_ASSET = equipmentAsset("aluminium");
    public static final ResourceKey<EquipmentAsset> PLATINUM_ASSET = equipmentAsset("platinum");

    public static final ArmorMaterial ALUMINIUM = new ArmorMaterial(
            14,
            Map.of(
                    ArmorType.BOOTS, 2,
                    ArmorType.LEGGINGS, 4,
                    ArmorType.CHESTPLATE, 5,
                    ArmorType.HELMET, 2
            ),
            12,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            itemTag("ingots/aluminium"),
            ALUMINIUM_ASSET
    );

    public static final ArmorMaterial PLATINUM = new ArmorMaterial(
            30,
            Map.of(
                    ArmorType.BOOTS, 3,
                    ArmorType.LEGGINGS, 5,
                    ArmorType.CHESTPLATE, 7,
                    ArmorType.HELMET, 3
            ),
            15,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            1.5F,
            0.0F,
            itemTag("ingots/platinum"),
            PLATINUM_ASSET
    );

    private ModArmorMaterials() {
    }

    private static ResourceKey<EquipmentAsset> equipmentAsset(String name) {
        return ResourceKey.create(
                EquipmentAssets.ROOT_ID,
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name)
        );
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
