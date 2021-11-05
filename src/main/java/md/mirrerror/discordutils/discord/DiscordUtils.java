package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.entity.Player;

public class DiscordUtils {

    public static boolean isVerified(Player player) {
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(player.getUniqueId().toString());
    }

    public static boolean isVerified(User user) {
        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong()) return true;
        }
        return false;
    }

    public static User getDiscordUser(Player player) {
        return BotController.getJda().retrieveUserById(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + player.getUniqueId().toString() + ".userId")).complete();
    }

    public static boolean hasTwoFactor(Player player) {
        return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + player.getUniqueId() + ".2factor");
    }

    public static boolean hasTwoFactor(User user) {
        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong())
                return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + s + ".2factor");
        }
        return false;
    }

    public static boolean isAdmin(User user) {
        for(Guild guild : BotController.getJda().getGuilds()) {
            for(long role : BotController.getAdminRoles()) {
                if(guild.retrieveMember(user).complete().getRoles().contains(guild.getRoleById(role))) return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(Player player) {
        for(Guild guild : BotController.getJda().getGuilds()) {
            for(long role : BotController.getAdminRoles()) {
                if(guild.retrieveMember(getDiscordUser(player)).complete().getRoles().contains(guild.getRoleById(role))) return true;
            }
        }
        return false;
    }

    public static Role getVerifiedRole(Guild guild) {
        long roleId = Main.getInstance().getConfigManager().getConfig().getLong("Discord.VerifiedRole.Id");
        if(roleId > 0) return guild.getRoleById(roleId);
        return null;
    }

}
