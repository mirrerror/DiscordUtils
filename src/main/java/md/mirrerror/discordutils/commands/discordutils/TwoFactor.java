package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.DiscordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TwoFactor implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            Message.SENDER_IS_NOT_A_PLAYER.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        Player player = (Player) sender;
        if(!DiscordUtils.isVerified(player)) {
            Message.ACCOUNT_IS_NOT_VERIFIED.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        if(DiscordUtils.hasTwoFactor(player)) {

            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                Main.getDatabaseType().getDatabaseManager().setTwoFactor(player.getUniqueId(), false);
            } else {
                Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", false);
                Main.getInstance().getConfigManager().saveConfigFiles();
            }
            Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getFormattedText(true).forEach(msg -> sender.sendMessage(msg.replace("%status%", Message.DISABLED.getText())));

        } else {

            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                Main.getDatabaseType().getDatabaseManager().setTwoFactor(player.getUniqueId(), true);
            } else {
                Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", true);
                Main.getInstance().getConfigManager().saveConfigFiles();
            }
            Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getFormattedText(true).forEach(msg -> sender.sendMessage(msg.replace("%status%", Message.ENABLED.getText())));

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

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("2fa");
    }
}
