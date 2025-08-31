package com.yourpackage.sessionmanager.mixin;

import com.yourpackage.sessionmanager.SessionManagerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    private void addSessionManagerButton(CallbackInfo ci) {
        // Add Session Manager button to the multiplayer screen
        ButtonWidget sessionManagerButton = ButtonWidget.builder(
            Text.literal("Session Manager"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new SessionManagerScreen((MultiplayerScreen)(Object)this));
                }
            }
        ).dimensions(this.width / 2 - 100, this.height - 52, 200, 20).build();
        
        this.addDrawableChild(sessionManagerButton);
        
        // Move the "Cancel" button up to make room
        this.children().stream()
            .filter(element -> element instanceof ButtonWidget)
            .map(element -> (ButtonWidget) element)
            .filter(button -> button.getMessage().getString().equals("Cancel") || 
                           button.getMessage().getString().equals("Done"))
            .findFirst()
            .ifPresent(cancelButton -> {
                // Move cancel button up by 24 pixels
                cancelButton.setY(cancelButton.getY() - 24);
            });
    }
}
