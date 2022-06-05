package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.security.SecureRandom;
import java.util.List;

public class BotCommandsListener extends ListenerAdapter {

    private final BotController botController = new BotController();
    private final EmbedManager embedManager = new EmbedManager();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if(!event.getMessage().getContentRaw().startsWith(botController.getBotPrefix())) return;
        List<Long> botCommandTextChannels = Main.getInstance().getConfigManager().getConfig().getLongList("Discord.BotCommandTextChannels");
        if(!botCommandTextChannels.isEmpty()) {
            if(!Main.getInstance().getConfigManager().getConfig().getLongList("Discord.BotCommandTextChannels").contains(event.getChannel().getIdLong())) return;
        }
        String[] args = event.getMessage().getContentRaw().replaceFirst(botController.getBotPrefix(), "").split(" ");

        switch (args[0]) {
            case "link": {
                if(DiscordUtils.isVerified(event.getAuthor())) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.ACCOUNT_ALREADY_VERIFIED.getText())).queue();
                    return;
                }
                if(BotController.getLinkCodes().containsValue(event.getAuthor())) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.LINK_ALREADY_INITIATED.getText())).queue();
                    return;
                }
                String code = "";
                byte[] secureRandomSeed = new SecureRandom().generateSeed(20);
                for(byte b : secureRandomSeed) code += b;
                code = code.replace("-", "");

                final String FINAL_CODE = code;
                event.getAuthor().openPrivateChannel().submit()
                        .thenCompose(channel -> channel.sendMessageEmbeds(embedManager.infoEmbed(Message.VERIFICATION_CODE_MESSAGE.getText().replace("%code%", FINAL_CODE))).submit())
                        .whenComplete((msg, error) -> {
                            if(error == null) {
                                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.VERIFICATION_MESSAGE.getText())).queue();
                                BotController.getLinkCodes().put(FINAL_CODE, event.getAuthor());
                                return;
                            }
                            event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.CAN_NOT_SEND_MESSAGE.getText())).queue();
                        });
                break;
            }
            case "online": {
                event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(Message.ONLINE.getText().replace("%online%", "" + Bukkit.getOnlinePlayers().size()))).queue();
                break;
            }
            case "sudo": {
                if(!DiscordUtils.isAdmin(event.getMember())) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.INSUFFICIENT_PERMISSIONS.getText())).queue();
                    return;
                }
                if(args.length < 2) {
                    event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(Message.DISCORD_SUDO_USAGE.getText())).queue();
                    return;
                }
                String command = "";
                for(int i = 1; i < args.length; i++) command += args[i] + " ";
                command = command.trim();

                final String FINAL_COMMAND = command;
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), FINAL_COMMAND));
                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.COMMAND_EXECUTED.getText())).queue();
                break;
            }
            case "embed": {
                if(!DiscordUtils.isAdmin(event.getMember())) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.INSUFFICIENT_PERMISSIONS.getText())).queue();
                    return;
                }
                if(args.length < 4) {
                    event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(Message.DISCORD_EMBED_USAGE.getText())).queue();
                    return;
                }
                String text = "";
                for(int i = 3; i < args.length; i++) text += args[i] + " ";
                text = text.trim();

                Color color;
                try {
                    color = Color.decode(args[2]);
                } catch (Exception e) {
                    color = null;
                }

                if(color == null) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.INVALID_COLOR_VALUE.getText())).queue();
                    return;
                }

                event.getChannel().sendMessageEmbeds(embedManager.embed(args[1], text, color, Message.EMBED_SENT_BY.getText().replace("%sender%",
                        event.getAuthor().getAsTag()))).queue();
                break;
            }
            case "stats": {
                OfflinePlayer player;

                if(args.length > 1) {

                    player = Bukkit.getOfflinePlayer(args[1]);

                } else {

                    if(!DiscordUtils.isVerified(event.getAuthor())) {
                        event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.ACCOUNT_IS_NOT_VERIFIED.getText())).queue();
                        return;
                    }

                    player = DiscordUtils.getOfflinePlayer(event.getAuthor());

                }

                String messageToSend = "";
                for (String s : Message.STATS_FORMAT.getStringList()) {
                    messageToSend += s + "\n";
                }

                messageToSend = Main.getInstance().getPapiManager().setPlaceholders(player, messageToSend);

                event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(messageToSend)).queue();
                break;
            }
        }
    }

}
