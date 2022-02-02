package md.mirrerror.discordutils.discord;

import com.google.common.collect.Sets;
import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.utils.integrations.permissions.PermissionsIntegration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordUtils {

    private static final DatabaseManager databaseManager = Main.getDatabaseType().getDatabaseManager();

    public static boolean isVerified(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.playerExists(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(player.getUniqueId().toString());
    }

    public static boolean isVerified(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.playerExists(offlinePlayer.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(offlinePlayer.getUniqueId().toString());
    }

    public static boolean isVerified(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.userLinked(user.getIdLong());
        }
        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong()) return true;
        }
        return false;
    }

    public static User getDiscordUser(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            long id = databaseManager.getUserId(player.getUniqueId());
            return BotController.getJda().getUserById(id);
        }

        String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + player.getUniqueId().toString() + ".userId");
        if(id != null) {
            return BotController.getJda().getUserById(id);
        }

        return null;
    }

    public static User getDiscordUser(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            long id = databaseManager.getUserId(offlinePlayer.getUniqueId());
            return BotController.getJda().getUserById(id);
        }

        String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + offlinePlayer.getUniqueId().toString() + ".userId");
        if(id != null) {
            return BotController.getJda().getUserById(id);
        }

        return null;
    }

    public static OfflinePlayer getOfflinePlayer(User user) {
        if(isVerified(user)) {
            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
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
                UUID playerUniqueId = databaseManager.getPlayer(user.getIdLong());
                if(playerUniqueId != null) return Bukkit.getPlayer(playerUniqueId);
            }

            for (String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
                String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId");
                if(id != null) {
                    if(Long.parseLong(id) == user.getIdLong()) return Bukkit.getPlayer(UUID.fromString(s));
                }
            }
        }
        return null;
    }

    public static boolean hasTwoFactor(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.hasTwoFactor(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + player.getUniqueId() + ".2factor");
    }

    public static boolean hasTwoFactor(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.hasTwoFactor(databaseManager.getPlayer(user.getIdLong()));
        }

        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId");
            if(id != null) {
                if(Long.parseLong(id) == user.getIdLong()) return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + s + ".2factor");
            }
        }
        return false;
    }

    public static boolean isAdmin(Member member) {
        if(member != null) {
            return Sets.intersection(new HashSet<>(BotController.getAdminRoles()), new HashSet<>(member.getRoles())).size() > 0;
        }
        return false;
    }

    public static boolean isAdmin(Player player, Guild guild) {
        User user = DiscordUtils.getDiscordUser(player);
        if(user == null) return false;

        if(guild.isMember(user)) {
            return isAdmin(guild.getMember(user));
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

        User user = DiscordUtils.getDiscordUser(offlinePlayer);

        if(user == null) return;
        AtomicReference<Member> member = new AtomicReference<>();

        for(Long roleId : groupRoles.keySet()) {
            String group = groupRoles.get(roleId);
            Role role = BotController.getJda().getRoleById(roleId);
            if(groups.contains(group)) {
                BotController.getJda().getGuilds().forEach(guild -> {

                    if(guild.isMember(user)) member.set(guild.getMember(user));

                    if(member.get() != null) {
                        try {
                            if(role != null) if(isPartOfGuild(role, guild)) guild.addRoleToMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't assign a role for member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            } else {
                BotController.getJda().getGuilds().forEach(guild -> {

                    if(guild.isMember(user)) member.set(guild.getMember(user));

                    if(member.get() != null) {
                        try {
                            if(role != null) if(isPartOfGuild(role, guild)) guild.removeRoleFromMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't remove a role from member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            }
        }
    }

    public static void checkNames(OfflinePlayer offlinePlayer) {
        if(!DiscordUtils.isVerified(offlinePlayer)) return;

        BotController.getJda().getGuilds().forEach(guild -> {
            User user = DiscordUtils.getDiscordUser(offlinePlayer);
            if(user == null) return;

            if(guild.isMember(user)) {
                Member member = guild.getMember(user);
                if(member == null) return;
                if(!guild.getMember(BotController.getJda().getSelfUser()).canInteract(member)) return;
                member.modifyNickname(offlinePlayer.getName()).queue();
            }
        });
    }

    public static boolean isPartOfGuild(Role role, Guild guild) {
        return guild.getRoles().contains(role);
    }

    /*public static User getDiscordUser(Player player) {
        AtomicReference<User> userAtomicReference = new AtomicReference<>();
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {

            long id = databaseManager.getUserId(player.getUniqueId());

            if(id >= 0) {
                BotController.getJda().retrieveUserById(id).submit()
                        .whenComplete((user, error) -> {
                            if(error != null) {
                                userAtomicReference.set(user);
                            }
                        });
            }

            return userAtomicReference.get();
        }

        String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + player.getUniqueId().toString() + ".userId");
        if(id != null) {
            BotController.getJda().retrieveUserById(id).submit()
                    .whenComplete((user, error) -> {
                        if(error != null) {
                            userAtomicReference.set(user);
                        }
                    });
        }

        return userAtomicReference.get();
    }

    public static User getDiscordUser(OfflinePlayer offlinePlayer) {
        AtomicReference<User> userAtomicReference = new AtomicReference<>();
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {

            long id = databaseManager.getUserId(offlinePlayer.getUniqueId());

            if(id >= 0) {
                BotController.getJda().retrieveUserById(id).submit()
                        .whenComplete((user, error) -> {
                            if(error != null) {
                                userAtomicReference.set(user);
                            }
                        });
            }

            return userAtomicReference.get();
        }

        String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + offlinePlayer.getUniqueId().toString() + ".userId");
        if(id != null) {
            BotController.getJda().retrieveUserById(id).submit()
                    .whenComplete((user, error) -> {
                        if(error != null) {
                            userAtomicReference.set(user);
                        }
                    });
        }

        return userAtomicReference.get();
    }

    public static OfflinePlayer getOfflinePlayer(User user) {
        if(isVerified(user)) {
            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
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
                UUID playerUniqueId = databaseManager.getPlayer(user.getIdLong());
                if(playerUniqueId != null) return Bukkit.getPlayer(playerUniqueId);
            }

            for (String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
                String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId");
                if(id != null) {
                    if(Long.parseLong(id) == user.getIdLong()) return Bukkit.getPlayer(UUID.fromString(s));
                }
            }
        }
        return null;
    }

    public static boolean hasTwoFactor(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.hasTwoFactor(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + player.getUniqueId() + ".2factor");
    }

    public static boolean hasTwoFactor(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.hasTwoFactor(databaseManager.getPlayer(user.getIdLong()));
        }

        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId");
            if(id != null) {
                if(Long.parseLong(id) == user.getIdLong()) return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + s + ".2factor");
            }
        }
        return false;
    }

    public static boolean isAdmin(Member member) {
        if(member != null) {
            return Sets.intersection(new HashSet<>(BotController.getAdminRoles()), new HashSet<>(member.getRoles())).size() > 0;
        }
        return false;
    }

    public static boolean isAdmin(Player player, Guild guild) {
        User user = DiscordUtils.getDiscordUser(player);
        if(guild.isMember(user)) {
            return isAdmin(guild.getMember(user));
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

        User user = DiscordUtils.getDiscordUser(offlinePlayer);
        if(user == null) return;
        AtomicReference<Member> member = new AtomicReference<>();

        for(Long roleId : groupRoles.keySet()) {
            String group = groupRoles.get(roleId);
            Role role = BotController.getJda().getRoleById(roleId);
            if(groups.contains(group)) {
                BotController.getJda().getGuilds().forEach(guild -> {

                    if(guild.isMember(user)) member.set(guild.getMember(user));

                    if(member.get() != null) {
                        try {
                            if(role != null) guild.addRoleToMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't assign a role for member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            } else {
                BotController.getJda().getGuilds().forEach(guild -> {

                    if(guild.isMember(user)) member.set(guild.getMember(user));

                    if(member.get() != null) {
                        try {
                            if(role != null) guild.removeRoleFromMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't remove a role from member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            }
        }
    }

    public static void checkNames(OfflinePlayer offlinePlayer) {
        if(!DiscordUtils.isVerified(offlinePlayer)) return;

        BotController.getJda().getGuilds().forEach(guild -> {
            User user = DiscordUtils.getDiscordUser(offlinePlayer);
            if(user == null) return;

            if(guild.isMember(user)) {
                Member member = guild.getMember(user);
                if(member == null) return;
                if(!guild.getMember(BotController.getJda().getSelfUser()).canInteract(member)) return;
                member.modifyNickname(offlinePlayer.getName()).queue();
            }
        });
    }*/

    /*public static final int UNKNOWN_MEMBER_EXCEPTION = 10007;
    public static final int UNKNOWN_USER_EXCEPTION = 10013;

    public static boolean isVerified(Player player) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.playerExists(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(player.getUniqueId().toString());
    }

    public static boolean isVerified(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.playerExists(offlinePlayer.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").contains(offlinePlayer.getUniqueId().toString());
    }

    public static boolean isVerified(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.userLinked(user.getIdLong());
        }
        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong()) return true;
        }
        return false;
    }

    public static User getDiscordUser(Player player) {
        User user = null;
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {

            try {
                user = BotController.getJda().retrieveUserById(databaseManager.getUserId(player.getUniqueId())).complete();
            } catch (ErrorResponseException exception) {
                if(exception.getErrorCode() != UNKNOWN_USER_EXCEPTION) exception.printStackTrace();
            }

            return user;
        }

        try {
            user = BotController.getJda().retrieveUserById(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + player.getUniqueId().toString() + ".userId")).complete();
        } catch (ErrorResponseException exception) {
            if(exception.getErrorCode() != UNKNOWN_USER_EXCEPTION) exception.printStackTrace();
        }
        return user;
    }

    public static User getDiscordUser(OfflinePlayer offlinePlayer) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            User user = null;

            try {
                user = BotController.getJda().retrieveUserById(databaseManager.getUserId(offlinePlayer.getUniqueId())).complete();
            } catch (ErrorResponseException exception) {
                if(exception.getErrorCode() != UNKNOWN_USER_EXCEPTION) exception.printStackTrace();
            }

            return user;
        }

        String id = Main.getInstance().getConfigManager().getData().getString("DiscordLink." + offlinePlayer.getUniqueId().toString() + ".userId");
        if(id != null) {
            return BotController.getJda().retrieveUserById(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + offlinePlayer.getUniqueId().toString() + ".userId")).complete();
        } else return null;
    }

    public static OfflinePlayer getOfflinePlayer(User user) {
        if(isVerified(user)) {
            if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
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
            return databaseManager.hasTwoFactor(player.getUniqueId());
        }
        return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + player.getUniqueId() + ".2factor");
    }

    public static boolean hasTwoFactor(User user) {
        if(Main.getDatabaseType() != Main.DatabaseType.NONE) {
            return databaseManager.hasTwoFactor(databaseManager.getPlayer(user.getIdLong()));
        }

        for(String s : Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)) {
            if(Long.parseLong(Main.getInstance().getConfigManager().getData().getString("DiscordLink." + s + ".userId")) == user.getIdLong())
                return Main.getInstance().getConfigManager().getData().getBoolean("DiscordLink." + s + ".2factor");
        }
        return false;
    }

    public static boolean isAdmin(User user) {
        Member member = null;
        for(Guild guild : BotController.getJda().getGuilds()) {
            for(long role : BotController.getAdminRoles()) {
                try {
                    member = guild.retrieveMember(user).complete();
                } catch (ErrorResponseException exception) {
                    if(exception.getErrorCode() != UNKNOWN_MEMBER_EXCEPTION) exception.printStackTrace();
                }

                if(member != null) if(member.getRoles().contains(guild.getRoleById(role))) return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(Player player) {
        Member member = null;
        for(Guild guild : BotController.getJda().getGuilds()) {
            for(long role : BotController.getAdminRoles()) {
                try {
                    member = guild.retrieveMember(getDiscordUser(player)).complete();
                } catch (ErrorResponseException exception) {
                    if(exception.getErrorCode() != UNKNOWN_MEMBER_EXCEPTION) exception.printStackTrace();
                }

                if(member != null) if(member.getRoles().contains(guild.getRoleById(role))) return true;
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

        User user = DiscordUtils.getDiscordUser(offlinePlayer);
        AtomicReference<Member> member = new AtomicReference<>();

        for(Long roleId : groupRoles.keySet()) {
            String group = groupRoles.get(roleId);
            Role role = BotController.getJda().getRoleById(roleId);
            if(groups.contains(group)) {
                BotController.getJda().getGuilds().forEach(guild -> {

                    try {
                        member.set(guild.retrieveMember(user).complete());
                    } catch (ErrorResponseException exception) {
                        if(exception.getErrorCode() != UNKNOWN_MEMBER_EXCEPTION) exception.printStackTrace();
                    }

                    if(member.get() != null) {
                        try {
                            if(role != null) guild.addRoleToMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't assign a role for member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            } else {
                BotController.getJda().getGuilds().forEach(guild -> {

                    try {
                        member.set(guild.retrieveMember(user).complete());
                    } catch (ErrorResponseException exception) {
                        if(exception.getErrorCode() != UNKNOWN_MEMBER_EXCEPTION) exception.printStackTrace();
                    }

                    if(member.get() != null) {
                        try {
                            if(role != null) guild.removeRoleFromMember(member.get(), role).queue();
                        } catch (HierarchyException exception) {
                            Main.getInstance().getLogger().warning("Couldn't assign a role for member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                        }
                    }
                });
            }
        }
    }

    public static void checkNames(OfflinePlayer offlinePlayer) {
        if(!DiscordUtils.isVerified(offlinePlayer)) return;

        BotController.getJda().getGuilds().forEach(guild -> {
            User user = DiscordUtils.getDiscordUser(offlinePlayer);
            Member member = null;

            try {
                member = guild.retrieveMember(user).complete();
            } catch (ErrorResponseException exception) {
                if(exception.getErrorCode() != UNKNOWN_MEMBER_EXCEPTION) exception.printStackTrace();
            }
            if(member != null) {
                if(!guild.getMember(BotController.getJda().getSelfUser()).canInteract(member)) return;
                member.modifyNickname(offlinePlayer.getName()).queue();
            }
        });
    }*/

    public static void setupDelayedRolesCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)
                .forEach(verified -> checkRoles(Bukkit.getOfflinePlayer(UUID.fromString(verified)))),
                0L, Main.getInstance().getConfigManager().getConfig().getInt("Discord.DelayedRolesCheck.Delay")*20L);
        Main.getInstance().getLogger().info("Delayed roles check has been successfully enabled.");
    }

    public static void setupDelayedNamesCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)
                        .forEach(verified -> checkNames(Bukkit.getOfflinePlayer(UUID.fromString(verified)))),
                0L, Main.getInstance().getConfigManager().getConfig().getInt("Discord.DelayedNamesCheck.Delay")*20L);
        Main.getInstance().getLogger().info("Delayed names check has been successfully enabled.");
    }

}
