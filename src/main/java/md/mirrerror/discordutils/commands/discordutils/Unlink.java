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

public class Unlink implements SubCommand {
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

        String playerIp = StringUtils.remove(player.getAddress().getAddress().toString(), '/');

        EmbedManager embedManager = new EmbedManager();
        DiscordUtils.getDiscordUser(player).openPrivateChannel().submit()
                .thenCompose(channel -> channel.sendMessageEmbeds(embedManager.infoEmbed(Message.ACCOUNT_UNLINK_CONFIRMATION.getText().replace("%playerIp%", playerIp))).submit())
                .whenComplete((msg, error) -> {
                    if (error == null) {
                        BotController.getUnlinkPlayers().put(player, msg);
                        msg.addReaction("✅").queue();
                        msg.addReaction("❎").queue();
                        return;
                    }
                    player.sendMessage(Message.CAN_NOT_SEND_MESSAGE.getText(true));
                });

        sender.sendMessage(Message.ACCOUNT_UNLINK_REQUEST_SENT.getText(true));
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.unlink";
    }
}
