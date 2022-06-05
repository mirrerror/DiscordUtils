package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Link implements SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            Message.SENDER_IS_NOT_A_PLAYER.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        if(args.length < 1) {
            Message.DISCORDUTILS_LINK_USAGE.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }
        Player player = (Player) sender;
        if(DiscordUtils.isVerified(player)) {
            Message.ACCOUNT_ALREADY_VERIFIED.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        if(!BotController.getLinkCodes().containsKey(args[0])) {
            Message.INVALID_LINK_CODE.getFormattedText(true).forEach(sender::sendMessage);
            return;
        }

        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            Main.getDatabaseType().getDatabaseManager().registerPlayer(player.getUniqueId(), Long.parseLong(BotController.getLinkCodes().get(args[0]).getId()), false);
        } else {
            Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".userId", "" + BotController.getLinkCodes().get(args[0]).getId());

            boolean defaultValue = Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.Default2FAValue");

            Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", defaultValue);
            Main.getInstance().getConfigManager().saveConfigFiles();
            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.VerifiedRole.Enabled")) {
                long roleId = Main.getInstance().getConfigManager().getConfig().getLong("Discord.VerifiedRole.Id");
                if(roleId > 0) {
                    BotController.getJda().getGuilds().forEach(guild -> {
                        Role verifiedRole = DiscordUtils.getVerifiedRole(guild);
                        if(verifiedRole != null) guild.addRoleToMember(guild.retrieveMember(BotController.getLinkCodes().get(args[0])).complete(), verifiedRole).queue();
                    });
                }
            }
        }
        BotController.getLinkCodes().remove(args[0]);
        Message.ACCOUNT_SUCCESSFULLY_LINKED.getFormattedText(true).forEach(sender::sendMessage);
        Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfterVerification").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.link";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }
}
