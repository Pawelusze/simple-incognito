package pl.lambda.incognito.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.lambda.incognito.IncognitoPlugin;
import pl.lambda.incognito.inventory.IncognitoInventory;
import pl.lambda.incognito.util.ChatHelper;
import pl.lambda.incognito.util.MessageType;

public class IncognitoCommand implements CommandExecutor {
    
    private final IncognitoPlugin plugin;
    
    public IncognitoCommand(IncognitoPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatHelper.translateColors("&cTą komendę może używać tylko gracz!"));
            return true;
        }
        
        if (!player.hasPermission("incognito.use")) {
            ChatHelper.sendMessage(player, MessageType.CHAT, "&cNie masz uprawnień do używania tej komendy!");
            return true;
        }
        
        new IncognitoInventory(plugin, player).open();
        return true;
    }
}
