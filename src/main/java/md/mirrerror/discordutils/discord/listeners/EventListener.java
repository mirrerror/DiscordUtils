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
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

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
                    }

                    long roleId = Main.getInstance().getConfigManager().getBotSettings().getLong("VerifiedRole.Id");
                    if(roleId > 0) {
                        BotController.getJda().getGuilds().forEach(guild -> {
                            Role verifiedRole = DiscordUtils.getVerifiedRole(guild);
                            Member member = null;

                            if(guild.isMember(event.getUser())) member = guild.getMember(event.getUser());

                            if(verifiedRole != null && member != null) if(member.getRoles().contains(verifiedRole)) guild.removeRoleFromMember(member, verifiedRole).queue();
                        });
                    }

                    BotController.getUnlinkPlayers().remove(player);
                    Message.ACCOUNT_SUCCESSFULLY_UNLINKED.getFormattedText(true).forEach(player::sendMessage);
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getConfigManager().getBotSettings().getStringList("CommandsAfterUnlink").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
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
                            LocalDateTime.now().plusSeconds(Main.getInstance().getConfigManager().getBotSettings().getLong("2FASessionTime"))));
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getConfigManager().getBotSettings().getStringList("CommandsAfter2FAPassing").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
                    });
                }
                if(event.getReaction().getReactionEmote().getName().equals("❎")) {
                    BotController.getTwoFactorPlayers().remove(player);
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        player.kickPlayer(Message.TWOFACTOR_REJECTED.getText());
                        Main.getInstance().getConfigManager().getBotSettings().getStringList("CommandsAfter2FADeclining").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
                    });
                }

                event.getChannel().deleteMessageById(event.getMessageId()).queue();
            }
        }
    }

}
