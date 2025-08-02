package pl.lambda.incognito.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.lambda.incognito.IncognitoPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

public final class DataManager {

    private final IncognitoPlugin plugin;
    private final ConcurrentHashMap<UUID, PlayerData> playerData;
    private final File dataFile;
    private final File backupFile;
    private final ScheduledExecutorService executor;
    private volatile FileConfiguration dataConfig;

    public DataManager(IncognitoPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.backupFile = new File(plugin.getDataFolder(), "data.yml.backup");
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "IncognitoData-Thread");
            t.setDaemon(true);
            return t;
        });
        loadData();
        startAutoSave();
    }
    
    private void loadData() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            if (!dataFile.exists()) {
                plugin.saveResource("data.yml", false);
            }

            dataConfig = YamlConfiguration.loadConfiguration(dataFile);

            dataConfig.getKeys(false).parallelStream()
                .forEach(this::loadPlayerData);

        } catch (Exception e) {
            plugin.getLogger().severe("Błąd ładowania danych: " + e.getMessage());
        }
    }

    private void loadPlayerData(String uuidString) {
        try {
            var uuid = UUID.fromString(uuidString);
            var originalName = dataConfig.getString(uuidString + ".original-name");
            if (originalName == null) return;

            var data = new PlayerData(originalName);
            data.setNicknameHidden(dataConfig.getBoolean(uuidString + ".nickname-hidden", false));
            data.setSkinHidden(dataConfig.getBoolean(uuidString + ".skin-hidden", false));
            data.setFakeNickname(dataConfig.getString(uuidString + ".fake-nickname"));
            playerData.put(uuid, data);
        } catch (IllegalArgumentException ignored) {}
    }
    
    private void startAutoSave() {
        executor.scheduleAtFixedRate(this::saveAllDataAsync, 5, 5, TimeUnit.MINUTES);
    }

    public CompletableFuture<Void> saveAllDataAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (dataFile.exists()) {
                    Files.copy(dataFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                playerData.forEach((uuid, data) -> {
                    var prefix = uuid.toString();
                    dataConfig.set(prefix + ".original-name", data.getOriginalNickname());
                    dataConfig.set(prefix + ".nickname-hidden", data.isNicknameHidden());
                    dataConfig.set(prefix + ".skin-hidden", data.isSkinHidden());
                    dataConfig.set(prefix + ".fake-nickname", data.getFakeNickname());
                });

                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Błąd zapisu: " + e.getMessage());
            }
        }, executor);
    }

    public void saveAllData() {
        saveAllDataAsync().join();
    }

    public PlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), 
            uuid -> new PlayerData(player.getName()));
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public CompletableFuture<Void> savePlayerDataAsync(Player player) {
        return CompletableFuture.runAsync(() -> {
            var uuid = player.getUniqueId();
            var data = playerData.get(uuid);
            if (data == null) return;

            var prefix = uuid.toString();
            dataConfig.set(prefix + ".original-name", data.getOriginalNickname());
            dataConfig.set(prefix + ".nickname-hidden", data.isNicknameHidden());
            dataConfig.set(prefix + ".skin-hidden", data.isSkinHidden());
            dataConfig.set(prefix + ".fake-nickname", data.getFakeNickname());

            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Błąd zapisu gracza: " + e.getMessage());
            }
        }, executor);
    }

    public void savePlayerData(Player player) {
        savePlayerDataAsync(player);
    }

    public void shutdown() {
        saveAllData();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
        var prefix = uuid.toString();
        dataConfig.set(prefix, null);
    }
}
