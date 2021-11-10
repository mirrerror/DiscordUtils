package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Objects;

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
                String code = "";
                byte[] secureRandomSeed = new SecureRandom().generateSeed(20);
                for(byte b : secureRandomSeed) code += b;
                BotController.getLinkCodes().put(code, event.getAuthor());
                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.VERIFICATION_MESSAGE.getText())).queue();
                event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embedManager.infoEmbed(Message.VERIFICATION_CODE_MESSAGE.getText().replace("%code%", code))).queue();
                break;
            }
            case "online": {
                event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(Message.ONLINE.getText().replace("%online%", "" + Bukkit.getOnlinePlayers().size()))).queue();
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

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(DiscordUtils.hasTwoFactor(event.getUser())) {
            if(!event.getPrivateChannel().equals(Objects.requireNonNull(event.getUser()).openPrivateChannel().complete())) return;
            Player player = DiscordUtils.getPlayer(event.getUser());
            if(player == null) return;
            if(event.getReaction().getReactionEmote().getName().equals("✅")) {
                BotController.getTwoFactorPlayers().remove(player);
                player.sendMessage(Message.TWOFACTOR_AUTHORIZED.getText(true));
                BotController.getSessions().put(player.getUniqueId(), StringUtils.remove(player.getAddress().getAddress().toString(), '/'));
            }
            if(event.getReaction().getReactionEmote().getName().equals("❎")) {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.kickPlayer(Message.TWOFACTOR_REJECTED.getText()));
            }
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
        }
    }

}
