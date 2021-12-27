package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Link implements SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Message.SENDER_IS_NOT_A_PLAYER.getText(true));
            return;
        }

        if(args.length < 1) {
            sender.sendMessage(Message.DISCORDUTILS_LINK_USAGE.getText(true));
            return;
        }
        Player player = (Player) sender;
        if(DiscordUtils.isVerified(player)) {
            sender.sendMessage(Message.ACCOUNT_ALREADY_VERIFIED.getText(true));
            return;
        }

        if(!BotController.getLinkCodes().containsKey(args[0])) {
            sender.sendMessage(Message.INVALID_LINK_CODE.getText(true));
            return;
        }

        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            databaseManager.registerPlayer(player.getUniqueId(), Long.parseLong(BotController.getLinkCodes().get(args[0]).getId()), false);
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
        sender.sendMessage(Message.ACCOUNT_SUCCESSFULLY_LINKED.getText(true));
        Main.getInstance().getConfigManager().getConfig().getStringList("Discord.CommandsAfterVerification").forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.link";
    }
}
