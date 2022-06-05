package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.TwoFactorSession;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class EventListener extends ListenerAdapter {

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
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfterUnlink").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
                    });
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
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        player.kickPlayer(Message.TWOFACTOR_REJECTED.getText());
                        Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfter2FADecline").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
                    });
                }

                event.getChannel().deleteMessageById(event.getMessageId()).queue();
            }
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