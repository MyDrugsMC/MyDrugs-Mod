package org.mydrugs.mydrugs.client.entity;

import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CowVariant;
import org.mydrugs.mydrugs.MyDrugs;

public class StonedCowRenderer extends CowRenderer {
    private static final ResourceLocation TEMPERATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/cow/stoned_temperate_cow.png");
    private static final ResourceLocation COLD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/cow/stoned_cold_cow.png");
    private static final ResourceLocation WARM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/cow/stoned_warm_cow.png");

    public StonedCowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CowRenderState state) {
        if (state.variant == null) {
            return MissingTextureAtlasSprite.getLocation();
        }
        return switch (state.variant.modelAndTexture().model()) {
            case COLD -> COLD_TEXTURE;
            case WARM -> WARM_TEXTURE;
            case NORMAL -> TEMPERATE_TEXTURE;
        };
    }
}
