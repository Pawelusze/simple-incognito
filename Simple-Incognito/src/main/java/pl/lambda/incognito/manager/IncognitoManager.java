package pl.lambda.incognito.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.lambda.incognito.IncognitoPlugin;
import pl.lambda.incognito.data.PlayerData;
import pl.lambda.incognito.util.ChatHelper;
import pl.lambda.incognito.util.MessageType;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

public final class IncognitoManager {

    private static final String INCOGNITO_SKIN_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYxNjc5NjMzNzAwMCwKICAicHJvZmlsZUlkIiA6ICJhNzk4YzI4YWIzNmE0NWNkYjUwNGNkODk3NGNhNjE2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RTa2luIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyOWRkOWQwOWI2YWQ2YzI0ZmY3NjUyMzZkZWJjM2JjOGE1ZTkwYmNlNDFiMDBjNWU0N2E0ZTFhNGMxNmIyNCIKICAgIH0KICB9Cn0=";
    private static final String INCOGNITO_SKIN_SIGNATURE = "";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    private IncognitoManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CompletableFuture<Void> toggleNicknameAsync(IncognitoPlugin plugin, Player player) {
        return CompletableFuture.runAsync(() -> {
            var data = plugin.getDataManager().getPlayerData(player);

            if (data.isNicknameHidden()) {
                data.setNicknameHidden(false);
                data.setFakeNickname(null);
                updatePlayerDisplay(plugin, player, data.getOriginalNickname());
                ChatHelper.sendMessage(player, MessageType.ACTION_BAR, "&aPrawdziwy nick widoczny!");
            } else {
                var fakeNick = generateSecureNickname();
                data.setNicknameHidden(true);
                data.setFakeNickname(fakeNick);
                updatePlayerDisplay(plugin, player, fakeNick);
                ChatHelper.sendMessage(player, MessageType.BOTH_TITLE, "&6Nick ukryty!|&7Nowy nick: &f" + fakeNick);
            }

            plugin.getDataManager().savePlayerDataAsync(player);
        });
    }

    public static void toggleNickname(IncognitoPlugin plugin, Player player) {
        toggleNicknameAsync(plugin, player);
    }
    
    public static CompletableFuture<Void> toggleSkinAsync(IncognitoPlugin plugin, Player player) {
        return CompletableFuture.runAsync(() -> {
            var data = plugin.getDataManager().getPlayerData(player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (data.isSkinHidden()) {
                    data.setSkinHidden(false);
                    restoreOriginalSkin(plugin, player);
                    ChatHelper.sendMessage(player, MessageType.ACTION_BAR, "&aPrawdziwy skin widoczny!");
                } else {
                    data.setSkinHidden(true);
                    applyIncognitoSkin(plugin, player);
                    ChatHelper.sendMessage(player, MessageType.ACTION_BAR, "&aSkin ukryty!");
                }

                plugin.getDataManager().savePlayerDataAsync(player);
            });
        });
    }

    public static void toggleSkin(IncognitoPlugin plugin, Player player) {
        toggleSkinAsync(plugin, player);
    }
    
    private static String generateSecureNickname() {
        var nick = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            nick.append(CHARS.charAt(SECURE_RANDOM.nextInt(CHARS.length())));
        }
        return nick.toString();
    }
    
    private static void updatePlayerDisplay(IncognitoPlugin plugin, Player player, String displayName) {
        var displayComponent = LegacyComponentSerializer.legacySection().deserialize(displayName);
        player.displayName(displayComponent);
        player.playerListName(displayComponent);
        player.customName(displayComponent);
        player.setCustomNameVisible(true);
        
        refreshPlayerForAll(plugin, player);
    }
    
    private static void applyIncognitoSkin(IncognitoPlugin plugin, Player player) {
        try {
            plugin.getLogger().info("Próba zmiany skina dla " + player.getName());

            var profile = Bukkit.createProfile(player.getUniqueId());
            profile.getProperties().clear();

            var property = new ProfileProperty("textures", INCOGNITO_SKIN_TEXTURE, INCOGNITO_SKIN_SIGNATURE);
            profile.setProperty(property);

            plugin.getLogger().info("Ustawiono texture property dla " + player.getName());

            player.setPlayerProfile(profile);

            plugin.getLogger().info("Zastosowano profil dla " + player.getName());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                refreshPlayerForAll(plugin, player);
                plugin.getLogger().info("Odświeżono gracza " + player.getName() + " dla wszystkich");
            }, 5L);

        } catch (Exception e) {
            plugin.getLogger().severe("Błąd zmiany skina dla " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void restoreOriginalSkin(IncognitoPlugin plugin, Player player) {
        try {
            plugin.getLogger().info("Próba przywrócenia oryginalnego skina dla " + player.getName());

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                var originalProfile = Bukkit.createProfile(player.getUniqueId(), player.getName());
                originalProfile.complete();

                plugin.getLogger().info("Pobrano oryginalny profil dla " + player.getName());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.setPlayerProfile(originalProfile);

                    plugin.getLogger().info("Przywrócono oryginalny profil dla " + player.getName());

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        refreshPlayerForAll(plugin, player);
                        plugin.getLogger().info("Odświeżono oryginalny skin dla " + player.getName());
                    }, 5L);
                });
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd przywracania skina dla " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void refreshPlayerForAll(IncognitoPlugin plugin, Player player) {
        if (!player.isOnline()) return;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player) && onlinePlayer.canSee(player)) {
                onlinePlayer.hidePlayer(plugin, player);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && onlinePlayer.isOnline()) {
                        onlinePlayer.showPlayer(plugin, player);
                    }
                }, 2L);
            }
        }
    }
    
    public static void applyIncognitoOnJoin(IncognitoPlugin plugin, Player player) {
        var data = plugin.getDataManager().getPlayerData(player);

        if (data.isNicknameHidden() && data.getFakeNickname() != null) {
            updatePlayerDisplay(plugin, player, data.getFakeNickname());
        }
        
        if (data.isSkinHidden()) {
            Bukkit.getScheduler().runTask(plugin, () -> applyIncognitoSkin(plugin, player));
        }
    }
}
