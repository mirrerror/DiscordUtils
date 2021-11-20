package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.integrations.permissions.PermissionsIntegration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscordUtils {

    public static boolean isVerified(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return databaseManager.playerExists(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(player.getUniqueId().toString());
    }

    public static boolean isVerified(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return databaseManager.playerExists(offlinePlayer.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(offlinePlayer.getUniqueId().toString());
    }

    public static boolean isVerified(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return databaseManager.userLinked(user.getIdLong());
        }
        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong()) return true;
        }
        return false;
    }

    public static User getDiscordUser(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return BotController.getJda().retrieveUserById(databaseManager.getUserId(player.getUniqueId())).complete();
        }
        return BotController.getJda().retrieveUserById(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + player.getUniqueId().toString() + ".userId")).complete();
    }

    public static User getDiscordUser(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return BotController.getJda().retrieveUserById(databaseManager.getUserId(offlinePlayer.getUniqueId())).complete();
        }
        return BotController.getJda().retrieveUserById(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + offlinePlayer.getUniqueId().toString() + ".userId")).complete();
    }

    public static OfflinePlayer getOfflinePlayer(User user) {
        if(isVerified(user)) {
            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
                return Bukkit.getOfflinePlayer(databaseManager.getPlayer(user.getIdLong()));
            }

            for (String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
                if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong())
                    return Bukkit.getOfflinePlayer(UUID.fromString(s));
            }
        }
        return null;
    }

    public static Player getPlayer(User user) {
        if(isVerified(user)) {
            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
                DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
                return Bukkit.getPlayer(databaseManager.getPlayer(user.getIdLong()));
            }

            for (String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
                if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong())
                    return Bukkit.getPlayer(UUID.fromString(s));
            }
        }
        return null;
    }

    public static boolean hasTwoFactor(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return databaseManager.hasTwoFactor(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + player.getUniqueId() + ".2factor");
    }

    public static boolean hasTwoFactor(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();
            return databaseManager.hasTwoFactor(databaseManager.getPlayer(user.getIdLong()));
        }

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

    public static void checkRoles(OfflinePlayer offlinePlayer) {
        if(!DiscordUtils.isVerified(offlinePlayer)) return;
        PermissionsIntegration permissionsIntegration = Main.getPermissionsPlugin().getPermissionsIntegration();
        if(permissionsIntegration == null) return;
        List<String> groups = permissionsIntegration.getUserGroups(offlinePlayer);
        Map<Long, String> groupRoles = BotController.getGroupRoles();
        /*for(String s : groups) {
            if(groupRoles.containsValue(s)) {
                groupRoles.forEach((groupId, group) -> { if(group.equals(s)) {
                    Role role = BotController.getJda().getRoleById(groupId);
                    BotController.getJda().getGuilds().forEach(guild -> {
                        if(DiscordUtils.isVerified(player)) {
                            User user = DiscordUtils.getDiscordUser(player);
                            if(guild.retrieveMember(user).complete() != null) {
                                if(role != null) guild.addRoleToMember(guild.retrieveMember(user).complete(), role).queue();
                            }
                        }
                    });
                }});
            }
        }*/
        for(Long roleId : groupRoles.keySet()) {
            String group = groupRoles.get(roleId);
            Role role = BotController.getJda().getRoleById(roleId);
            if(groups.contains(group)) {
                BotController.getJda().getGuilds().forEach(guild -> {
                    User user = DiscordUtils.getDiscordUser(offlinePlayer);
                    if(guild.retrieveMember(user).complete() != null) {
                        if(role != null) guild.addRoleToMember(guild.retrieveMember(user).complete(), role).queue();
                    }
                });
            } else {
                BotController.getJda().getGuilds().forEach(guild -> {
                    User user = DiscordUtils.getDiscordUser(offlinePlayer);
                    if(guild.retrieveMember(user).complete() != null) {
                        if(role != null) guild.removeRoleFromMember(guild.retrieveMember(user).complete(), role).queue();
                    }
                });
            }
        }
    }

    public static void setupDelayedRolesCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)
                .forEach(verified -> checkRoles(Bukkit.getOfflinePlayer(UUID.fromString(verified)))),
                0L, Main.getInstance().getConfigManager().getConfig().getInt("Discord.DelayedRolesCheck.Delay")*20L);
        Main.getInstance().getLogger().info("Delayed roles check has been successfully enabled.");
    }

}
