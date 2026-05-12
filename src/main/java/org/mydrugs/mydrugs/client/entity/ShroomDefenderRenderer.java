package org.mydrugs.mydrugs.client.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.entity.ShroomDefenderEntity;

public final class ShroomDefenderRenderer
        extends AbstractZombieRenderer<ShroomDefenderEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/entity/shroom_defender.png");

    public ShroomDefenderRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), ZombieModel::new),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_BABY_ARMOR, context.getModelSet(), ZombieModel::new)
        );
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
