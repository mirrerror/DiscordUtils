package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Unlink implements SubCommand {
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

        String playerIp = StringUtils.remove(player.getAddress().getAddress().toString(), '/');

        DiscordUtils.getDiscordUser(player).openPrivateChannel().submit()
                .thenCompose(channel -> channel.sendMessageEmbeds(new EmbedManager().infoEmbed(Message.ACCOUNT_UNLINK_CONFIRMATION.getText().replace("%playerIp%", playerIp))).submit())
                .whenComplete((msg, error) -> {
                    if (error == null) {
                        BotController.getUnlinkPlayers().put(player, msg);
                        msg.addReaction("✅").queue();
                        msg.addReaction("❎").queue();
                        return;
                    }
                    Message.CAN_NOT_SEND_MESSAGE.getFormattedText(true).forEach(sender::sendMessage);
                });

        Message.ACCOUNT_UNLINK_REQUEST_SENT.getFormattedText(true).forEach(sender::sendMessage);
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.unlink";
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("ulink");
        return aliases;
    }
}
