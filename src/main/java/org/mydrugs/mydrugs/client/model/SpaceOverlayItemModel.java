package org.mydrugs.mydrugs.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SpaceOverlayItemModel(
        List<BakedQuad> quads,
        ModelRenderProperties properties,
        int overlayColor
) implements ItemModel {

    @Override
    public void update(
            ItemStackRenderState state,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        state.appendModelIdentityElement(this);
        state.appendModelIdentityElement(overlayColor);

        LayerRenderState layer = state.newLayer();

        layer.setExtents(() -> BlockModelWrapper.computeExtents(this.quads));
        layer.setRenderType(Sheets.translucentItemSheet());
        this.properties.applyToLayer(layer, displayContext);

        int[] tintLayers = layer.prepareTintLayers(1);
        tintLayers[0] = ARGB.opaque(this.overlayColor);

        layer.prepareQuadList().addAll(this.quads);
    }

    public record Unbaked(
            ResourceLocation model,
            int overlayColor
    ) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::model),
                        Codec.INT.optionalFieldOf("overlay_color", 0x55FF55).forGetter(Unbaked::overlayColor)
                ).apply(instance, Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolvedModel = baker.getModel(this.model);
            TextureSlots slots = resolvedModel.getTopTextureSlots();

            return new SpaceOverlayItemModel(
                    resolvedModel.bakeTopGeometry(slots, baker, BlockModelRotation.X0_Y0).getAll(),
                    ModelRenderProperties.fromResolvedModel(baker, resolvedModel, slots),
                    this.overlayColor
            );
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}