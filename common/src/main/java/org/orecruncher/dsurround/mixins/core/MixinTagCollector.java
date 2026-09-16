package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.orecruncher.dsurround.eventing.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryDataCollector.class)
public class MixinTagCollector {
    @Inject(method = "loadOnlyTags(Lnet/minecraft/client/multiplayer/RegistryDataCollector$TagCollector;Lnet/minecraft/core/RegistryAccess$Frozen;Z)V", at = @At("TAIL"))
    private static void dsurround_tagsUpdated(@Coerce Object tagCollector, RegistryAccess.Frozen registryManager, boolean local, CallbackInfo ci) {
        if (local)
            ClientState.TAG_SYNC.raise().onTagSync(registryManager);
    }

    @Inject(method = "collectGameRegistries(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/core/RegistryAccess$Frozen;Z)Lnet/minecraft/core/RegistryAccess$Frozen;", at = @At("TAIL"))
    private void dsurround_collectGameRegistries(ResourceProvider resourceProvider, RegistryAccess.Frozen registryAccess, boolean local, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        if (local)
            ClientState.TAG_SYNC.raise().onTagSync(cir.getReturnValue());
    }
}
