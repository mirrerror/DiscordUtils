package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.*;

public class BotController {

    private static Map<Long, String> groupRoles;
    private static List<Long> adminRoles;

    private static JDA jda;
    private String botPrefix = Main.getInstance().getConfigManager().getConfig().getString("Discord.BotPrefix");

    private static Map<String, User> linkCodes = new HashMap<>();
    private static Map<Player, String> twoFactorPlayers = new HashMap<>();
    private static Map<UUID, String> sessions = new HashMap<>();
    private static Map<User, LocalDateTime> voiceTime = new HashMap<>();
    private static Map<Player, Message> unlinkPlayers = new HashMap<>();

    private static List<Long> rewardBlacklistedVoiceChannels = new ArrayList<>();

    public static void setupBot(String token) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                jda = JDABuilder.createDefault(token).build().awaitReady();
                jda.addEventListener(new EventListener());
                //new SlashCommandsManager();
                setupGroupRoles();
                setupAdminRoles();
                setupRewardBlacklistedVoiceChannels();
                setupActivityChanger();
                if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.DelayedRolesCheck.Enabled")) {
                    DiscordUtils.setupDelayedRolesCheck();
                }
                if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.DelayedNamesCheck.Enabled")) {
                    DiscordUtils.setupDelayedNamesCheck();
                }
                Main.getInstance().getLogger().info("Bot successfully loaded.");
            } catch (LoginException | InterruptedException e) {
                Main.getInstance().getLogger().severe("Something went wrong while setting up the bot!");
                Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
            }
        });
    }

    public static void setupGroupRoles() {
        groupRoles = new HashMap<>();
        for(String s : Main.getInstance().getConfigManager().getConfig().getConfigurationSection("Discord.GroupRoles").getKeys(false)) {
            groupRoles.put(Long.parseLong(s), Main.getInstance().getConfigManager().getConfig().getString("Discord.GroupRoles." + s));
        }
    }

    public static void setupAdminRoles() {
        adminRoles = Main.getInstance().getConfigManager().getConfig().getLongList("Discord.AdminRoles");
    }

    public static void setupRewardBlacklistedVoiceChannels() {
        adminRoles = Main.getInstance().getConfigManager().getConfig().getLongList("Discord.GuildVoiceRewards.BlacklistedChannels");
    }

    public static void setupActivityChanger() {
        if(Main.getInstance().getActivityManager().getBotActivities().size() <= 1) return;

        long updateDelay = Main.getInstance().getConfigManager().getConfig().getLong("Discord.Activities.UpdateDelay");
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            Activity activity = Main.getInstance().getActivityManager().nextActivity();
            jda.getPresence().setActivity(activity);
        }, 0L, updateDelay*20L);
    }

    public static Map<Long, String> getGroupRoles() {
        return groupRoles;
    }

    public static JDA getJda() {
        return jda;
    }

    public String getBotPrefix() {
        return botPrefix;
    }

    public static Map<String, User> getLinkCodes() {
        return linkCodes;
    }

    public static Map<Player, String> getTwoFactorPlayers() {
        return twoFactorPlayers;
    }

    public static List<Long> getAdminRoles() {
        return adminRoles;
    }

    public static Map<UUID, String> getSessions() {
        return sessions;
    }

    public static Map<User, LocalDateTime> getVoiceTime() {
        return voiceTime;
    }

    public static List<Long> getRewardBlacklistedVoiceChannels() {
        return rewardBlacklistedVoiceChannels;
    }

    public static Map<Player, Message> getUnlinkPlayers() {
        return unlinkPlayers;
    }
}
