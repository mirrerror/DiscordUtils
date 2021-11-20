package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.discord.DiscordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TwoFactor implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Message.SENDER_IS_NOT_A_PLAYER.getText(true));
            return;
        }

        Player player = (Player) sender;
        if(!DiscordUtils.isVerified(player)) {
            sender.sendMessage(Message.ACCOUNT_IS_NOT_VERIFIED.getText(true));
            return;
        }

        if(DiscordUtils.hasTwoFactor(player)) {

            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
                databaseManager.setTwoFactor(player.getUniqueId(), false);
            } else {
                Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", false);
                Main.getInstance().getConfigManager().saveConfigFiles();
            }
            sender.sendMessage(Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getText(true) + Message.DISABLED.getText());

        } else {

            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
                databaseManager.setTwoFactor(player.getUniqueId(), true);
            } else {
                Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", true);
                Main.getInstance().getConfigManager().saveConfigFiles();
            }
            sender.sendMessage(Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getText(true) + Message.ENABLED.getText());

        }
    }

    @Override
    public String getName() {
        return "twofactor";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.twofactor";
    }
}
