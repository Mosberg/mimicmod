package com.mimicmod.entity.client;

import com.mimicmod.MimicMod;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Model definition for Mimic entities.
 * Represents a chest that jumps and bites by closing its lid.
 *
 * <p>
 * Features:
 * <ul>
 * <li>Jumping/bouncing animation when moving</li>
 * <li>Rapid chomping when attacking</li>
 * <li>Menacing breathing when revealed but idle</li>
 * <li>Subtle breathing when disguised</li>
 * </ul>
 */
public class MimicEntityModel extends EntityModel<MimicRenderState> {

        /**
         * Model layer location for registration.
         */
        public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
                        Identifier.of(MimicMod.MODID, "mimic"), "main");

        // Animation constants
        private static final float LID_OPEN_ANGLE = (float) Math.PI / 3.0F; // 60 degrees
        private static final float LID_IDLE_ANGLE = (float) Math.PI / 4.0F; // 45 degrees
        private static final float ATTACK_SPEED = 0.8F;
        private static final float ATTACK_BOB_SPEED = 1.5F;
        private static final float ATTACK_BOB_HEIGHT = 0.5F;
        private static final float IDLE_BREATHE_SPEED = 0.08F;
        private static final float IDLE_BREATHE_HEIGHT = 0.3F;
        private static final float DISGUISE_BREATHE_SPEED = 0.05F;
        private static final float DISGUISE_BREATHE_HEIGHT = 0.05F;
        private static final float JUMP_BOUNCE_SCALE = 3.0F;
        private static final float IDLE_BREATHING_MODULATION = 0.1F;

        private final ModelPart root;
        private final ModelPart base;
        private final ModelPart lid;

        public MimicEntityModel(ModelPart root) {
                super(root, RenderLayer::getEntityCutout);
                this.root = root;
                this.base = root.getChild("base");
                this.lid = root.getChild("lid");
        }

        /**
         * Creates the model data for the mimic entity.
         * Uses standard Minecraft chest UV layout (64x64 texture).
         *
         * Model coordinates: Blockbench format (1-15 range = 0-14 in Minecraft units)
         */
        public static TexturedModelData getTexturedModelData() {
                ModelData modelData = new ModelData();
                ModelPartData modelPartData = modelData.getRoot();

                // Base (lower chest part)
                // From: [1, 0, 1] To: [15, 10, 15]
                // Minecraft coordinates: [-7, 0, -7] to [7, 10, 7]
                ModelPartData base = modelPartData.addChild("base",
                                ModelPartBuilder.create()
                                                .uv(0, 19)
                                                .cuboid(-7.0F, 0.0F, -7.0F, 14.0F, 10.0F, 14.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

                // Lid (upper chest part) - pivot at back edge for opening
                // From: [1, 9, 1] To: [15, 14, 15]
                // Rotation origin: [8, 9, 15] (back edge center)
                ModelPartData lid = modelPartData.addChild("lid",
                                ModelPartBuilder.create()
                                                .uv(0, 0)
                                                .cuboid(-7.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 9.0F, 7.0F, 0.0F, 0.0F, 0.0F));

                // Latch (decorative clasp on front)
                // From: [7, 7, 0] To: [9, 11, 1]
                // Child of lid for attachment
                lid.addChild("latch",
                                ModelPartBuilder.create()
                                                .uv(0, 0)
                                                .cuboid(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

                // Teeth upper (on inside of lid)
                // From: [2, 9, 2] To: [14, 10, 3]
                // Rotation origin: [8, 9, 15] (matches lid rotation)
                // Child of lid so teeth move with lid when opening
                lid.addChild("teeth_upper",
                                ModelPartBuilder.create()
                                                .uv(0, 38)
                                                .cuboid(-6.0F, 0.0F, -13.0F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

                // Teeth lower (on inside of base)
                // From: [2, 10, 13] To: [14, 11, 14]
                // Child of base so teeth stay fixed
                base.addChild("teeth_lower",
                                ModelPartBuilder.create()
                                                .uv(0, 40)
                                                .cuboid(-6.0F, 10.0F, 6.0F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

                // Tongue (inside mouth cavity)
                // From: [5, 10, 5] To: [11, 10.5, 11]
                // Child of base so tongue stays in mouth
                base.addChild("tongue",
                                ModelPartBuilder.create()
                                                .uv(0, 42)
                                                .cuboid(-3.0F, 10.0F, -3.0F, 6.0F, 0.5F, 6.0F, new Dilation(0.0F)),
                                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

                return TexturedModelData.of(modelData, 64, 64);
        }

        @Override
        public void setAngles(MimicRenderState state) {
                super.setAngles(state);

                // Reset to default
                this.lid.pitch = 0.0F;
                this.root.originY = 0.0F;
                this.base.pitch = 0.0F;

                if (state.revealed) {
                        if (state.attacking) {
                                animateAttack(state);
                        } else {
                                animateReveledIdle(state);
                        }

                        // Apply jump animation if moving
                        if (state.movementSpeed > 0.01F) {
                                animateJump(state);
                        }
                } else {
                        animateDisguised(state);
                }
        }

        /**
         * Animates rapid chomping attack.
         * Lid snaps open and closed rapidly with aggressive bobbing.
         */
        private void animateAttack(MimicRenderState state) {
                float attackProgress = state.age * ATTACK_SPEED;
                float chomp = MathHelper.abs(MathHelper.sin(attackProgress));

                // Chomping lid motion (0 = closed, max = 60°)
                this.lid.pitch = -chomp * LID_OPEN_ANGLE;

                // Aggressive vertical bobbing
                this.root.originY = MathHelper.sin(attackProgress * ATTACK_BOB_SPEED) * ATTACK_BOB_HEIGHT;
        }

        /**
         * Animates revealed idle state (menacing breathing).
         * Lid stays open about 45° with gentle breathing motion.
         */
        private void animateReveledIdle(MimicRenderState state) {
                float idleBreathing = MathHelper.sin(state.age * IDLE_BREATHE_SPEED) * IDLE_BREATHING_MODULATION;

                // Menacing open mouth with breathing modulation
                this.lid.pitch = -(LID_IDLE_ANGLE + idleBreathing);

                // Gentle vertical breathing motion
                this.root.originY = MathHelper.sin(state.age * IDLE_BREATHE_SPEED) * IDLE_BREATHE_HEIGHT;
        }

        /**
         * Animates jumping/bouncing motion when moving.
         * Creates a bouncing effect as the chest hops toward the player.
         */
        private void animateJump(MimicRenderState state) {
                float jumpCycle = state.limbPose * 0.6662F;
                float bounceHeight = MathHelper.abs(MathHelper.sin(jumpCycle)) * JUMP_BOUNCE_SCALE
                                * state.movementSpeed;

                // Bounce effect (adds to existing vertical offset)
                this.root.originY += bounceHeight;

                // Forward pitch during jump (slight tilt)
                this.base.pitch = MathHelper.sin(jumpCycle) * 0.1F * state.movementSpeed;
        }

        /**
         * Animates disguised state (subtle breathing).
         * Lid completely closed with barely perceptible breathing to hint it's alive.
         */
        private void animateDisguised(MimicRenderState state) {
                // Completely closed and still
                this.lid.pitch = 0.0F;
                this.root.originY = 0.0F;
                this.base.pitch = 0.0F;

                // Very subtle "alive" breathing hint (barely visible)
                float subtleBreath = MathHelper.sin(state.age * DISGUISE_BREATHE_SPEED) * DISGUISE_BREATHE_HEIGHT;
                this.root.originY = subtleBreath;
        }
}
