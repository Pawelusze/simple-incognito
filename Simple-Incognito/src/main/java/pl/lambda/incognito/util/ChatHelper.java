package pl.lambda.incognito.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public final class ChatHelper {

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");

    private ChatHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void sendMessage(Player player, MessageType type, String message) {
        if (player == null || !player.isOnline() || message == null) {
            return;
        }

        String coloredMessage = translateColors(message);

        switch (type) {
            case CHAT:
                player.sendMessage(coloredMessage);
                break;

            case ACTION_BAR:
                sendActionBar(player, coloredMessage);
                break;

            case TITLE:
                sendTitleCompat(player, coloredMessage, "", 10, 70, 20);
                break;

            case SUBTITLE:
                sendTitleCompat(player, "", coloredMessage, 10, 70, 20);
                break;

            case BOTH_TITLE:
                String[] parts = message.split("\\|", 2);
                String title = parts.length > 0 ? translateColors(parts[0]) : "";
                String subtitle = parts.length > 1 ? translateColors(parts[1]) : "";
                sendTitleCompat(player, title, subtitle, 10, 70, 20);
                break;
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) {
            return;
        }

        String coloredTitle = title != null ? translateColors(title) : "";
        String coloredSubtitle = subtitle != null ? translateColors(subtitle) : "";

        sendTitleCompat(player, coloredTitle, coloredSubtitle, fadeIn, stay, fadeOut);
    }

    @SuppressWarnings("deprecation")
    private static void sendTitleCompat(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch (Exception e) {
            player.sendMessage("§6[TITLE] §r" + title);
            if (!subtitle.isEmpty()) {
                player.sendMessage("§7[SUBTITLE] §r" + subtitle);
            }
        }
    }

    private static void sendActionBar(Player player, String message) {
        try {
            player.sendActionBar(message);
        } catch (Exception e) {
            player.sendMessage("§8[§aACTION§8] §r" + message);
        }
    }

    @SuppressWarnings("deprecation")
    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return ChatColor.stripColor(translateColors(message));
    }

    public static boolean canReceiveMessage(Player player) {
        return player != null && player.isOnline() && player.isValid();
    }

    public static String translateColors(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (!message.contains("&")) {
            return message;
        }

        return COLOR_PATTERN.matcher(message).replaceAll("§$1");
    }
}
