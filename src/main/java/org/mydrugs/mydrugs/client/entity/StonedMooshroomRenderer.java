package org.mydrugs.mydrugs.client.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;
import org.mydrugs.mydrugs.MyDrugs;

public class StonedMooshroomRenderer extends MushroomCowRenderer {
    private static final ResourceLocation RED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/cow/stoned_red_mooshroom.png");
    private static final ResourceLocation BROWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/cow/stoned_brown_mooshroom.png");

    public StonedMooshroomRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCowRenderState state) {
        return state.variant == MushroomCow.Variant.BROWN ? BROWN_TEXTURE : RED_TEXTURE;
    }
}
