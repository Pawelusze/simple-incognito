package pl.lambda.incognito;

import org.bukkit.plugin.java.JavaPlugin;
import pl.lambda.incognito.command.IncognitoCommand;
import pl.lambda.incognito.data.DataManager;
import pl.lambda.incognito.listener.InventoryListener;
import pl.lambda.incognito.listener.PlayerJoinListener;

public final class IncognitoPlugin extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        this.dataManager = new DataManager(this);

        var incognitoCommand = getCommand("incognito");
        if (incognitoCommand != null) {
            incognitoCommand.setExecutor(new IncognitoCommand(this));
        }

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new InventoryListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("Incognito włączony!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        getLogger().info("Incognito wyłączony!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
