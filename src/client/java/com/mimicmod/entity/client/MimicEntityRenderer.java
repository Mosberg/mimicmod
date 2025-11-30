package com.mimicmod.entity.client;

import com.mimicmod.MimicMod;
import com.mimicmod.entity.MimicEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import java.util.Map;

/**
 * Renderer for Mimic entities.
 * Handles rendering of different mimic variants with appropriate textures and
 * models.
 */
public class MimicEntityRenderer extends MobEntityRenderer<MimicEntity, MimicRenderState, MimicEntityModel> {

  /**
   * Texture cache by variant for efficient texture lookup.
   */
  private static final Map<MimicVariant, Identifier> VARIANT_TEXTURES = Map.ofEntries(
      Map.entry(MimicVariant.CLASSIC, Identifier.of(MimicMod.MODID, "textures/entity/mimic_classic.png")),
      Map.entry(MimicVariant.CORRUPTED, Identifier.of(MimicMod.MODID, "textures/entity/mimic_corrupted.png")),
      Map.entry(MimicVariant.ENDER, Identifier.of(MimicMod.MODID, "textures/entity/mimic_ender.png")),
      Map.entry(MimicVariant.CHRISTMAS, Identifier.of(MimicMod.MODID, "textures/entity/mimic_christmas.png")));

  /**
   * Mimic variant enum for type-safe lookups.
   */
  private enum MimicVariant {
    CLASSIC("classic"),
    CORRUPTED("corrupted"),
    ENDER("ender"),
    CHRISTMAS("christmas");

    private final String id;

    MimicVariant(String id) {
      this.id = id;
    }

    /**
     * Gets variant from string ID.
     *
     * @param id Variant string identifier
     * @return Matching variant, or CLASSIC if not found
     */
    public static MimicVariant fromId(String id) {
      for (MimicVariant variant : values()) {
        if (variant.id.equals(id)) {
          return variant;
        }
      }
      return CLASSIC;
    }
  }

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
    state.variant = entity.getVariant();
    state.revealed = entity.isRevealed();
    state.attacking = entity.getTarget() != null;
  }

  @Override
  public Identifier getTexture(MimicRenderState state) {
    MimicVariant variant = MimicVariant.fromId(state.variant);
    return VARIANT_TEXTURES.getOrDefault(variant, VARIANT_TEXTURES.get(MimicVariant.CLASSIC));
  }
}
