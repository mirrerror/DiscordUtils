package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Link extends SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            Message.sendMessage(sender, Message.SENDER_IS_NOT_A_PLAYER, true);
            return;
        }
        if(args.length < 1) {
            Message.sendMessage(sender, Message.DISCORDUTILS_LINK_USAGE, true);
            return;
        }
        Player player = (Player) sender;
        if(DiscordUtils.isVerified(player)) {
            Message.sendMessage(player, Message.ACCOUNT_ALREADY_VERIFIED, true);
            return;
        }
        if(!BotController.getLinkCodes().containsKey(args[0])) {
            Message.sendMessage(player, Message.INVALID_LINK_CODE, true);
            return;
        }

        Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".userId", "" + BotController.getLinkCodes().get(args[0]).getId());
        Main.getInstance().getConfigManager().getData().set("DiscordLink." + player.getUniqueId() + ".2factor", false);
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
        BotController.getLinkCodes().remove(args[0]);
        Message.sendMessage(player, Message.ACCOUNT_SUCCESSFULLY_LINKED, true);
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
