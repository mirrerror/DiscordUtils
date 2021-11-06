package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.DiscordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TwoFactor extends SubCommand {

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
            Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", false);
            sender.sendMessage(Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getText(true) + Message.DISABLED.getText());
        } else {
            Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", true);
            sender.sendMessage(Message.DISCORDUTILS_TWOFACTOR_SUCCESSFUL.getText(true) + Message.ENABLED.getText());
        }
        Main.getInstance().getConfigManager().saveConfigFiles();
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
