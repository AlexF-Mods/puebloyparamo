package io.github.alexf6.puebloyparamo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.alexf6.puebloyparamo.entity.SeatEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Renderizador invisible para {@link SeatEntity}.
 */
public class SeatRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {

    public SeatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(
            EntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        // Entidad invisible: no emite geometría.
    }
}
