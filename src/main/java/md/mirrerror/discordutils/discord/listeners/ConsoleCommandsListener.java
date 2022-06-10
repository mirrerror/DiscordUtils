package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ConsoleCommandsListener extends ListenerAdapter {

    private final EmbedManager embedManager = new EmbedManager();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if(!event.isFromGuild()) return;

        TextChannel textChannel = event.getTextChannel();

        if(!textChannel.getId().equals(BotController.getConsoleLoggingTextChannel().getId())) return;
        if(!DiscordUtils.isAdmin(event.getMember())) {
            textChannel.sendMessageEmbeds(embedManager.errorEmbed(Message.INSUFFICIENT_PERMISSIONS.getText())).queue();
            return;
        }

        for(String cmd : BotController.getVirtualConsoleBlacklistedCommands()) if(event.getMessage().getContentRaw().startsWith(cmd)) {
            textChannel.sendMessageEmbeds(embedManager.errorEmbed(Message.THIS_COMMAND_IS_BLACKLISTED.getText())).queue();
            return;
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getMessage().getContentRaw()));
        textChannel.sendMessageEmbeds(embedManager.successfulEmbed(Message.COMMAND_EXECUTED.getText())).queue();
    }

}
