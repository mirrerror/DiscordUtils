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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
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
            List<Role> adminRoles = new ArrayList<>();
            BotController.getAdminRoles().forEach(roleId -> {
                Role role = member.getGuild().getRoleById(roleId);
                if(role != null) adminRoles.add(member.getGuild().getRoleById(roleId));
            });
            return Sets.intersection(new HashSet<>(adminRoles), new HashSet<>(member.getRoles())).size() > 0;
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
        long roleId = Main.getInstance().getConfigManager().getBotSettings().getLong("VerifiedRole.Id");
        if(roleId > 0) return guild.getRoleById(roleId);
        return null;
    }

    public static void checkRoles(OfflinePlayer offlinePlayer) {
        if(!DiscordUtils.isVerified(offlinePlayer)) return;
        PermissionsIntegration permissionsIntegration = Main.getPermissionsPlugin().getPermissionsIntegration();
        if(permissionsIntegration == null) return;

        User user = DiscordUtils.getDiscordUser(offlinePlayer);
        if(user == null) return;
        AtomicReference<Member> member = new AtomicReference<>();

        Map<Long, String> groupRoles = BotController.getGroupRoles();

        if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("RolesSynchronization.AssignOnlyPrimaryGroup")) {

            String primaryGroup = permissionsIntegration.getHighestUserGroup(offlinePlayer);

            for(Long roleId : groupRoles.keySet()) {
                String group = groupRoles.get(roleId);
                Role role = BotController.getJda().getRoleById(roleId);

                if(primaryGroup.equals(group)) {
                    BotController.getJda().getGuilds().forEach(guild -> {

                        if(guild.isMember(user)) member.set(guild.getMember(user));

                        if(member.get() != null) {
                            try {
                                if(role != null)
                                    if(isPartOfGuild(role, guild))
                                        if(!member.get().getRoles().contains(role)) guild.addRoleToMember(member.get(), role).queue();
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
                                if(role != null)
                                    if(isPartOfGuild(role, guild))
                                        if(member.get().getRoles().contains(role)) guild.removeRoleFromMember(member.get(), role).queue();
                            } catch (HierarchyException exception) {
                                Main.getInstance().getLogger().warning("Couldn't remove a role from member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                            }
                        }
                    });
                }
            }

        } else {

            List<String> groups = permissionsIntegration.getUserGroups(offlinePlayer);

            for(Long roleId : groupRoles.keySet()) {
                String group = groupRoles.get(roleId);
                Role role = BotController.getJda().getRoleById(roleId);

                if(groups.contains(group)) {
                    BotController.getJda().getGuilds().forEach(guild -> {

                        if(guild.isMember(user)) member.set(guild.getMember(user));

                        if(member.get() != null) {
                            try {
                                if(role != null)
                                    if(isPartOfGuild(role, guild))
                                        if(!member.get().getRoles().contains(role)) guild.addRoleToMember(member.get(), role).queue();
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
                                if(role != null)
                                    if(isPartOfGuild(role, guild))
                                        if(member.get().getRoles().contains(role)) guild.removeRoleFromMember(member.get(), role).queue();
                            } catch (HierarchyException exception) {
                                Main.getInstance().getLogger().warning("Couldn't remove a role from member: " + user.getAsTag() + ". The bot probably doesn't have the needed permissions.");
                            }
                        }
                    });
                }
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

                String format = Main.getInstance().getConfigManager().getBotSettings().getString("NamesSynchronization.NamesSyncFormat")
                        .replace("%player%", offlinePlayer.getName());
                if(Main.getInstance().getPapiManager().isEnabled()) format = Main.getInstance().getPapiManager().setPlaceholders(offlinePlayer, format);

                if(member.getNickname() != null) if(member.getNickname().equals(format)) return;

                member.modifyNickname(format).queue();
            }
        });
    }

    public static boolean isPartOfGuild(Role role, Guild guild) {
        return guild.getRoles().contains(role);
    }

    public static void setupDelayedRolesCheck() {
        new BukkitRunnable() {

            @Override
            public void run() {
                Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false).forEach(verified -> {

                    try {
                        checkRoles(Bukkit.getOfflinePlayer(UUID.fromString(verified)));
                    } catch (NoClassDefFoundError exception) {
                        Main.getInstance().getLogger().severe("Delayed roles check has been disabled due to an error. It seems like you don't have the permission plugin that is set up in your config file.");
                        super.cancel();
                    }

                });
            }

        }.runTaskTimerAsynchronously(Main.getInstance(), 0L, Main.getInstance().getConfigManager().getBotSettings().getInt("DelayedRolesCheck.Delay")*20L);

        Main.getInstance().getLogger().info("Delayed roles check has been successfully enabled.");
    }

    public static void setupDelayedNamesCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> Main.getInstance().getConfigManager().getData().getConfigurationSection("DiscordLink").getKeys(false)
                        .forEach(verified -> checkNames(Bukkit.getOfflinePlayer(UUID.fromString(verified)))),
                0L, Main.getInstance().getConfigManager().getBotSettings().getInt("DelayedNamesCheck.Delay")*20L);
        Main.getInstance().getLogger().info("Delayed names check has been successfully enabled.");
    }

}
