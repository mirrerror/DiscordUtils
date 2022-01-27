package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetDiscord implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length < 1) {
            Message.DISCORDUTILS_GETDISCORD_USAGE.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        User user = null;
        if(player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if(offlinePlayer != null) user = DiscordUtils.getDiscordUser(offlinePlayer);
        } else user = DiscordUtils.getDiscordUser(player);

        if(user == null) {
            Message.INVALID_PLAYER_NAME_OR_UNVERIFIED.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        final String tag = user.getAsTag();
        Message.GETDISCORD_SUCCESSFUL.getFormattedText(true).forEach(msg -> sender.sendMessage(msg.replace("%discord%", tag)));

    }

    @Override
    public String getName() {
        return "getdiscord";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.getdiscord";
    }

}
