package com.yourpackage.sessionmanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

public class SessionHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHelper.class);
    
    public static void applySession(String username, String uuid, String accessToken, String sessionType) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Parse UUID
            UUID playerUuid = null;
            if (uuid != null && !uuid.isEmpty()) {
                try {
                    playerUuid = UUID.fromString(uuid);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid UUID format: {}", uuid);
                }
            }
            
            // Parse session type
            Session.AccountType accountType = Session.AccountType.MOJANG;
            try {
                accountType = Session.AccountType.valueOf(sessionType);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid session type: {}, defaulting to MOJANG", sessionType);
            }
            
            // Create new session
            Session newSession = new Session(username, playerUuid, accessToken, 
                Optional.empty(), Optional.empty(), accountType);
            
            // Use reflection to set the session field
            setClientSession(client, newSession);
            
            LOGGER.info("Successfully applied session for user: {}", username);
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§aSession loaded: " + username), false);
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to apply session", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cFailed to load session!"), false);
            }
        }
    }
    
    private static void setClientSession(MinecraftClient client, Session session) {
        try {
            // Find the session field in MinecraftClient
            Field sessionField = null;
            Field[] fields = MinecraftClient.class.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType() == Session.class) {
                    sessionField = field;
                    break;
                }
            }
            
            if (sessionField == null) {
                // Try common field names if direct type search fails
                try {
                    sessionField = MinecraftClient.class.getDeclaredField("session");
                } catch (NoSuchFieldException e) {
                    try {
                        sessionField = MinecraftClient.class.getDeclaredField("field_1726"); // Obfuscated name
                    } catch (NoSuchFieldException e2) {
                        throw new RuntimeException("Could not find session field in MinecraftClient", e2);
                    }
                }
            }
            
            sessionField.setAccessible(true);
            sessionField.set(client, session);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to set client session", e);
        }
    }
    
    public static Session getCurrentSession() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSession();
    }
    
    public static boolean hasValidSession() {
        Session session = getCurrentSession();
        return session != null && 
               session.getUsername() != null && 
               !session.getUsername().isEmpty() &&
               session.getAccessToken() != null &&
               !session.getAccessToken().isEmpty();
    }
}
