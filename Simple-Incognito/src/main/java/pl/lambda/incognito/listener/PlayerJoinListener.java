package pl.lambda.incognito.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.lambda.incognito.IncognitoPlugin;
import pl.lambda.incognito.manager.IncognitoManager;

import java.util.concurrent.TimeUnit;

public final class PlayerJoinListener implements Listener {

    private final IncognitoPlugin plugin;

    public PlayerJoinListener(IncognitoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        IncognitoManager.applyIncognitoOnJoin(plugin, event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!player.isOnline()) {
                plugin.getDataManager().removePlayerData(player.getUniqueId());
            }
        }, TimeUnit.HOURS.toSeconds(1) * 20L);
    }
}
