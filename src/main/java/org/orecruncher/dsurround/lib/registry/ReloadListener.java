package org.orecruncher.dsurround.lib.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.config.libraries.AssetLibraryEvent;
import org.orecruncher.dsurround.config.libraries.IReloadEvent;
import org.orecruncher.dsurround.eventing.ClientState;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.resources.ResourceUtilities;

public class ReloadListener implements ResourceManagerReloadListener {

    public ReloadListener() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        Minecraft mc = GameUtils.getMC();
        // During Minecraft construction gameThread is still null, so isSameThread() is false.
        // Queue the work so it runs once the game thread is assigned (after library handlers register).
        if (mc.isSameThread()) {
            this.applyReload(resourceManager);
        } else {
            mc.execute(() -> this.applyReload(resourceManager));
        }
    }

    private void applyReload(ResourceManager resourceManager) {
        Library.LOGGER.info("ReloadListener - raising notification");
        ClientState.RESOURCE_RELOAD.raise().onResourceReload(resourceManager);

        Library.LOGGER.info("ReloadListener - resetting configuration caches");
        var resourceUtilities = ResourceUtilities.createForResourceManager(resourceManager);
        AssetLibraryEvent.RELOAD.raise().onReload(resourceUtilities, IReloadEvent.Scope.RESOURCES);
    }
}
