package md.mirrerror.discordutils.commands.discordutilsadmin;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ForceUnlink implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            Message.DISCORDUTILSADMIN_FORCEUNLINK_USAGE.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if(!DiscordUtils.isVerified(player)) {
            Message.ACCOUNT_IS_NOT_VERIFIED.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        User user = DiscordUtils.getDiscordUser(player);

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

                    if(user != null) if(guild.isMember(user)) member = guild.getMember(user);

                    if(verifiedRole != null && member != null) guild.removeRoleFromMember(member, verifiedRole).queue();
                });
            }
        }

        if(player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            BotController.getUnlinkPlayers().remove(onlinePlayer);
            Message.DISCORDUTILSADMIN_FORCEUNLINK_SUCCESSFUL_TO_TARGET.getFormattedText(true).forEach(msg -> {
                onlinePlayer.sendMessage(msg.replace("%sender%", sender.getName()).replace("%target%", player.getName()));
            });
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfterUnlink").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
        });

        Message.DISCORDUTILSADMIN_FORCEUNLINK_SUCCESSFUL_TO_SENDER.getFormattedText(true).forEach(msg -> {
            sender.sendMessage(msg.replace("%sender%", sender.getName()).replace("%target%", player.getName()));
        });
    }

    @Override
    public String getName() {
        return "forceunlink";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutilsadmin.forceunlink";
    }

    @Override
    public List<String> getAliases() {
        return Collections.unmodifiableList(Arrays.asList("funlink", "fulink", "forceulink"));
    }

}
