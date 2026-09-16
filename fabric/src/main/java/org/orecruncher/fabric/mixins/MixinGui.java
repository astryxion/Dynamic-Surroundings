package org.orecruncher.fabric.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.orecruncher.dsurround.gui.overlay.OverlayManager;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class MixinGui {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"))
    public void dsurround_extractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Add the overlay manager to the render layers of Gui
        if (!((Hud) (Object) this).isHidden()) {
            OverlayManager dsurround_overlayManager = ContainerManager.resolve(OverlayManager.class);
            dsurround_overlayManager.render(context, deltaTracker);
        }
    }
}
