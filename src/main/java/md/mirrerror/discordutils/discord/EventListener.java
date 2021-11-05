package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EventListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if(!event.getMessage().getContentRaw().startsWith(new BotController().getBotPrefix())) return;
        String[] args = event.getMessage().getContentRaw().replaceFirst(new BotController().getBotPrefix(), "").split(" ");
        EmbedManager embedManager = new EmbedManager();
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
                String code = UUID.randomUUID().toString().replaceAll("-", "");
                BotController.getLinkCodes().put(code, event.getAuthor());
                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.VERIFICATION_MESSAGE.getText())).queue();
                event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embedManager.infoEmbed(Message.VERIFICATION_CODE_MESSAGE.getText().replaceAll("%code%", code))).queue();
                break;
            }
            case "online": {
                event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(Message.ONLINE.getText().replaceAll("%online%", "" + Bukkit.getOnlinePlayers().size()))).queue();
                break;
            }
            case "sudo": {
                if(!DiscordUtils.isAdmin(event.getAuthor())) {
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
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.COMMAND_EXECUTED.getText())).queue();
                break;
            }
        }
    }

}
