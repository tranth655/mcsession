package com.yourpackage.sessionmanager.mixin;

import com.yourpackage.sessionmanager.SessionManagerClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        // Handle key presses
        if (SessionManagerClient.saveSessionKey != null && SessionManagerClient.wasKeyPressed(SessionManagerClient.saveSessionKey)) {
            SessionManagerClient.saveCurrentSession();
        }
        
        if (SessionManagerClient.loadSessionKey != null && SessionManagerClient.wasKeyPressed(SessionManagerClient.loadSessionKey)) {
            SessionManagerClient.loadSavedSession();
        }
    }
}
