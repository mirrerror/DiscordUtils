package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

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

                final String FINAL_COMMAND = command;
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), FINAL_COMMAND));
                event.getChannel().sendMessageEmbeds(embedManager.successfulEmbed(Message.COMMAND_EXECUTED.getText())).queue();
                break;
            }
            case "embed": {
                if(!DiscordUtils.isAdmin(event.getAuthor())) {
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
                    Field field = Class.forName("java.awt.Color").getField(args[2].toUpperCase());
                    color = (Color) field.get(null);
                } catch (Exception e) {
                    color = null;
                }

                if(color == null) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.INVALID_COLOR_VALUE.getText())).queue();
                    return;
                }

                event.getChannel().sendMessageEmbeds(embedManager.embed(args[1], text, color, Message.EMBED_SENT_BY.getText().replace("%sender%",
                        event.getAuthor().getAsTag()))).queue();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(DiscordUtils.hasTwoFactor(event.getUser())) {
            if(event.getChannelType() != ChannelType.PRIVATE) return;
            if(!event.getPrivateChannel().equals(event.getUser().openPrivateChannel().complete())) return;
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

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.GuildVoiceRewards.Enabled")) {
            if(BotController.getRewardBlacklistedVoiceChannels().contains(event.getChannelJoined().getIdLong())) return;
            User user = event.getEntity().getUser();
            if(!DiscordUtils.isVerified(user)) return;
            LocalDateTime localDateTime = LocalDateTime.now();
            BotController.getVoiceTime().put(user, localDateTime);
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.GuildVoiceRewards.Enabled")) {
            if(BotController.getRewardBlacklistedVoiceChannels().contains(event.getChannelLeft().getIdLong())) return;
            User user = event.getEntity().getUser();
            if(!DiscordUtils.isVerified(user)) return;
            if(!BotController.getVoiceTime().containsKey(user)) return;
            LocalDateTime localDateTime = LocalDateTime.now();
            long difference = Duration.between(BotController.getVoiceTime().get(user), localDateTime).getSeconds();
            long multiplier = Math.round(difference/Main.getInstance().getConfigManager().getConfig().getDouble("Discord.GuildVoiceRewards.Time"));
            BotController.getVoiceTime().remove(user);
            for (long i = 0; i < multiplier; i++) Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            Main.getInstance().getConfigManager().getConfig().getString("Discord.GuildVoiceRewards.Reward")
                                    .replace("%player%", DiscordUtils.getOfflinePlayer(user).getName())));
        }
    }

}
