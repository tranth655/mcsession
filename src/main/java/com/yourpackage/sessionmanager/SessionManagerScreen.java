package com.yourpackage.sessionmanager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SessionManagerScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    
    private final Screen parent;
    private ButtonWidget saveButton;
    private ButtonWidget loadButton;
    private ButtonWidget deleteButton;
    private ButtonWidget closeButton;
    
    private String currentSessionInfo = "";
    private String savedSessionInfo = "";
    
    public SessionManagerScreen(Screen parent) {
        super(Text.literal("Session Manager"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        
        // Save Session Button
        this.saveButton = ButtonWidget.builder(
            Text.literal("Save Current Session"),
            button -> {
                SessionManagerClient.saveCurrentSession();
                updateSessionInfo();
                button.setMessage(Text.literal("Session Saved!").formatted(Formatting.GREEN));
                // Reset button text after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        if (client != null) {
                            client.execute(() -> button.setMessage(Text.literal("Save Current Session")));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        ).dimensions(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        
        // Load Session Button
        this.loadButton = ButtonWidget.builder(
            Text.literal("Load Saved Session"),
            button -> {
                SessionManagerClient.loadSavedSession();
                updateSessionInfo();
                button.setMessage(Text.literal("Session Loaded!").formatted(Formatting.GREEN));
                // Reset button text after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        if (client != null) {
                            client.execute(() -> button.setMessage(Text.literal("Load Saved Session")));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        ).dimensions(centerX - BUTTON_WIDTH / 2, startY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        
        // Delete Session Button
        this.deleteButton = ButtonWidget.builder(
            Text.literal("Delete Saved Session"),
            button -> {
                deleteSavedSession();
                updateSessionInfo();
                button.setMessage(Text.literal("Session Deleted!").formatted(Formatting.RED));
                // Reset button text after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        if (client != null) {
                            client.execute(() -> button.setMessage(Text.literal("Delete Saved Session")));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        ).dimensions(centerX - BUTTON_WIDTH / 2, startY + 60, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        
        // Close Button - Returns to multiplayer screen
        this.closeButton = ButtonWidget.builder(
            Text.literal("Back to Multiplayer"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(this.parent);
                }
            }
        ).dimensions(centerX - BUTTON_WIDTH / 2, startY + 100, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        
        this.addDrawableChild(this.saveButton);
        this.addDrawableChild(this.loadButton);
        this.addDrawableChild(this.deleteButton);
        this.addDrawableChild(this.closeButton);
        
        updateSessionInfo();
        updateButtonStates();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // Draw current session info
        int infoStartY = 60;
        context.drawTextWithShadow(this.textRenderer, "Current Session:", 20, infoStartY, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, currentSessionInfo, 20, infoStartY + 12, 0xCCCCCC);
        
        // Draw saved session info
        context.drawTextWithShadow(this.textRenderer, "Saved Session:", 20, infoStartY + 40, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, savedSessionInfo, 20, infoStartY + 52, 0xCCCCCC);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void updateSessionInfo() {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Current session info
        if (client.getSession() != null) {
            currentSessionInfo = String.format("User: %s | Type: %s", 
                client.getSession().getUsername(),
                client.getSession().getAccountType().name());
        } else {
            currentSessionInfo = "No active session";
        }
        
        // Saved session info
        if (Files.exists(SessionManagerClient.SESSION_FILE)) {
            try {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(SessionManagerClient.SESSION_FILE.toFile())) {
                    props.load(fis);
                }
                
                String username = props.getProperty("username", "Unknown");
                String sessionType = props.getProperty("sessionType", "Unknown");
                String timestamp = props.getProperty("timestamp", "0");
                
                long savedTime = Long.parseLong(timestamp);
                long currentTime = System.currentTimeMillis();
                long hoursOld = (currentTime - savedTime) / (60 * 60 * 1000);
                
                savedSessionInfo = String.format("User: %s | Type: %s | Age: %d hours", 
                    username, sessionType, hoursOld);
                    
            } catch (IOException | NumberFormatException e) {
                savedSessionInfo = "Error reading saved session";
            }
        } else {
            savedSessionInfo = "No saved session found";
        }
    }
    
    private void updateButtonStates() {
        // Enable/disable buttons based on current state
        boolean hasCurrentSession = MinecraftClient.getInstance().getSession() != null;
        boolean hasSavedSession = Files.exists(SessionManagerClient.SESSION_FILE);
        
        saveButton.active = hasCurrentSession;
        loadButton.active = hasSavedSession;
        deleteButton.active = hasSavedSession;
    }
    
    private void deleteSavedSession() {
        try {
            if (Files.exists(SessionManagerClient.SESSION_FILE)) {
                Files.delete(SessionManagerClient.SESSION_FILE);
                SessionManagerClient.LOGGER.info("Saved session deleted");
            }
        } catch (IOException e) {
            SessionManagerClient.LOGGER.error("Failed to delete saved session", e);
        }
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
