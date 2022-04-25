package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import md.mirrerror.discordutils.discord.TwoFactorSession;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public class EventListener extends ListenerAdapter {

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
                if(!DiscordUtils.isVerified(event.getAuthor())) {
                    event.getChannel().sendMessageEmbeds(embedManager.errorEmbed(Message.ACCOUNT_IS_NOT_VERIFIED.getText())).queue();
                    return;
                }

                String messageToSend = "";
                for (String s : Message.STATS_FORMAT.getStringList()) {
                    messageToSend += s + "\n";
                }


                Player player = DiscordUtils.getPlayer(event.getAuthor());
                if(player != null) {
                    messageToSend = Main.getInstance().getPapiManager().setPlaceholders(player, messageToSend);
                } else {
                    OfflinePlayer offlinePlayer = DiscordUtils.getOfflinePlayer(event.getAuthor());
                    messageToSend = Main.getInstance().getPapiManager().setPlaceholders(offlinePlayer, messageToSend);
                }

                event.getChannel().sendMessageEmbeds(embedManager.infoEmbed(messageToSend)).queue();
                break;
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(event.getChannelType() != ChannelType.PRIVATE) return;
        if(event.getUser().equals(BotController.getJda().getSelfUser())) return;
        if(!event.getPrivateChannel().equals(event.getUser().openPrivateChannel().complete())) return;
        Player player = DiscordUtils.getPlayer(event.getUser());
        if(player == null) return;

        long messageId = event.getMessageIdLong();
        if(BotController.getUnlinkPlayers().containsKey(player)) {
            if(BotController.getUnlinkPlayers().get(player).getIdLong() == messageId) {

                if(event.getReaction().getReactionEmote().getName().equals("✅")) {
                    if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                        DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
                        databaseManager.unregisterPlayer(player.getUniqueId());
                    } else {
                        Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId(), null);
                        Main.getInstance().getConfigManager().saveConfigFiles();

                        long roleId = Main.getInstance().getConfigManager().getConfig().getLong("Discord.VerifiedRole.Id");
                        if(roleId > 0) {
                            BotController.getJda().getGuilds().forEach(guild -> {
                                Role verifiedRole = DiscordUtils.getVerifiedRole(guild);
                                Member member = null;

                                if(guild.isMember(event.getUser())) member = guild.getMember(event.getUser());

                                if(verifiedRole != null && member != null) guild.removeRoleFromMember(member, verifiedRole).queue();
                            });
                        }
                    }
                    BotController.getUnlinkPlayers().remove(player);
                    Message.ACCOUNT_SUCCESSFULLY_UNLINKED.getFormattedText(true).forEach(player::sendMessage);
                }
                if(event.getReaction().getReactionEmote().getName().equals("❎")) {
                    BotController.getUnlinkPlayers().remove(player);
                    Message.ACCOUNT_UNLINK_CANCELLED.getFormattedText(true).forEach(player::sendMessage);
                }

                event.getChannel().deleteMessageById(event.getMessageId()).queue();

            }
        }

        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            if(messageId == Long.parseLong(BotController.getTwoFactorPlayers().get(player))) {
                if(event.getReaction().getReactionEmote().getName().equals("✅")) {
                    BotController.getTwoFactorPlayers().remove(player);
                    player.sendMessage(Message.TWOFACTOR_AUTHORIZED.getText(true));
                    BotController.getSessions().put(player.getUniqueId(), new TwoFactorSession(StringUtils.remove(player.getAddress().getAddress().toString(), '/'),
                            LocalDateTime.now().plusSeconds(Main.getInstance().getConfigManager().getConfig().getLong("Discord.2FASessionTime"))));
                }
                if(event.getReaction().getReactionEmote().getName().equals("❎")) {
                    BotController.getTwoFactorPlayers().remove(player);
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.kickPlayer(Message.TWOFACTOR_REJECTED.getText()));
                }

                event.getChannel().deleteMessageById(event.getMessageId()).queue();
            }
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

            String command = Main.getInstance().getConfigManager().getConfig().getString("Discord.GuildVoiceRewards.Reward")
                    .replace("%player%", DiscordUtils.getOfflinePlayer(user).getName());
            for (long i = 0; i < multiplier; i++) Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        OffsetDateTime newTime = event.getNewTimeBoosted();
        OffsetDateTime oldTime = event.getOldTimeBoosted();
        if(newTime == null || oldTime == null) {
            Main.getInstance().getLogger().severe("An error occurred while handling the GuildMemberUpdateBoostTimeEvent! Please, contact the developer!");
            return;
        }

        if(newTime.isAfter(oldTime)) {
            Member member = event.getEntity();
            User user = member.getUser();
            if(DiscordUtils.isVerified(user)) return;

            OfflinePlayer offlinePlayer = DiscordUtils.getOfflinePlayer(user);
            Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfterServerBoosting").forEach(command -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", offlinePlayer.getName()).replace("%user%", user.getAsTag()));
            });

        }
    }

}
