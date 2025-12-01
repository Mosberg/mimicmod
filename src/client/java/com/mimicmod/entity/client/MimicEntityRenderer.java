package com.mimicmod.entity.client;

import com.mimicmod.MimicMod;
import com.mimicmod.entity.MimicEntity;
import com.mimicmod.entity.MimicVariant;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for Mimic entities.
 * Handles rendering of different mimic variants with appropriate textures and
 * models.
 */
public class MimicEntityRenderer extends MobEntityRenderer<MimicEntity, MimicRenderState, MimicEntityModel> {

    /**
     * Texture cache by variant for efficient texture lookup.
     * Mapping of variant enum to texture identifier.
     */
    private static final Map<MimicVariant, Identifier> VARIANT_TEXTURES = new HashMap<>();

    static {
        VARIANT_TEXTURES.put(MimicVariant.CLASSIC, Identifier.of(MimicMod.MODID, "textures/entity/mimic_classic.png"));
        VARIANT_TEXTURES.put(MimicVariant.CORRUPTED,
                Identifier.of(MimicMod.MODID, "textures/entity/mimic_corrupted.png"));
        VARIANT_TEXTURES.put(MimicVariant.ENDER, Identifier.of(MimicMod.MODID, "textures/entity/mimic_ender.png"));
        VARIANT_TEXTURES.put(MimicVariant.CHRISTMAS,
                Identifier.of(MimicMod.MODID, "textures/entity/mimic_christmas.png"));
    }

    /**
     * Constructor for the renderer.
     * 
     * @param context Factory context for the renderer
     */
    public MimicEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new MimicEntityModel(context.getPart(MimicEntityModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public MimicRenderState createRenderState() {
        return new MimicRenderState();
    }

    @Override
    public void updateRenderState(MimicEntity entity, MimicRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        // Sync entity state to render state
        if (entity.getVariant() != null) {
            state.variant = entity.getVariant().getId();
        } else {
            state.variant = MimicVariant.CLASSIC.getId();
        }
        state.revealed = entity.isRevealed();
        state.attacking = entity.getTarget() != null;
    }

    @Override
    public Identifier getTexture(MimicRenderState state) {
        try {
            if (state.variant == null || state.variant.isEmpty()) {
                return VARIANT_TEXTURES.get(MimicVariant.CLASSIC);
            }
            MimicVariant variant = MimicVariant.fromId(state.variant);
            return VARIANT_TEXTURES.getOrDefault(variant, VARIANT_TEXTURES.get(MimicVariant.CLASSIC));
        } catch (Exception e) {
            MimicMod.LOGGER.error("Failed to get texture for variant: {}", state.variant, e);
            return VARIANT_TEXTURES.get(MimicVariant.CLASSIC);
        }
    }
}
